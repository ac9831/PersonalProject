package com.gunjun.android.personalproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.gunjun.android.personalproject.api.FacebookSession;
import com.gunjun.android.personalproject.api.GoogleSession;
import com.gunjun.android.personalproject.api.InstagramApp;
import com.gunjun.android.personalproject.api.InstagramSession;

import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;

    private CallbackManager callbackManager;
    private InstagramSession instagramSession;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth auth;
    private String token;
    private FacebookSession facebookSession;
    private GoogleSession googleSession;
    private InstagramApp mApp;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch(buttonView.getId()) {
                case R.id.google_check:
                    if (isChecked) {
                        google();
                    } else {
                        googleSession.resetAccessToken();
                        googleApiClient.clearDefaultAccountAndReconnect();
                        googleCheck.setChecked(false);
                        googleText.setText(R.string.not_connect);
                    }
                    break;
                case R.id.facebook_check:
                    if (isChecked) {
                        facebook();
                    } else {
                        facebookSession.resetAccessToken();
                        facebookCheck.setChecked(false);
                        facebookText.setText(R.string.not_connect);
                    }
                    break;
                case R.id.instagram_check:
                    if (isChecked) {
                        connectOrDisconnectUser();
                    } else {
                        connectOrDisconnectUser();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
                instagramCheck.setChecked(true);
                instagramText.setText(R.string.connect);
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(SettingActivity.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });


    @BindView(R.id.facebook_check)
    protected CheckBox facebookCheck;

    @BindView(R.id.instagram_check)
    protected CheckBox instagramCheck;

    @BindView(R.id.google_check)
    protected CheckBox googleCheck;

    @BindView(R.id.setting_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.google_status)
    protected TextView googleText;

    @BindView(R.id.facebook_status)
    protected TextView facebookText;

    @BindView(R.id.instagram_status)
    protected TextView instagramText;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initUI();
        instagramSetting();
        GoogleLogin();
        FacebookLogin();
    }

    private void initUI() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Setting");
        callbackManager = CallbackManager.Factory.create();
        instagramSession = new InstagramSession(this);
        facebookSession = new FacebookSession(this);
        googleSession = new GoogleSession(this);
        if (facebookSession.getAccessToken() != null) {
            facebookCheck.setChecked(true);
            facebookText.setText(R.string.connect);
        }

        if(googleSession.getAccessToken() != null) {
            googleCheck.setChecked(true);
            googleText.setText(R.string.connect);
        }

        if(instagramSession.getAccessToken() != null) {
            instagramCheck.setChecked(true);
            instagramText.setText(R.string.connect);
        }

        googleCheck.setOnCheckedChangeListener(onCheckedChangeListener );
        instagramCheck.setOnCheckedChangeListener(onCheckedChangeListener);
        facebookCheck.setOnCheckedChangeListener(onCheckedChangeListener);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void instagramSetting() {
        mApp = new InstagramApp(this, BuildConfig.CLIENT_ID,
                BuildConfig.CLIENT_SECRET, BuildConfig.CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                mApp.fetchUserName(handler);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(SettingActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        if (mApp.hasAccessToken()) {
            mApp.fetchUserName(handler);
        }
    }

    // instagram
    private void connectOrDisconnectUser() {
        if (mApp.hasAccessToken()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    SettingActivity.this);
            builder.setMessage("Disconnect from Instagram?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    instagramSession.resetAccessToken();
                                    instagramCheck.setChecked(false);
                                    instagramText.setText(R.string.not_connect);
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();

        } else {
            mApp.authorize();
        }
    }


    // google
    private void GoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        auth = FirebaseAuth.getInstance();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        token = acct.getId();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            googleSession.storeAccessToken(token);
                            googleCheck.setChecked(true);
                            googleText.setText("연결됨");
                        }
                    }
                });
    }

    public void google() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    // facebook
    private void FacebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookSession.storeAccessToken(loginResult.getAccessToken().toString());
                facebookCheck.setChecked(true);
                facebookText.setText(R.string.connect);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("error", error + "");
            }
        });
    }

    public void facebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logInWithReadPermissions(SettingActivity.this, Arrays.asList("public_profile", "email"));
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("A", "onConnectionFailed:" + connectionResult);
    }
}
