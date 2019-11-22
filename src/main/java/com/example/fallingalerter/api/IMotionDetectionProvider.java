package com.example.fallingalerter.api;

import com.example.fallingalerter.commons.MotionStateEnum;

import java.util.Stack;

/**
 * Created by bbuescu on 7/13/2016.
 */
public interface IMotionDetectionProvider {
    public MotionStateEnum getSystemState();

    public int computeZeroCrossingRate(Stack<Double> samplingData);

    public void addData(double xAxys, double yAxys, double zAxys);

    public void detectFall();

    public MotionStateEnum computeSystemState(Stack<Double> sampleData, double yAxys);
}
