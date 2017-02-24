package com.gunjun.android.personalproject.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gunjunLee on 2017-02-21.
 */

public class Youtube extends RealmObject {

    @PrimaryKey
    private int id;
    private String channelName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
