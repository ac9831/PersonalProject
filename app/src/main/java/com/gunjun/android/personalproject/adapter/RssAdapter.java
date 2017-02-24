package com.gunjun.android.personalproject.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.viewholder.RssViewHolder;
import com.gunjun.android.personalproject.models.RssItem;

import java.util.ArrayList;

public class RssAdapter extends RecyclerView.Adapter<RssViewHolder>  {

    private ArrayList<RssItem> rssItems;
    private Activity context;
    private int size;

    public void setRssList(ArrayList<RssItem> rssList) {
        rssItems = rssList;
        size = rssList.size();
    }

    public RssAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public RssViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_list, parent, false);
        RssViewHolder holder = new RssViewHolder(v);
        return holder;
    }

    // inner 함수 접근은 final 변수와 전역 변수로 가능하다.
    @Override
    public void onBindViewHolder(final RssViewHolder holder, int position) {
        int index = 0;
        if(position > 0) {
            index = 2 * position;
        } else {
            index = 0;
        }
        if(index < rssItems.size()) {
            Glide.with(context).load(rssItems.get(index).getImageUrl())
                    .into(new ViewTarget<ImageView, GlideDrawable>(holder.imageOne) {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {

                    // Set your resource on myView and/or start your animation here.
                    holder.imageOne.setBackground(resource);
                }
            });
            holder.titleOne.setText(rssItems.get(index).getTitle());
            holder.titleOne.bringToFront();
        }

        if(index + 1 < rssItems.size()) {
            Glide.with(context).load(rssItems.get(index+1).getImageUrl())
                    .into(new ViewTarget<ImageView, GlideDrawable>(holder.imageTwo) {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {

                    // Set your resource on myView and/or start your animation here.
                    holder.imageTwo.setBackground(resource);
                }
            });
            holder.titleTwo.setText(rssItems.get(index+1).getTitle());
            holder.titleTwo.bringToFront();
        }

    }

    @Override
    public int getItemCount() {
        if(size == 0) {
            return 0;
        } else {
            return size/2 + 1;
        }
    }
}
