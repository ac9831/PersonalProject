package com.gunjun.android.personalproject.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gunjun.android.personalproject.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class YoutubeViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.video_thumbnail)
    public ImageView videoThumbnail;

    @BindView(R.id.playlist_title)
    public TextView playlistTitle;

    @BindView(R.id.videos_number)
    public TextView videosNumber;

    public YoutubeViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
