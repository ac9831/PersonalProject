package com.gunjun.android.personalproject.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.adapter.ChannelAdapter;
import com.gunjun.android.personalproject.models.Youtube;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by gunjunLee on 2017-02-21.
 */

public class ChannelViewHolder extends RecyclerView.ViewHolder {

    private Realm realm;
    private ChannelAdapter channelAdapter;

    @BindView(R.id.channel_text)
    public TextView channelText;

    @BindView(R.id.channel_id)
    public TextView channelId;

    public ChannelViewHolder(View itemView, Realm realm, ChannelAdapter channelAdapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.realm = realm;
        this.channelAdapter = channelAdapter;
    }

    @OnClick(R.id.channel_del)
    public void onClick(View v) {
        realm.beginTransaction();
        realm.where(Youtube.class).equalTo("id", Integer.parseInt(channelId.getText().toString())).findFirst().deleteFromRealm();
        realm.commitTransaction();
        channelAdapter.notifyDataSetChanged();
    }
}
