package com.gunjun.android.personalproject.api;

import android.os.Handler;

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
    private static final String API_URL = "https://api.instagram.com/v1/users/self";


    public void getAllMediaImages(final Handler handler, final InstagramSession instagramSession) {
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
}
