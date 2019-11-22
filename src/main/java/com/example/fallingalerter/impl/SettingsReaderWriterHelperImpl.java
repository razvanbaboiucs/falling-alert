package com.example.fallingalerter.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fallingalerter.api.ISettingsReaderWriterHelper;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by bbuescu on 7/7/2016.
 */
public class SettingsReaderWriterHelperImpl implements ISettingsReaderWriterHelper{
    private Context context;
    private SharedPreferences sharedPreferences;

    private final static String APP_SETTING = "APP_SETTINGS";
    public static final String PHONE_NUMBER_KEY = "PHONE_NUMBER";
    public static final String PERSON_NAME_KEY = "PHONE_NUMBER_NAME";
    public static final String SOUND_KEY = "SOUND";
    public static final String SERVICE_STATUS = "SERVICE_STATUS";
    public static final String FEEDBACK_STATUS = "FEEDBACK_STATUS";
    public static final String ACTION_CHECKED = "ACTION_CHECKED";
    public static final String ACTION_CALL = "CALL";
    public static final String ACTION_SMS = "SMS";
    public static final String ACTION_BOTH = "BOTH";
    public static final String ACTION_NONE = "NONE";
    public static final String SENSOR_SENSIBILITY = "SENSOR_SENSIBILITY";
    public static final String AUDIO_VOLUME = "AUDIO_VOLUME";
    public static final String NO_EMERGENCY_CONTACT="NO_EMERGENCY_CONTACT";

    public SettingsReaderWriterHelperImpl(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(APP_SETTING, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getPhoneNumber() {
       return getValueByKey(PHONE_NUMBER_KEY);
    }

    @Override
    public String getPersonName() {
        return getValueByKey(PERSON_NAME_KEY);
    }

    @Override
    public String getValueByKey(String key) {
        return sharedPreferences.getString(key, "");
    }

    @Override
    public void writeValue(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void setPhoneNumber(String phoneNumber) {
        writeValue(PHONE_NUMBER_KEY, phoneNumber);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(SOUND_KEY, enabled).apply();
    }

    public boolean getSoundEnabled() {
        return sharedPreferences.getBoolean(FEEDBACK_STATUS, false);
    }

    public Map<String, ?> getAllKeys() {
        return sharedPreferences.getAll();
    }

    public void writeBooleanValue(String key, boolean value)
    {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean readBooleanValue(String key)
    {
        return sharedPreferences.getBoolean(key, false);
    }

    public void writeIntValue(String key, int value)
    {
        sharedPreferences.edit().putInt(key,value).apply();
    }

    public int readIntValue(String key)
    {
        return sharedPreferences.getInt(key, 0);
    }
}
