package com.example.fallingalerter.api;

import android.hardware.Sensor;

import java.util.List;

/**
 * Created by bbuescu on 7/11/2016.
 */
public interface ISensorManagerHelper {
    public List<Sensor> getSensorList();
    public Boolean isSensorAvailable(int sensorType);
    public Boolean wakeSensor(int sensorType);
    public Sensor getSensorByType(int sensorType);
    public void registerListeners(Sensor sensor, int samplePeriodTime);
}
