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
import com.gunjun.android.personalproject.adapter.RssAdapter;
import com.gunjun.android.personalproject.api.RssApi;
import com.gunjun.android.personalproject.interfaces.RssReceiver;
import com.gunjun.android.personalproject.listener.EndlessRecyclerViewScrollListener;
import com.gunjun.android.personalproject.models.RssItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RssFragment extends Fragment implements RssReceiver{

    private ArrayList<RssItem> rssItems;
    private ArrayList<RssItem> scrollResultsList;
    private Handler handler;
    private RssAdapter rssAdapter;
    private RssApi rssApi;
    private int onScrollIndex = 0;
    private RecyclerView.LayoutManager layoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;


    @BindView(R.id.rss_list)
    protected RecyclerView recyclerView;

    @BindView(R.id.rss_progress_bar)
    protected ProgressBar loadingProgressBar;

    public RssFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        layoutManager = new LinearLayoutManager(this.getActivity());
        rssAdapter = new RssAdapter(this.getActivity());
        scrollResultsList = new ArrayList<>();
        rssItems = new ArrayList<>();
        onScrollIndex = 0;
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                addMoreData();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rss, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rssAdapter);
        recyclerView.addOnScrollListener(scrollListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        rssApi = new RssApi();
        rssApi.setRssReceiver(this);
        rssApi.rssCall();
        loadingProgressBar.setVisibility(View.VISIBLE);
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
    public void rssDataReceiver(ArrayList<RssItem> rssItems) {
        this.rssItems.clear();
        scrollResultsList.clear();
        scrollResultsList.addAll(rssItems);
        handler.post(new Runnable() {
            public void run() {
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
        addMoreData();
    }

    private void addMoreData() {
        List<RssItem> subList;
        if (scrollResultsList.size() < (onScrollIndex + 10)) {
            subList = scrollResultsList.subList(onScrollIndex, scrollResultsList.size());
            onScrollIndex += (scrollResultsList.size() % 10);
        } else {
            subList = scrollResultsList.subList(onScrollIndex, onScrollIndex + 10);
            onScrollIndex += 10;
        }

        if (!subList.isEmpty()) {
            rssItems.addAll(subList);
            handler.post(new Runnable() {
                public void run() {
                    if (rssAdapter != null) {
                        rssAdapter.setRssList(rssItems);
                        rssAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
