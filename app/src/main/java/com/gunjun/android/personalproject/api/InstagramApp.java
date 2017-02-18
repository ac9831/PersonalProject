package com.gunjun.android.personalproject.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class InstagramApp {
    private InstagramSession session;
    private InstagramDialog dialog;
    private OAuthAuthenticationListener authListener;
    private HashMap<String, String> userInfo = new HashMap<String, String>();
    private ProgressDialog progress;
    private String authUrl;
    private String tokenUrl;
    private String accessToken;
    private Context ctx;
    private String clientId;
    private String clientSecret;

    public static int WHAT_FINALIZE = 0;
    public static int WHAT_ERROR = 1;
    public static int WHAT_FETCH_INFO = 2;

    public static String callbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_URL = "https://api.instagram.com/v1";

    private static final String TAG = "InstagramAPI";

    public InstagramApp(Context context, String clientId, String clientSecret,
                        String callbackUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.ctx = context;
        this.session = new InstagramSession(context);
        this.accessToken = this.session.getAccessToken();
        this.callbackUrl = callbackUrl;
        this.tokenUrl = TOKEN_URL + "?client_id=" + clientId + "&client_secret="
                + clientSecret + "&redirect_uri=" + this.callbackUrl + "&grant_type=authorization_code";
        this.authUrl = AUTH_URL + "?client_id=" + clientId + "&redirect_uri="
                + this.callbackUrl + "&response_type=code&display=touch&scope=likes+comments+relationships";
        InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                getAccessToken(code);
            }

            @Override
            public void onError(String error) {
                authListener.onFail("Authorization failed");
            }
        };

        this.dialog = new InstagramDialog(context, this.authUrl, listener);
        this.progress = new ProgressDialog(context);
        this.progress.setCancelable(false);
    }

    private void getAccessToken(final String code) {
        this.progress.setMessage("Getting access token ...");
        this.progress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    StringBuilder sb = new StringBuilder();
                    sb.append("client_id=");
                    sb.append(clientId);
                    sb.append("&client_secret=");
                    sb.append(clientSecret);
                    writer.write("client_id="+clientId+
                            "&client_secret="+clientSecret+
                            "&grant_type=authorization_code" +
                            "&redirect_uri="+callbackUrl+
                            "&code=" + code);
                    writer.flush();
                    String response = streamToString(urlConnection.getInputStream());
                    Log.i(TAG, "response " + response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    accessToken = jsonObj.getString("access_token");
                    Log.i(TAG, "Got access token: " + accessToken);
                    String id = jsonObj.getJSONObject("user").getString("id");
                    String user = jsonObj.getJSONObject("user").getString("username");
                    String name = jsonObj.getJSONObject("user").getString("full_name");
                    session.storeAccessToken(accessToken, id, user, name);
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));

            }
        }.start();
    }

    public void     fetchUserName(final Handler handler) {
        progress.setMessage("Finalizing ...");
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FINALIZE;
                try {
                    URL url = new URL(API_URL + "/users/" + session.getId() + "/?access_token=" + accessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    String name = jsonObj.getJSONObject("data").getString("full_name");
                    String bio = jsonObj.getJSONObject("data").getString("bio");
                    Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                handler.sendMessage(handler.obtainMessage(what, 2, 0));
            }
        }.start();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                progress.dismiss();
                if(msg.arg1 == 1) {
                    authListener.onFail("Failed to get access token");
                }
                else if(msg.arg1 == 2) {
                    authListener.onFail("Failed to get user information");
                }
            }
            else if(msg.what == WHAT_FETCH_INFO) {
                progress.dismiss();
                authListener.onSuccess();
            }
        }
    };

    public boolean hasAccessToken() {
        return (this.accessToken == null) ? false : true;
    }

    public void setListener(OAuthAuthenticationListener listener) {
        authListener = listener;
    }

    public String getUserName() {
        return this.session.getUsername();
    }

    public String getId() {
        return this.session.getId();
    }
    public String getName() {
        return this.session.getName();
    }
    public void authorize() {
        this.dialog.show();
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }

    public void resetAccessToken() {
        if (accessToken != null) {
            session.resetAccessToken();
            accessToken = null;
        }
    }

    public HashMap<String, String> getUserInfo() {
        return userInfo;
    }

    public interface OAuthAuthenticationListener {
        public abstract void onSuccess();

        public abstract void onFail(String error);
    }


}