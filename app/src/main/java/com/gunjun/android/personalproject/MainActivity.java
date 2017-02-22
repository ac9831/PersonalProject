package com.gunjun.android.personalproject;

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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.gunjun.android.personalproject.Rss.RssReader;
import com.gunjun.android.personalproject.api.InstagramApp;
import com.gunjun.android.personalproject.api.InstagramMediaFile;
import com.gunjun.android.personalproject.api.InstagramSession;
import com.gunjun.android.personalproject.behavior.BottomNavigationViewHelper;
import com.gunjun.android.personalproject.models.Profile;
import com.gunjun.android.personalproject.models.RssFeed;
import com.gunjun.android.personalproject.models.RssItem;
import com.gunjun.android.personalproject.models.Step;
import com.gunjun.android.personalproject.service.ShakeService;

import org.xml.sax.SAXException;

import java.io.File;
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
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

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

    private MenuItem prevBottomNavigation;

    @BindView(R.id.bottom_navigation)
    protected BottomNavigationView bottomNavigationView;

    @BindView(R.id.fab)
    protected CircleImageView floatingActionButton;

    @BindView(R.id.news_detail_one)
    protected TextView newsDetailOne;

    @BindView(R.id.news_detail_two)
    protected TextView newsDetailTwo;

    @BindView(R.id.news_title_one)
    protected TextView newsTitleOne;

    @BindView(R.id.news_title_two)
    protected TextView newsTitleTwo;

    @BindView(R.id.step_number)
    protected TextView stepNumber;

    @BindView(R.id.calendar_list)
    protected TextView calendarList;

    @BindView(R.id.main_toolbar)
    protected Toolbar toolbar;

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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        instagramMediaFile = new InstagramMediaFile();
        instagramSession = new InstagramSession(this);
        bottomSetting();
        List<Profile> query = realm.where(Profile.class).findAll();
        if(query.size() < 1) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else {
            File imgFile = new  File(query.get(0).getImgPath());
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                floatingActionButton.setImageBitmap(myBitmap);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Personal");

        displaySteps();
        instagramMediaFile.getAllMediaImages(handler, instagramSession);

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
        Intent service = new Intent(this, ShakeService.class);
        startService(service);
    }

    public void bottomSetting() {
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.findViewById(floatingActionButton.getId());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        return true;
                    case R.id.action_rss:
                        return true;
                    case R.id.action_video:
                        startActivity(new Intent(context, YoutubeActivity.class));
                    case R.id.action_sns:
                        return true;
                }
                return false;
            }


        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        stopService(new Intent(this, ShakeService.class));
    }

    public void rssCall() throws IOException, SAXException{

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

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                newsDetailOne.setText(splitDetail(rssItems.get(0).getDescription()));
                newsDetailTwo.setText(splitDetail(rssItems.get(1).getDescription()));
                newsTitleOne.setText(rssItems.get(0).getTitle());
                newsTitleTwo.setText(rssItems.get(1).getTitle());

            }
        };

        mTask.execute();
    }

    public String splitDetail(String s) {
        s = s.replaceAll("&nbsp", "");
        s = s.replaceAll(System.getProperty("line.separator"), "");
        return s.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

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
                this, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                                getPreferences(Context.MODE_PRIVATE);
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

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    calendarList.setText("The following error occurred:\n"
                            + lastError.getMessage());
                }
            } else {
                calendarList.setText("Request cancelled.");
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        displaySteps();
        instagramMediaFile.getAllMediaImages(handler, instagramSession);
    }
}
