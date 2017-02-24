package com.gunjun.android.personalproject.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gunjunLee on 2017-02-15.
 */

public class GoogleSession {

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private static final String SHARED = "Google_Preferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    public GoogleSession(Context context) {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }


    public void storeAccessToken(String accessToken, String id, String username, String name) {
        editor.putString(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public void storeAccessToken(String accessToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.commit();
    }


    public void resetAccessToken() {
        editor.putString(KEY_ID, null);
        editor.putString(KEY_NAME, null);
        editor.putString(KEY_ACCESS_TOKEN, null);
        editor.putString(KEY_USERNAME, null);
        editor.commit();
    }


    public String getUsername() {
        return sharedPref.getString(KEY_USERNAME, null);
    }

    public String getId() {
        return sharedPref.getString(KEY_ID, null);
    }

    public String getName() {
        return sharedPref.getString(KEY_NAME, null);
    }


    public String getAccessToken() {
        return sharedPref.getString(KEY_ACCESS_TOKEN, null);
    }
}
