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
import android.telephony.SmsManager;

import com.example.fallingalerter.commons.ToastHelper;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

/**
 * Created by bbuescu on 7/13/2016.
 */
public class BothActionsActivity extends Activity {
    private static final int PERMISSION_TO_USE_PHONE_AND_SMS = 1;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private String _message;
    private ToastHelper toastHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        toastHelper = new ToastHelper(this);
        Intent intent = getIntent();
        _message = intent.getExtras() != null ? intent.getExtras().getString(SmsActivity.MESS_TEXT) : getResources().getString(R.string.SMS_NoLocation);
    }

    @Override
    protected void onStart() {

        new AlertDialog.Builder(BothActionsActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.both)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executeActions();
                        SensorService.setFallNotificationOn(Boolean.FALSE);
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

    private void executeActions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS},
                    PERMISSION_TO_USE_PHONE_AND_SMS);
            return;
        }
        sendSms();
        callPhone();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_TO_USE_PHONE_AND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        callPhone();
                    } else {
                        toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        sendSms();
                    } else {
                        toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                    }

                } else {
                    toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                }
                break;
            }

        }
    }

    @SuppressWarnings("MissingPermission")
    private void callPhone() {
        String phNum = "tel:" + settingsReaderWriterHelper.getPhoneNumber();
        Intent myIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phNum));
        startActivity(myIntent);
    }

    @SuppressWarnings("MissingPermission")
    private void sendSms() {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(settingsReaderWriterHelper.getPhoneNumber(), null, _message, null, null);
        toastHelper.showLongMessage(getResources().getString(R.string.smsSent));
    }
}
