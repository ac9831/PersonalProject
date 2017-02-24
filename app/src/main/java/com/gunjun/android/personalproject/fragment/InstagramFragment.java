package com.gunjun.android.personalproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.InstagramAdapter;
import com.gunjun.android.personalproject.api.InstagramMediaFile;
import com.gunjun.android.personalproject.api.InstagramSession;
import com.gunjun.android.personalproject.interfaces.InstagramReceiver;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class InstagramFragment extends Fragment implements InstagramReceiver {

    private InstagramAdapter instagramAdapter;
    private ArrayList<String> instagramUrl;
    private InstagramMediaFile instagramMediaFile;
    private InstagramSession instagramSession;
    private RecyclerView.LayoutManager layoutManager;
    private Handler handler;

    @BindView(R.id.instagram_list)
    protected RecyclerView recyclerView;

    public InstagramFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new LinearLayoutManager(this.getActivity());
        instagramSession = new InstagramSession(this.getActivity());
        instagramUrl = new ArrayList<String>();
        instagramAdapter = new InstagramAdapter(instagramUrl, this.getActivity());
        handler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();
        instagramMediaFile = new InstagramMediaFile();
        instagramMediaFile.setInstagramReceiver(this);
        instagramMediaFile.getAllMediaImages(handler, instagramSession);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instagram, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(instagramAdapter);
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
    public void onInstagramImageReceived(ArrayList<String> instagramList) {
        instagramUrl.clear();
        instagramUrl = instagramList;
        addData();
    }

    public void addData() {
        Log.d("aaa",instagramUrl.get(0));

        handler.post(new Runnable() {
            public void run() {
                if(instagramAdapter != null) {
                    instagramAdapter.setInstagramUrl(instagramUrl);
                    instagramAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
