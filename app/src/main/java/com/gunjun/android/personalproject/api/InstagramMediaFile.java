package com.gunjun.android.personalproject.api;

import android.os.Handler;

import com.gunjun.android.personalproject.interfaces.InstagramReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gunjunLee on 2017-02-16.
 */

public class InstagramMediaFile {

    private ArrayList<String> imageThumbList = new ArrayList<String>();
    private int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private static final String TAG_DATA = "data";
    private static final String TAG_COUNT = "counts";
    private static final String TAG_MEDIA = "media";
    private static final String TAG_FOLLOW = "follows";
    private static final String TAG_FOLLOWER = "followed_by";
    private static final String TAG_IMAGES = "images";
    private static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_URL = "url";

    private static final String API_URL = "https://api.instagram.com/v1/users/self";

    private InstagramReceiver instagramReceiver;

    public ArrayList<String> getImageThumbList() {
        return this.imageThumbList;
    }

    public void setInstagramReceiver(InstagramReceiver instagramReceiver) {
        this.instagramReceiver = instagramReceiver;
    }

    public void getUserInfo(final Handler handler, final InstagramSession instagramSession) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    if(instagramSession.getAccessToken() == null ) {
                        what = WHAT_ERROR;
                    } else {
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = jsonParser
                                .getJSONFromUrlByGet(API_URL
                                        + "/?access_token="
                                        + instagramSession.getAccessToken());
                        JSONObject data = jsonObject.getJSONObject(TAG_DATA);
                        JSONObject counts = data.getJSONObject(TAG_COUNT);
                        String media = counts.getString(TAG_MEDIA);
                        String follow = counts.getString(TAG_FOLLOW);
                        String follower = counts.getString(TAG_FOLLOWER);
                        instagramSession.setInstagramInfo(media, follow, follower);
                        System.out.println("jsonObject::" + jsonObject);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = WHAT_ERROR;
                }
                // pd.dismiss();
                handler.sendMessage(handler.obtainMessage(what, 2, 0));
            }
        }).start();
    }

    public void getAllMediaImages(final Handler handler, final InstagramSession instagramSession) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = jsonParser
                            .getJSONFromUrlByGet("https://api.instagram.com/v1/users/"
                                    + instagramSession.getId()
                                    + "/media/recent/?access_token="
                                    + instagramSession.getAccessToken()
                                    + "&count="
                                    + instagramSession.getMedia());

                    JSONArray data = jsonObject.getJSONArray(TAG_DATA);
                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        JSONObject data_obj = data.getJSONObject(data_i);
                        JSONObject images_obj = data_obj
                                .getJSONObject(TAG_IMAGES);

                        JSONObject thumbnail_obj = images_obj
                                .getJSONObject(TAG_THUMBNAIL);

                        // String str_height =
                        // thumbnail_obj.getString(TAG_HEIGHT);
                        //
                        // String str_width =
                        // thumbnail_obj.getString(TAG_WIDTH);

                        String str_url = thumbnail_obj.getString(TAG_URL);
                        imageThumbList.add(str_url);
                    }

                    System.out.println("jsonObject::" + jsonObject);
                    instagramReceiver.onInstagramImageReceived(imageThumbList);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }
}
