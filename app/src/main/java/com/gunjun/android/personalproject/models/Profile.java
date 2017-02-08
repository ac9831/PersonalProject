package com.gunjun.android.personalproject.models;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gunjunLee on 2017-02-07.
 */

public class Profile extends RealmObject {

    @PrimaryKey
    private int id;
    private float weight;
    private float height;
    private int age;
    private float goalWeight;
    private int goalStep;
    private RealmList<Step> step;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(float goalWeight) {
        this.goalWeight = goalWeight;
    }

    public int getGoalStep() {
        return goalStep;
    }

    public void setGoalStep(int goalStep) {
        this.goalStep = goalStep;
    }

    public RealmList<Step> getStep() {
        return step;
    }

    public void setStep(RealmList<Step> step) {
        this.step = step;
    }
}
