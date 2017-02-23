package com.gunjun.android.personalproject.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.gunjun.android.personalproject.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by gunjunLee on 2017-02-23.
 */

public class InstagramViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.instagram_image_one)
    public ImageView imageOne;

    @BindView(R.id.instagram_image_two)
    public ImageView imageTwo;

    @BindView(R.id.instagram_image_three)
    public ImageView imageThree;

    public InstagramViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
