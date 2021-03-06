package com.gunjun.android.personalproject.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gunjunLee on 2017-02-15.
 */

public class InstagramSession {

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private static final String SHARED = "Instagram_Preferences";
    private static final String API_USERNAME = "username";
    private static final String API_ID = "id";
    private static final String API_NAME = "name";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_FOLLOW = "follows";
    private static final String API_FOLLOWER = "followed_by";
    private static final String API_MEDIA = "media";

    public InstagramSession(Context context) {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }


    public void storeAccessToken(String accessToken, String id, String username, String name) {
        editor.putString(API_ID, id);
        editor.putString(API_NAME, name);
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.putString(API_USERNAME, username);
        editor.commit();
    }

    public void storeAccessToken(String accessToken) {
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public void setInstagramInfo(String media, String follow, String follower) {
        editor.putString(API_MEDIA, media);
        editor.putString(API_FOLLOW, follow);
        editor.putString(API_FOLLOWER, follower);
        editor.commit();
    }

    public void resetAccessToken() {
        editor.putString(API_ID, null);
        editor.putString(API_NAME, null);
        editor.putString(API_ACCESS_TOKEN, null);
        editor.putString(API_USERNAME, null);
        editor.commit();
    }


    public String getUsername() {
        return sharedPref.getString(API_USERNAME, null);
    }

    public String getId() {
        return sharedPref.getString(API_ID, null);
    }

    public String getName() {
        return sharedPref.getString(API_NAME, null);
    }

    public String getAccessToken() {
        return sharedPref.getString(API_ACCESS_TOKEN, null);
    }

    public String getMedia() {
        return sharedPref.getString(API_MEDIA, null);
    }

    public String getFollow() {
        return sharedPref.getString(API_FOLLOW, null);
    }

    public String getFollower() {
        return sharedPref.getString(API_FOLLOWER, null);
    }
}
