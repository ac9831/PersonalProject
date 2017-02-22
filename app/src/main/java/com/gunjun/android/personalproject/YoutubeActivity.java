package com.gunjun.android.personalproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.gunjun.android.personalproject.adapter.YoutubeAdapter;
import com.gunjun.android.personalproject.api.YoutubeApi;
import com.gunjun.android.personalproject.interfaces.YouTubeVideosReceiver;
import com.gunjun.android.personalproject.models.YouTubeVideo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class YoutubeActivity extends AppCompatActivity implements YouTubeVideosReceiver {

    private YoutubeApi youtubeApi;
    private Realm realm;
    private Handler handler;
    private ArrayList<YouTubeVideo> scrollResultsList;
    private ArrayList<YouTubeVideo> searchResultsList;
    private RecyclerView.LayoutManager layoutManager;
    private int onScrollIndex = 0;
    private YoutubeAdapter youtubeAdapter;


    @BindView(R.id.youtube_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.youtube_list)
    protected RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Personal");
        realm = Realm.getDefaultInstance();
        youtubeAdapter = new YoutubeAdapter(searchResultsList, this);
        youtubeApi = new YoutubeApi(this, realm);
        handler = new Handler();
        searchResultsList = new ArrayList<>();
        scrollResultsList = new ArrayList<>();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(youtubeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        youtubeApi = new YoutubeApi(this, realm);
        youtubeApi.setYouTubeVideosReceiver(this);
        youtubeApi.searchVideos();
    }

    @Override
    public void onVideosReceived(ArrayList<YouTubeVideo> youTubeVideos) {
        searchResultsList.clear();
        scrollResultsList.clear();
        scrollResultsList.addAll(youTubeVideos);
        addMoreData();
    }

    private void addMoreData() {

        List<YouTubeVideo> subList;
        if (scrollResultsList.size() < (onScrollIndex + 10)) {
            subList = scrollResultsList.subList(onScrollIndex, scrollResultsList.size());
            onScrollIndex += (scrollResultsList.size() % 10);
        } else {
            subList = scrollResultsList.subList(onScrollIndex, onScrollIndex + 10);
            onScrollIndex += 10;
        }

        if (!subList.isEmpty()) {
            searchResultsList.addAll(subList);
            handler.post(new Runnable() {
                public void run() {
                    if (youtubeAdapter != null) {
                        Log.d("하이","하이루");
                        youtubeAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onPlaylistNotFound(String playlistId, int errorCode) {

    }
}
