package com.gunjun.android.personalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.gunjun.android.personalproject.behavior.BottomNavigationViewHelper;
import com.gunjun.android.personalproject.fragment.DashBoardFragment;
import com.gunjun.android.personalproject.fragment.YoutubeFragment;
import com.gunjun.android.personalproject.models.Profile;
import com.gunjun.android.personalproject.service.ShakeService;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;


public class MainActivity extends AppCompatActivity {


    private GoogleAccountCredential credential;
    private Realm realm;
    private AsyncTask<Void, Void, Void> mTask;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @BindView(R.id.bottom_navigation)
    protected BottomNavigationView bottomNavigationView;

    @BindView(R.id.fab)
    protected CircleImageView floatingActionButton;

    @BindView(R.id.main_toolbar)
    protected Toolbar toolbar;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        List<Profile> query = realm.where(Profile.class).findAll();
        if(query.size() < 1) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else {
            File imgFile = new  File(query.get(0).getImgPath());
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                floatingActionButton.setImageBitmap(myBitmap);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Personal");

        fragment = new DashBoardFragment();
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment, fragment).commit();

        bottomSetting();

        Intent service = new Intent(this, ShakeService.class);
        startService(service);
    }

    public void bottomSetting() {
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.findViewById(floatingActionButton.getId());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        return true;
                    case R.id.action_rss:
                        return true;
                    case R.id.action_video:
                        fragment = new YoutubeFragment();
                        break;
                    case R.id.action_sns:
                        return true;
                }
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_fragment, fragment).commit();
                return true;
            }


        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        stopService(new Intent(this, ShakeService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
