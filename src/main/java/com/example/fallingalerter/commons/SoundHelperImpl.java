package com.example.fallingalerter.commons;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.example.fallingalerter.R;
import com.example.fallingalerter.api.ISoundHelper;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

import java.util.ArrayList;

/**
 * Created by bbuescu on 7/11/2016.
 */
public class SoundHelperImpl implements ISoundHelper {
    private Context context;
    SoundPool soundPool;
    private int fallingSound;
    private int sittingSound;
    private int standingSound;
    private int walkingSound;



    private int smsWarning;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private ArrayList<Integer> soundArray;

    public SoundHelperImpl(Context context) {
        this.context = context;
        soundArray = new ArrayList<>();
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(context);
        loadSound(0);
    }

    @Override
    public void playSound(int sound) {
        Log.d("audio", "sunet: "+sound);
        Log.d("audio", "eval: "+(soundArray.contains(sound) && isSoundEnabled()));
        if (soundArray.contains(sound) && isSoundEnabled()) {
            float volume =  ((float)settingsReaderWriterHelper.readIntValue(SettingsReaderWriterHelperImpl.AUDIO_VOLUME) / 10);
            Log.d("audio", "volum: "+settingsReaderWriterHelper.readIntValue(SettingsReaderWriterHelperImpl.AUDIO_VOLUME));
            soundPool.play(sound, volume, volume, 1, 0, 1f);
        }
    }

    private boolean isSoundEnabled() {
        return settingsReaderWriterHelper.getSoundEnabled();
    }

    @Override
    public void loadSound(int sound) {
        this.fallingSound = soundPool.load(context, R.raw.falling, 1);
        this.sittingSound = soundPool.load(context, R.raw.sitting, 2);
        this.standingSound = soundPool.load(context, R.raw.standing, 3);
        this.walkingSound = soundPool.load(context, R.raw.walking, 4);
        this.smsWarning = soundPool.load(context,R.raw.sms_warning,5);
        soundArray.add(this.fallingSound);
        soundArray.add(this.standingSound);
        soundArray.add(this.walkingSound);
        soundArray.add(this.sittingSound);
        soundArray.add(this.smsWarning);
    }

    public int getFallingSound() {
        return fallingSound;
    }

    public int getSittingSound() {
        return sittingSound;
    }

    public int getStandingSound() {
        return standingSound;
    }

    public int getWalkingSound() {
        return walkingSound;
    }

    public int getSmsWarning() {
        return smsWarning;
    }
}
