package com.gunjun.android.personalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gunjun.android.personalproject.models.Profile;

import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();
        List<Profile> query = realm.where(Profile.class).findAll();
        if(query.size() < 1) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
