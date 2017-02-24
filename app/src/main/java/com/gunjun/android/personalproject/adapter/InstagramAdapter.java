package com.gunjun.android.personalproject.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.viewholder.InstagramViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gunjunLee on 2017-02-23.
 */

public class InstagramAdapter extends RecyclerView.Adapter<InstagramViewHolder>  {

    private Activity context;
    private List<String> instagramUrl;
    private int size;


    public InstagramAdapter(List<String> instagramUrl, Activity context) {
        this.instagramUrl = instagramUrl;
        this.context = context;
        this.size = instagramUrl.size();
    }

    public void setInstagramUrl(ArrayList<String> instagramUrl) {
        this.instagramUrl = instagramUrl;
        this.size = instagramUrl.size();
    }

    @Override
    public InstagramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.instagram_list, parent, false);
        InstagramViewHolder holder = new InstagramViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(InstagramViewHolder holder, int position) {
        int index = 0;
        if(position > 0) {
            index = 3 * position;
        } else {
            index = 0;
        }
        if(index < instagramUrl.size()) {
            Glide.with(context).load(instagramUrl.get(index)).into(holder.imageOne);
        }

        if(index + 1 < instagramUrl.size()) {
            Glide.with(context).load(instagramUrl.get(index+1)).into(holder.imageTwo);
        }

        if(index + 2 < instagramUrl.size()) {
            Glide.with(context).load(instagramUrl.get(index+2)).into(holder.imageThree);
        }
    }

    @Override
    public int getItemCount() {
        if (size > 0) {
            return size/3 + 1;
        } else {
            return 0;
        }
    }
}
