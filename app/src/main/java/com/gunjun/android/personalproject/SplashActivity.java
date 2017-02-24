package com.gunjun.android.personalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.gunjun.android.personalproject.api.FacebookSession;
import com.gunjun.android.personalproject.api.GoogleSession;

public class SplashActivity extends AppCompatActivity {

    private FacebookSession facebookSession;
    private GoogleSession googleSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler hd = new Handler();
        facebookSession = new FacebookSession(this);
        googleSession = new GoogleSession(this);
        hd.postDelayed(new splashhandler() , 3000);

    }

    private class splashhandler implements Runnable{
        public void run() {


            if(facebookSession.getAccessToken() != null || googleSession.getAccessToken() != null) {
                startActivity(new Intent(getApplication(), MainActivity.class));
                SplashActivity.this.finish();
            } else {
                startActivity(new Intent(getApplication(), LoginActivity.class));
                SplashActivity.this.finish();
            }

        }
    }
}
