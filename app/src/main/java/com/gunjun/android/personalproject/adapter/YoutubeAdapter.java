package com.gunjun.android.personalproject.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.viewholder.YoutubeViewHolder;
import com.gunjun.android.personalproject.models.YouTubeVideo;

import java.util.List;

/**
 * Created by gunjunLee on 2017-02-21.
 */

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeViewHolder>  {

    private List<YouTubeVideo> youTubeVideoList;
    private Activity context;

    public YoutubeAdapter(List<YouTubeVideo> youTubeVideos, Activity context) {
        youTubeVideoList = youTubeVideos;
        this.context = context;
    }

    @Override
    public YoutubeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_list, parent, false);
        YoutubeViewHolder holder = new YoutubeViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(YoutubeViewHolder holder, int position) {
        final YouTubeVideo searchResult = youTubeVideoList.get(position);
        holder.videosNumber.setText(searchResult.getViewCount());
        Glide.with(context).load(searchResult.getThumbnailURL()).into(holder.videoThumbnail);
        holder.playlistTitle.setText(searchResult.getTitle());
    }

    @Override
    public int getItemCount() {
        return youTubeVideoList.size();
    }
}
