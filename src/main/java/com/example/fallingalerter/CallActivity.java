package com.example.fallingalerter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.example.fallingalerter.commons.ToastHelper;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

/**
 * Created by bbuescu on 7/13/2016.
 */
public class CallActivity extends Activity {
    private static final int PERMISSION_TO_USE_PHONE = 1;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private ToastHelper toastHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        toastHelper = new ToastHelper(this);
    }

    @Override
    protected void onStart() {
        new AlertDialog.Builder(CallActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.call)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SensorService.setFallNotificationOn(Boolean.FALSE);
                        initiateCall();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SensorService.setFallNotificationOn(Boolean.FALSE);
                        finish();
                    }
                }).show();

        super.onStart();
    }

    private void initiateCall() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSION_TO_USE_PHONE);
            return;
        }
        makeCall();
    }

    @SuppressWarnings("MissingPermission")
    private void makeCall() {
        String phoneNumber = "tel:" + settingsReaderWriterHelper.getPhoneNumber();
        Intent myIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber));
        startActivity(myIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_TO_USE_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();

                } else {
                    toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                }
                break;
            }
        }
    }
}
