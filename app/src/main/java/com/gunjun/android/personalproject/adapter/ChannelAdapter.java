package com.gunjun.android.personalproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.viewholder.ChannelViewHolder;
import com.gunjun.android.personalproject.models.Youtube;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by gunjunLee on 2017-02-21.
 */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelViewHolder> {

    private Realm realm;
    private Context context;
    private RealmResults<Youtube> items;


    public ChannelAdapter(RealmResults<Youtube> items, Context mContext, Realm realm) {
        this.items = items;
        this.context = mContext;
        this.realm = realm;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_channel_list, parent, false);
        ChannelViewHolder holder = new ChannelViewHolder(v, realm, this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        holder.channelText.setText(items.get(position).getChannelName());
        holder.channelId.setText(items.get(position).getId()+"");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
