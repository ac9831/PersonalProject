package com.gunjun.android.personalproject.api;

import android.os.Handler;

import com.gunjun.android.personalproject.BuildConfig;

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
    private static final String TAG_IMAGES = "images";
    private static final String TAG_THUMBNAIL = "thumbnail";
    private static final String TAG_URL = "url";


    private void getAllMediaImages(final Handler handler, InstagramSession instagramSession) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = jsonParser
                            .getJSONFromUrlByGet("https://api.instagram.com/v1/users/self"
                                    + "/media/recent/?client_id="
                                    + BuildConfig.CLIENT_ID);
                    JSONArray data = jsonObject.getJSONArray(TAG_DATA);
                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        JSONObject data_obj = data.getJSONObject(data_i);

                        JSONObject images_obj = data_obj
                                .getJSONObject(TAG_IMAGES);

                        JSONObject thumbnail_obj = images_obj
                                .getJSONObject(TAG_THUMBNAIL);


                        String str_url = thumbnail_obj.getString(TAG_URL);
                        imageThumbList.add(str_url);
                    }

                    System.out.println("jsonObject::" + jsonObject);

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
