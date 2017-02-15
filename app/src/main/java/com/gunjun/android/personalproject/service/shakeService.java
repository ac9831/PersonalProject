package com.gunjun.android.personalproject.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gunjun.android.personalproject.models.Step;

import java.text.DateFormat;
import java.util.Date;

import io.realm.Realm;

/**
 * Created by gunjunLee on 2017-02-13.
 */

public class ShakeService extends Service implements SensorEventListener {

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;
    private Realm realm;
    private int step;
    private DateFormat format;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    public class ShakeServiceBinder extends Binder {
        public ShakeService getService() {
            return ShakeService.this;
        }
    }

    private final IBinder binder = new ShakeServiceBinder();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        realm = Realm.getDefaultInstance();
        format = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Date date = new Date();

        format.format(date);
        Step query = realm.where(Step.class).equalTo("today",format.format(date)).findFirst();
        if(query == null) {
            step = 0;
        } else {
            step = query.getStep();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Date date = new Date();

        realm.beginTransaction();
        Step query = realm.where(Step.class).equalTo("today",format.format(date)).findFirst();

        if (query == null) {
            Step stepClass = realm.createObject(Step.class);
            stepClass.setToday(format.format(date));
            stepClass.setStep(step);
        } else {
            query.setStep(step);
        }
        realm.commitTransaction();
        realm.close();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = event.values[DATA_X];
                y = event.values[DATA_Y];
                z = event.values[DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 20000;

                if (speed > SHAKE_THRESHOLD) {
                    step++;
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
