package com.gunjun.android.personalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.gunjun.android.personalproject.models.Profile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class ProfileActivity extends AppCompatActivity  {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private Uri imageCaptureUri;
    private Realm realm;

    @BindView(R.id.profile_age)
    protected EditText profileAge;

    @BindView(R.id.profile_height)
    protected EditText profileHeight;

    @BindView(R.id.profile_weight)
    protected EditText profileWeight;

    @BindView(R.id.profile_name)
    protected EditText profileName;

    @BindView(R.id.profile_goal_step)
    protected EditText profileGoalStep;

    @BindView(R.id.profile_image)
    protected CircleImageView circleImageView;

    @BindView(R.id.my_toolbar)
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        permissionCreate();
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Profile");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else  if (id == R.id.action_check) {
            onClickFinish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void onClickFinish() {

        List<Profile> query = realm.where(Profile.class).findAll();
        Profile profile;
        realm.beginTransaction();
        if(query.size() < 1) {
            profile = realm.createObject(Profile.class, 1);
        } else {
            profile = query.get(0);
        }
        profile.setName(profileName.getText().toString());

        if (!profileAge.getText().toString().equals("")) {
            profile.setAge(Integer.parseInt(profileAge.getText().toString()));
        } else {
            profile.setAge(0);
        }

        if (!profileWeight.getText().toString().equals("")) {
            profile.setWeight(Integer.parseInt(profileWeight.getText().toString()));
        } else {
            profile.setWeight(0);
        }

        if (!profileHeight.getText().toString().equals("")) {
            profile.setHeight(Integer.parseInt(profileHeight.getText().toString()));
        } else {
            profile.setHeight(0);
        }

        if (!profileGoalStep.getText().toString().equals("")) {
            profile.setGoalStep(Integer.parseInt(profileGoalStep.getText().toString()));
        } else {
            profile.setGoalStep(0);
        }

        realm.commitTransaction();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)
            return;

        switch(requestCode) {
            case PICK_FROM_ALBUM: {
                imageCaptureUri = data.getData();
                cropImage();
                break;
            }

            case PICK_FROM_CAMERA: {
                cropImage();
                break;
            }
            case CROP_FROM_IMAGE: {
                final Bundle extras = data.getExtras();

                String filePath = this.getFilesDir().getAbsolutePath() +
                        File.separator + "personalProfile" + File.separator + System.currentTimeMillis() + ".jpg";

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    circleImageView.setImageBitmap(photo);

                    storeCropImage(photo, filePath);
                }

                File f = new File(imageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    // 모든 작업에 있어 사전에 FALG_GRANT_WRITE_URI_PERMISSION과 READ 퍼미션을 줘야 uri를 활용한 작업에 지장을 받지 않는다는 것이 핵심입니다.
    // 해결방안은 addFlags와 Permission 체크 입니다.
    public void cropImage() {
        this.grantUriPermission("com.android.camera", imageCaptureUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageCaptureUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_FROM_IMAGE);
    }

    public void imageSelect(View v) {
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakePhotoAction();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakeAlbumAction();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드 방법")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();
    }

    private void createDir(String path) {
        String dirPath = this.getFilesDir().getAbsolutePath() + path;
        File directorySmartWheel = new File(dirPath);
        if (!directorySmartWheel.exists()) {
            directorySmartWheel.mkdir();
        }
    }

    public void doTakePhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(getFilesDir(), url);
        imageCaptureUri = FileProvider.getUriForFile(this, "com.gunjun.android.fileprovider", file);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }


    public void doTakeAlbumAction()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        createDir(File.separator  + "personalProfile");

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    FileProvider.getUriForFile(this, "com.gunjun.android.fileprovider",copyFile)));

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void permissionCreate() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(ProfileActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(ProfileActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }
}
