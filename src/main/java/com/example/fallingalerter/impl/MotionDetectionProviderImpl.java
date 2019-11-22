package com.example.fallingalerter.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.fallingalerter.api.IMotionDetectionProvider;
import com.example.fallingalerter.commons.MotionStateEnum;

import java.util.Stack;

/**
 * Created by bbuescu on 7/13/2016.
 */
public class MotionDetectionProviderImpl implements IMotionDetectionProvider, SensorEventListener {
    private static MotionStateEnum FINAL_STATE = MotionStateEnum.STATE_UNKNOWN;
    private static MotionStateEnum PREVIEW_STATE = MotionStateEnum.STATE_UNKNOWN;
    private static MotionStateEnum CURRENT_STATE = MotionStateEnum.STATE_UNKNOWN;
    private MotionStateEnum systemState;
    private static int BUFF_SIZE = 50;
    public static int FALLING_THRESHOLD = 10;
    private Stack<Double> sampleData;
    private double sigmaFilter = 0.7, thresholdHigh = 10, thresholdLow = 5, thresholdMiddle = 2;
    private SensorManager sensorManager;
    SettingsReaderWriterHelperImpl settingsReaderWriterHelper;

    public MotionDetectionProviderImpl(Context context) {
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(context);
        sampleData = new Stack<>();
        FALLING_THRESHOLD += settingsReaderWriterHelper.readIntValue(SettingsReaderWriterHelperImpl.SENSOR_SENSIBILITY);
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public MotionStateEnum getSystemState() {
        return FINAL_STATE;
    }

    @Override
    public int computeZeroCrossingRate(Stack<Double> samplingData) {
        int count = 0;
        for (int i = 1; i <= samplingData.size() - 1; i++) {
            if ((samplingData.get(i) - thresholdHigh) < sigmaFilter && (samplingData.get(i - 1) - thresholdHigh) > sigmaFilter)
                count++;
        }
        Log.i("ZeroCrossingRate", "Val: "+count);
        return count;
    }

    @Override
    public void addData(double xAxis, double yAxis, double zAxis) {
        double accelerationVector = Math.sqrt(xAxis * xAxis + yAxis * yAxis + zAxis * zAxis);
        if (sampleData.size() >= BUFF_SIZE) {
            sampleData.remove(0);
        }
        sampleData.push(accelerationVector);
        //Log.i("Acceleration", "Value: "+accelerationVector + " stack size: " + sampleData.size());
    }

    @Override
    public void detectFall() {
        int min = Integer.MAX_VALUE, max = 0, maxIndex = 0, minIndex = 0;

        for (int i = 1; i < sampleData.size() - 1; i++) {
            if (sampleData.get(i) - sampleData.get(i - 1) > max) {
                max = (int) (sampleData.get(i) - sampleData.get(i - 1));
                maxIndex = i;
            }
            if (sampleData.get(i) - sampleData.get(i - 1) < min) {
                min = (int) (sampleData.get(i) - sampleData.get(i - 1));
                minIndex = i;
            }
        }
        if (Math.abs(maxIndex - minIndex) == 1) {
            if (max - min > FALLING_THRESHOLD)
                FINAL_STATE = MotionStateEnum.STATE_FALL;
            //Log.e("DetectFall", "Value max-min: " + (max - min) + " IndexVal: " + (maxIndex - minIndex));
        }
    }

    @Override
    public MotionStateEnum computeSystemState(Stack<Double> sampleData, double yAxys) {
        int zeroCrossingRate = computeZeroCrossingRate(sampleData);
        if (zeroCrossingRate == 0) {
            // Log.e("AY2", "Val: "+ Math.abs(ay2));
            if (Math.abs(yAxys) < thresholdLow) {
                CURRENT_STATE = MotionStateEnum.STATE_SITTING;
            } else {
                CURRENT_STATE = MotionStateEnum.STATE_STANDING;
            }

        } else if (zeroCrossingRate > thresholdMiddle) {
            CURRENT_STATE = MotionStateEnum.STATE_WALKING;
        } else {
            CURRENT_STATE = MotionStateEnum.STATE_NORMAL;
        }
        return CURRENT_STATE;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            addData(ax, ay, az);
            systemState = computeSystemState(sampleData, ay);
            detectFall();
            setFinalSystemState(CURRENT_STATE, PREVIEW_STATE);
            if (!PREVIEW_STATE.equals(CURRENT_STATE)) {
                PREVIEW_STATE = CURRENT_STATE;
            }
        }
    }

    private void setFinalSystemState(MotionStateEnum currentState, MotionStateEnum previewState) {
        if (!previewState.equals(currentState)) {
            if (MotionStateEnum.STATE_FALL.equals(systemState) && (MotionStateEnum.STATE_SITTING.equals(CURRENT_STATE) || MotionStateEnum.STATE_NORMAL.equals(CURRENT_STATE))) {
                FINAL_STATE = MotionStateEnum.STATE_FALL;
                //Log.e(FALLING_STATE, FALLING_STATE);
            } else
            if (MotionStateEnum.STATE_SITTING.equals(currentState)) {
                FINAL_STATE = MotionStateEnum.STATE_SITTING;
                // Log.e(SITTING_STATE, SITTING_STATE);
            } else
            if (currentState.equals(MotionStateEnum.STATE_STANDING)) {
                FINAL_STATE = MotionStateEnum.STATE_STANDING;
                // Log.e(STANDING_STATE, STANDING_STATE);
            } else
            if (currentState.equals(MotionStateEnum.STATE_WALKING)) {
                //Log.e(WALKING_STATE, WALKING_STATE);
                FINAL_STATE = MotionStateEnum.STATE_WALKING;
            } else
                FINAL_STATE = MotionStateEnum.STATE_UNKNOWN;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stopDetection() {
        sensorManager.unregisterListener(this);
    }

    public static MotionStateEnum getFinalState() {
        return FINAL_STATE;
    }
}
