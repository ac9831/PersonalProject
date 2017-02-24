package com.gunjun.android.personalproject.interfaces;

import com.gunjun.android.personalproject.models.RssItem;

import java.util.ArrayList;

/**
 * Created by gunjunLee on 2017-02-24.
 */

public interface RssReceiver {
    void rssDataReceiver(ArrayList<RssItem> rssItems);
}
