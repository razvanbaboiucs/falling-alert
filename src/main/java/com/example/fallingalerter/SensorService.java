package com.example.fallingalerter;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.fallingalerter.commons.MotionStateEnum;
import com.example.fallingalerter.commons.SoundHelperImpl;
import com.example.fallingalerter.commons.ToastHelper;
import com.example.fallingalerter.impl.MotionDetectionProviderImpl;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int EMERGENCY_ACTION_DELAY = 60;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private final Handler taskHandler = new Handler();
    SoundHelperImpl soundHelper;
    private Runnable refreshTask;
    private static int REFRESH_DELAY = 2000;
    private Date startNotification;
    private ToastHelper toastHelper;
    public Date curentTime;
    public Date lastTimeSoundPlayed;
    private Location curentLocation;
    private GoogleApiClient mGoogleApiClient;
    MotionStateEnum serviceMotionState;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private static final int PERMISSION_TO_USE_GPS = 3;
    @SuppressLint("SimpleDateFormat")
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private MotionDetectionProviderImpl motionDetectionProvider;

    private static boolean isNotificationOn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        toastHelper = new ToastHelper(this);
        motionDetectionProvider = new MotionDetectionProviderImpl(this);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        soundHelper=new SoundHelperImpl(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        locationRequest = createLocationRequest();
        createTaskRunner();
        setupSounds();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        taskHandler.postDelayed(refreshTask, 500);
        toastHelper.showLongMessage(getResources().getString(R.string.serviceStarted));
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    private void setupSounds() {
        soundHelper = new SoundHelperImpl(this);
    }

    private void createTaskRunner() {
        if (refreshTask == null) {
            refreshTask = new Runnable() {
                @Override
                public void run() {
                    MotionStateEnum currentState = MotionDetectionProviderImpl.getFinalState();
                    if (!isNotificationOn) {
                        if (MotionStateEnum.STATE_FALL.equals(currentState)) {
                            setFallNotificationOn(Boolean.TRUE);
                            playSoundByState(currentState);
                            showFallNotification();
                        } else if (!currentState.equals(serviceMotionState) &&
                                (!MotionStateEnum.STATE_UNKNOWN.equals(currentState) || !MotionStateEnum.STATE_NORMAL.equals(currentState))) {
                            serviceMotionState = currentState;
                            playSoundByState(currentState);
                        }
                    }
                    executeFallAction(currentState);
                    taskHandler.postDelayed(this, REFRESH_DELAY);
                }
            };
        } else {
            refreshTask.run();
        }
    }

    private void playSoundByState(MotionStateEnum currentState) {
        switch(currentState){
            case STATE_FALL:
                playSound(soundHelper.getFallingSound());
                break;
            case STATE_WALKING:
                playSound(soundHelper.getWalkingSound());
                break;
            case STATE_NORMAL:
                break;
            case STATE_SITTING:
                playSound(soundHelper.getSittingSound());
                break;
            case STATE_STANDING:
                playSound(soundHelper.getStandingSound());
                break;
            case STATE_UNKNOWN:
                break;
        }
    }

    private void executeFallAction(MotionStateEnum currentStateEnum) {
        if(isNotificationOn&&isMoreThanTenSeconds())
        {
            alertUser();
        }
        if (isNotificationOn && isMoreThanOneMinute() && (MotionStateEnum.STATE_SITTING.equals(currentStateEnum) ||
                MotionStateEnum.STATE_UNKNOWN.equals(currentStateEnum))) {
            doAction(settingsReaderWriterHelper.getValueByKey(SettingsReaderWriterHelperImpl.ACTION_CHECKED));
            setFallNotificationOn(Boolean.FALSE);
        }
    }

    private void alertUser() {
        if(SettingsReaderWriterHelperImpl.ACTION_SMS.equals
                (settingsReaderWriterHelper.getValueByKey(SettingsReaderWriterHelperImpl.ACTION_CHECKED))){
            soundHelper.playSound(soundHelper.getSmsWarning());
        }
    }

    private void doAction(String whatToDo) {
        switch (whatToDo) {
            case SettingsReaderWriterHelperImpl.ACTION_CALL:
                startCallActivity();
            case SettingsReaderWriterHelperImpl.ACTION_SMS:
                startSmsActivity();
                break;
            case SettingsReaderWriterHelperImpl.ACTION_BOTH:
                startBothActionsActivity();
                break;
            case SettingsReaderWriterHelperImpl.ACTION_NONE:
                break;
        }
    }

    private void startBothActionsActivity() {
        Intent bothActionsIntent = new Intent(this, BothActionsActivity.class);
        bothActionsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(bothActionsIntent);
    }

    @Override
    public void onDestroy() {
        taskHandler.removeCallbacks(refreshTask);
        toastHelper.showLongMessage(getResources().getString(R.string.serviceStopped));
        motionDetectionProvider.stopDetection();
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void startSmsActivity() {
        Intent smsIntent = new Intent(this, SmsActivity.class);
        if (curentLocation!=null) {
            smsIntent.putExtra(SmsActivity.MESS_TEXT, getResources().getString(R.string.SMS_Location, getAddressFromLocation(curentLocation).toString()));
        }
        else {
            smsIntent.putExtra(SmsActivity.MESS_TEXT, getResources().getString(R.string.SMS_NoLocation));
        }
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(smsIntent);
    }

    private void startCallActivity() {
        Intent callIntent = new Intent(this, CallActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {

        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }
        return false;

    }


    public void playSound(int soundType) {
        soundHelper.playSound(soundType);
    }

    public void showFallNotification() {
        createNotification(null, null);
    }

    public void createNotification(String title, String text) {
        String action = settingsReaderWriterHelper.getValueByKey(SettingsReaderWriterHelperImpl.ACTION_CHECKED);

        Intent actionIntent = getIntent(action != null ? action : SettingsReaderWriterHelperImpl.ACTION_NONE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(title != null ? title : getResources().getString(R.string.app_name))
                .setContentText(text != null ? text : getResources().getString(R.string.defaultNotificationText, motionDetectionProvider.getSystemState().toString()))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(Boolean.TRUE)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;

        notificationManager.notify(0, notification);
        startNotification = new Date();
        lastTimeSoundPlayed=new Date();
    }

    @Nullable
    private Intent getIntent(String notificationTypeEnum) {
        Intent actionIntent = null;
        if (notificationTypeEnum != null) {
            if (SettingsReaderWriterHelperImpl.ACTION_CALL.equals(notificationTypeEnum)) {
                actionIntent = new Intent(this, CallActivity.class);
            } else if (SettingsReaderWriterHelperImpl.ACTION_SMS.equals(notificationTypeEnum)) {
                actionIntent = new Intent(this, SmsActivity.class);
            } else if (SettingsReaderWriterHelperImpl.ACTION_BOTH.equals(notificationTypeEnum)) {
                actionIntent = new Intent(this, BothActionsActivity.class);
            } else {
                actionIntent = new Intent(this, MainActivity.class);
            }
        }
        return actionIntent;
    }


    private boolean isMoreThanOneMinute() {
        curentTime = new Date();
        return startNotification != null ? (curentTime.getTime() - startNotification.getTime()) / 1000 > EMERGENCY_ACTION_DELAY : false;
    }

    private boolean isMoreThanTenSeconds() {
        curentTime = new Date();
        if(lastTimeSoundPlayed != null && (curentTime.getTime() - lastTimeSoundPlayed.getTime()) / 1000 > 10)
        {
            lastTimeSoundPlayed = new Date();
            return  Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static boolean isNotificationOn() {
        return isNotificationOn;
    }

    public static void setFallNotificationOn(boolean notificationOn) {
        isNotificationOn = notificationOn;
    }

    private StringBuilder getAddressFromLocation(Location lastLocation) {
        StringBuilder addressText = new StringBuilder();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i))
                            .append(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return addressText.length() == 0 ? new StringBuilder(getResources().getString(R.string.noAddress)) : addressText;
    }

    public void onLocationChanged(Location location) {
        this.curentLocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

