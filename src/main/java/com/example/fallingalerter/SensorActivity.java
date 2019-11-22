package com.example.fallingalerter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.fallingalerter.commons.SensorManagerHelperImpl;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

   private SensorManagerHelperImpl sensorManagerHelper;
    float maxx=0,maxy=0,maxz=0;
    TextView axaX, axaY, axaZ,maxValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sensorManagerHelper = new SensorManagerHelperImpl(this);
        sensorManagerHelper.registerListeners(
                sensorManagerHelper.getSensorByType(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        setupUI();
    }

    private void setupUI() {
        axaX=(TextView) findViewById(R.id.axaX);
        axaY=(TextView) findViewById(R.id.axaY);
        axaZ=(TextView) findViewById(R.id.axaZ);
        maxValues=(TextView) findViewById(R.id.maxValues);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            if (x > maxx) maxx = x;
            if (y > maxy) maxy = y;
            if (z > maxz) maxz = z;
            printSensorValues(x,y,z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void printSensorValues(float x,float y, float z) {
        axaX.setText(getResources().getString(R.string.axaX,x));
        axaY.setText(getResources().getString(R.string.axaY,y));
        axaZ.setText(getResources().getString(R.string.axaZ,z));
        maxValues.setText(getResources().getString(R.string.maxValues,maxx,maxy,maxz));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
