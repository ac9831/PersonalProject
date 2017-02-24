package com.gunjun.android.personalproject.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gunjun.android.personalproject.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RssViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title_one)
    public TextView titleOne;

    @BindView(R.id.title_two)
    public TextView titleTwo;

    @BindView(R.id.image_one)
    public ImageView imageOne;

    @BindView(R.id.image_two)
    public ImageView imageTwo;

    public RssViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
