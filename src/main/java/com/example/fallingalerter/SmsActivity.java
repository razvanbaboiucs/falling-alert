package com.example.fallingalerter;

import android.*;
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
import android.util.Log;

import com.example.fallingalerter.commons.ToastHelper;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

/**
 * Created by bbuescu on 7/13/2016.
 */
public class SmsActivity extends Activity {
    private static final int PERMISSION_TO_SEND_SMS = 1;
    private String _phoneNumber;
    private String _message;
    public static String MESS_TEXT = "MESSAGE_TEXT";
    ToastHelper toastHelper;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        Intent intent = getIntent();
        _phoneNumber = settingsReaderWriterHelper.getPhoneNumber();
        _message = intent.getExtras() != null ? intent.getExtras().getString(MESS_TEXT) : getResources().getString(R.string.SMS_NoLocation);
        toastHelper = new ToastHelper(this);
    }

    @Override
    protected void onStart() {
        new AlertDialog.Builder(SmsActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.sms)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS();
                        SensorService.setFallNotificationOn(Boolean.FALSE);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SensorService.setFallNotificationOn(false);
                        finish();
                    }
                }).show();

        super.onStart();
    }

    private void sendSMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_TO_SEND_SMS);
            return;
        }
        doSendSms();
    }

    private void doSendSms() {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(_phoneNumber, null, _message, null, null);
        toastHelper.showLongMessage(getResources().getString(R.string.smsSent));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_TO_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSendSms();
                } else {
                    toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                }
                break;
            }

        }
    }
}
