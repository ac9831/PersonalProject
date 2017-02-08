package com.gunjun.android.personalproject.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by gunjunLee on 2017-02-07.
 */

public class Step extends RealmObject {

    private int step;
    private Date today;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }
}
