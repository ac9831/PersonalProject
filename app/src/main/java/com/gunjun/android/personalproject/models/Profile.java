package com.gunjun.android.personalproject.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gunjunLee on 2017-02-07.
 */

public class Profile extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private float weight;
    private float height;
    private int age;
    private int goalStep;
    private String imgPath;
    private RealmList<Step> step;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getGoalStep() {
        return goalStep;
    }

    public void setGoalStep(int goalStep) {
        this.goalStep = goalStep;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public RealmList<Step> getStep() {
        return step;
    }

    public void setStep(RealmList<Step> step) {
        this.step = step;
    }
}
