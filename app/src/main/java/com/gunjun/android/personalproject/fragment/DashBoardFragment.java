package com.gunjun.android.personalproject.fragment;

import static android.app.Activity.RESULT_OK;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth
        .GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.Rss.RssReader;
import com.gunjun.android.personalproject.api.InstagramApp;
import com.gunjun.android.personalproject.api.InstagramMediaFile;
import com.gunjun.android.personalproject.api.InstagramSession;
import com.gunjun.android.personalproject.api.WeatherParser;
import com.gunjun.android.personalproject.api.WeatherHttpClient;
import com.gunjun.android.personalproject.models.RssFeed;
import com.gunjun.android.personalproject.models.RssItem;
import com.gunjun.android.personalproject.models.Step;
import com.gunjun.android.personalproject.models.Weather;
import com.gunjun.android.personalproject.service.ShakeService;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class DashBoardFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                todayMedia.setText(instagramSession.getMedia());
                follow.setText(instagramSession.getFollow());
                follower.setText(instagramSession.getFollower());
                instagramInfo.setVisibility(View.VISIBLE);
                instagramWarm.setVisibility(View.INVISIBLE);
            } else if (msg.what == InstagramApp.WHAT_ERROR) {
                instagramInfo.setVisibility(View.INVISIBLE);
                instagramWarm.setVisibility(View.VISIBLE);
            }
            return false;
        }
    });

    private GoogleAccountCredential credential;
    private Realm realm;
    private DateFormat format;
    private AsyncTask<Void, Void, Void> mTask;
    private InstagramMediaFile instagramMediaFile;
    private InstagramSession instagramSession;
    private Context context;

    @BindView(R.id.news_image_one)
    protected ImageView newsImageOne;

    @BindView(R.id.news_image_two)
    protected ImageView newsImageTwo;

    @BindView(R.id.news_title_one)
    protected TextView newsTitleOne;

    @BindView(R.id.news_title_two)
    protected TextView newsTitleTwo;

    @BindView(R.id.step_number)
    protected TextView stepNumber;

    @BindView(R.id.calendar_list)
    protected TextView calendarList;

    @BindView(R.id.today_media)
    protected TextView todayMedia;

    @BindView(R.id.follow)
    protected TextView follow;

    @BindView(R.id.follower)
    protected TextView follower;

    @BindView(R.id.instagram_info)
    protected LinearLayout instagramInfo;

    @BindView(R.id.instagram_warm)
    protected LinearLayout instagramWarm;

    @BindView(R.id.icon_weather)
    protected ImageView iconWeather;

    @BindView(R.id.text_weather)
    protected TextView textWeather;

    @BindView(R.id.dashboard_progress_bar)
    protected ProgressBar progressBar;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();

        instagramMediaFile = new InstagramMediaFile();
        instagramSession = new InstagramSession(this.getActivity());

        Intent service = new Intent(this.getActivity(), ShakeService.class);
        this.getActivity().startService(service);
        context = this.getActivity();
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{"Chamsil,KR"});

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dash_board, container, false);
        ButterKnife.bind(this, view);

        displaySteps();
        instagramMediaFile.getUserInfo(handler, instagramSession);

        try {
            rssCall();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void displaySteps() {
        Date date = new Date();
        format = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Step step = realm.where(Step.class).equalTo("today",format.format(date)).findFirst();
        if (step == null) {
            stepNumber.setText("0");
        } else {
            stepNumber.setText(Integer.toString(step.getStep()));
        }

    }

    public void rssCall() throws IOException, SAXException {

        mTask = new AsyncTask<Void, Void, Void>() {

            private ArrayList<RssItem> rssItems;

            @Override
            protected Void doInBackground(Void... params) {
                URL url = null;
                try {
                    url = new URL("http://www.chosun.com/site/data/rss/rss.xml");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                RssFeed feed = null;

                try {
                    feed = RssReader.read(url);
                    rssItems = feed.getRssItems();

                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    for(int i=0;i<2;i++) {
                        Document doc = Jsoup.connect(rssItems.get(i).getLink()).get();
                        Elements items = doc.select("meta");
                        for(int j=0;j<items.size();j++){
                            if(items.get(j).attr("property").equals("og:image")) {
                                rssItems.get(i).setImageUrl(items.get(j).attr("content") );
                            }
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Glide.with(context).load(rssItems.get(0).getImageUrl())
                        .into(new ViewTarget<ImageView, GlideDrawable>(newsImageOne) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {

                        // Set your resource on myView and/or start your animation here.
                        newsImageOne.setBackground(resource);
                    }
                });
                Glide.with(context).load(rssItems.get(1).getImageUrl())
                        .into(new ViewTarget<ImageView, GlideDrawable>(newsImageTwo) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {

                                // Set your resource on myView and/or start your animation here.
                                newsImageTwo.setBackground(resource);
                            }
                        });
                newsTitleOne.setText(rssItems.get(0).getTitle());
                newsTitleOne.bringToFront();
                newsTitleTwo.setText(rssItems.get(1).getTitle());
                newsTitleTwo.bringToFront();

            }
        };

        mTask.execute();
    }

    /*public String splitDetail(String s) {
        s = s.replaceAll("&nbsp", "");
        s = s.replaceAll(System.getProperty("line.separator"), "");
        return s.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                this.getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            new MakeRequestTask(credential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this.getActivity(), android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = this.getActivity().getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this.getActivity());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this.getActivity());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this.getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar service = null;
        private Exception lastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                lastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (output == null || output.size() == 0) {
                calendarList.setText("No results returned.");
            } else {
                calendarList.setText(TextUtils.join("\n", output));
            }
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            if (lastError != null) {
                if (lastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) lastError)
                                    .getConnectionStatusCode());
                } else if (lastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) lastError).getIntent(),
                            DashBoardFragment.REQUEST_AUTHORIZATION);
                } else {
                    calendarList.setText("The following error occurred:\n"
                            + lastError.getMessage());
                }
            } else {
                calendarList.setText("Request cancelled.");
            }
        }


        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = service.events().list("primary")
                    .setMaxResults(5)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }

                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            return eventStrings;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        displaySteps();
        instagramMediaFile.getUserInfo(handler, instagramSession);
    }


    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = WeatherParser.getWeather(data);

                // Let's retrieve the icon
                weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;

        }




        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                iconWeather.setImageBitmap(img);
            }
            textWeather.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "℃");
        }

    }

}
