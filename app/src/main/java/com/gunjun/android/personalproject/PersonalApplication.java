package com.gunjun.android.personalproject;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by gunjunLee on 2017-02-06.
 */

public class PersonalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
