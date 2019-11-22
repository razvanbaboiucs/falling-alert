package com.example.fallingalerter.commons;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import com.example.fallingalerter.api.ISensorManagerHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 7/11/2016.
 */
public class SensorManagerHelperImpl implements ISensorManagerHelper {
    private Context context;

    SensorManager mSensorManager;
    Sensor sensor;

    public SensorManagerHelperImpl(Context context) {
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public List<Sensor> getSensorList() {
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        return deviceSensors;
    }

    @Override
    public Boolean isSensorAvailable(int sensorType) {
        return getSensorByType(sensorType) != null;
    }

    @Override
    public Boolean wakeSensor(int sensorType) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return getSensorByType(sensorType).isWakeUpSensor();
        }
        return isSensorAvailable(sensorType);
    }

    @Override
    public Sensor getSensorByType(int sensorType) {
        return mSensorManager.getDefaultSensor(sensorType);
    }


    @Override
    public void registerListeners(Sensor sensor, int samplingPeriodUs) {
        mSensorManager.registerListener((SensorEventListener) context, sensor, samplingPeriodUs);
    }

}
