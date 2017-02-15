package com.gunjun.android.personalproject.models;

import io.realm.RealmObject;

/**
 * Created by gunjunLee on 2017-02-07.
 */

public class Step extends RealmObject {

    private int step;
    private String today;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }
}
