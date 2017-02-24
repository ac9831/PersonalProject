package com.gunjun.android.personalproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.YoutubeAdapter;
import com.gunjun.android.personalproject.api.YoutubeApi;
import com.gunjun.android.personalproject.interfaces.YouTubeVideosReceiver;
import com.gunjun.android.personalproject.listener.EndlessRecyclerViewScrollListener;
import com.gunjun.android.personalproject.models.YouTubeVideo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class YoutubeFragment extends Fragment implements YouTubeVideosReceiver {

    private YoutubeApi youtubeApi;
    private Realm realm;
    private Handler handler;
    private ArrayList<YouTubeVideo> scrollResultsList;
    private ArrayList<YouTubeVideo> searchResultsList;
    private RecyclerView.LayoutManager layoutManager;
    private int onScrollIndex = 0;
    private YoutubeAdapter youtubeAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    @BindView(R.id.youtube_list)
    protected RecyclerView recyclerView;

    @BindView(R.id.youtube_progress_bar)
    protected ProgressBar progressBar;

    public YoutubeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        layoutManager = new LinearLayoutManager(this.getActivity());
        youtubeAdapter = new YoutubeAdapter(searchResultsList, this.getActivity());
        youtubeApi = new YoutubeApi(this.getActivity(), realm);
        handler = new Handler();
        searchResultsList = new ArrayList<>();
        scrollResultsList = new ArrayList<>();
        onScrollIndex = 0;
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                addMoreData();
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        youtubeApi = new YoutubeApi(this.getActivity(), realm);
        youtubeApi.setYouTubeVideosReceiver(this);
        youtubeApi.searchVideos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(youtubeAdapter);
        recyclerView.addOnScrollListener(scrollListener);
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
                        youtubeAdapter.setYouTubeVideoList(searchResultsList);
                        youtubeAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onPlaylistNotFound(String playlistId, int errorCode) {

    }
}
