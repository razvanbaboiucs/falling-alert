package com.example.fallingalerter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

public class SettingsActivity extends AppCompatActivity {
    private static final int PICK_CONTACT = 1;

    private TextView curentSeekBarValue;
    private SeekBar sensorSlider;
    private CheckBox serviceOnOff;
    private CheckBox audioFeedback;
    private RadioGroup actOnFall;
    private Button changeContact;
    private TextView currentContact;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private TextView currentAudioValue;
    private SeekBar audioSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        setupUI();
        setupListeners();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadValues();
    }

    private void loadValues() {
        int sensorValue = settingsReaderWriterHelper.readIntValue(SettingsReaderWriterHelperImpl.SENSOR_SENSIBILITY);
        boolean serviceStatus = settingsReaderWriterHelper.readBooleanValue(SettingsReaderWriterHelperImpl.SERVICE_STATUS);
        boolean feedbackStatus = settingsReaderWriterHelper.readBooleanValue(SettingsReaderWriterHelperImpl.FEEDBACK_STATUS);
        String actionChecked = settingsReaderWriterHelper.getValueByKey(SettingsReaderWriterHelperImpl.ACTION_CHECKED);
        int audioValue = settingsReaderWriterHelper.readIntValue(SettingsReaderWriterHelperImpl.AUDIO_VOLUME);
        curentSeekBarValue.setText(getResources().getString(R.string.currentValue, String.valueOf(sensorValue)));
        currentAudioValue.setText(getResources().getString(R.string.currentVolume, String.valueOf(audioValue)));
        sensorSlider.setProgress(sensorValue);
        audioSlider.setProgress(audioValue);
        serviceOnOff.setChecked(serviceStatus);
        audioFeedback.setChecked(feedbackStatus);
        switch (actionChecked) {
            case SettingsReaderWriterHelperImpl.ACTION_CALL: {
                actOnFall.check(R.id.radio_call);
                break;
            }
            case SettingsReaderWriterHelperImpl.ACTION_SMS: {
                actOnFall.check(R.id.radio_sms);
                break;
            }
            case SettingsReaderWriterHelperImpl.ACTION_BOTH: {
                actOnFall.check(R.id.radio_both);
                break;
            }
            case SettingsReaderWriterHelperImpl.ACTION_NONE: {
                actOnFall.check(R.id.radio_nothing);
                break;
            }
        }

        String personName = settingsReaderWriterHelper.getPersonName();
        String phoneNumber = settingsReaderWriterHelper.getPhoneNumber();
        currentContact.setText(getResources().getString(R.string.emergencyContact, personName, phoneNumber));

        if (!audioFeedback.isChecked()) {
            audioSlider.setEnabled(false);
        }

    }

    private void setupListeners() {


        audioSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAudioValue.setText(getResources().getString(R.string.currentVolume, progress));
                currentValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settingsReaderWriterHelper.writeIntValue(SettingsReaderWriterHelperImpl.AUDIO_VOLUME, currentValue);
            }
        });

        sensorSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                curentSeekBarValue.setText(getResources().getString(R.string.currentValue, progress));
                currentValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settingsReaderWriterHelper.writeIntValue(SettingsReaderWriterHelperImpl.SENSOR_SENSIBILITY, currentValue);
            }
        });

        serviceOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsReaderWriterHelper.writeBooleanValue(SettingsReaderWriterHelperImpl.SERVICE_STATUS, isChecked);
            }
        });
        audioFeedback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsReaderWriterHelper.writeBooleanValue(SettingsReaderWriterHelperImpl.FEEDBACK_STATUS, isChecked);
                audioSlider.setEnabled(isChecked);

            }
        });
        actOnFall.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_sms: {
                        settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.ACTION_CHECKED, SettingsReaderWriterHelperImpl.ACTION_SMS);
                        break;
                    }
                    case R.id.radio_call: {
                        settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.ACTION_CHECKED, SettingsReaderWriterHelperImpl.ACTION_CALL);
                        break;
                    }
                    case R.id.radio_both: {
                        settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.ACTION_CHECKED, SettingsReaderWriterHelperImpl.ACTION_BOTH);
                        break;
                    }
                    case R.id.radio_nothing: {
                        settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.ACTION_CHECKED, SettingsReaderWriterHelperImpl.ACTION_NONE);
                        break;
                    }
                    default: {
                        settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.ACTION_CHECKED, SettingsReaderWriterHelperImpl.ACTION_NONE);
                        break;
                    }
                }
            }
        });
        changeContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialog();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    processResultData(data);
                }
                break;
        }
    }

    private void processResultData(Intent data) {
        Uri contactData = data.getData();
        Cursor cursor = getContentResolver().query(contactData, new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String phone = cursor.getString(1);
            settingsReaderWriterHelper.setPhoneNumber(phone);
            settingsReaderWriterHelper.writeValue(SettingsReaderWriterHelperImpl.PERSON_NAME_KEY, name);
            currentContact.setText(getResources().getString(R.string.emergencyContact, name, phone));
        }
    }

    private void showContactDialog() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void setupUI() {
        curentSeekBarValue = (TextView) findViewById(R.id.textViewCurentSeekbar);
        sensorSlider = (SeekBar) findViewById(R.id.seekbar);
        serviceOnOff = (CheckBox) findViewById(R.id.checkbox_service);
        audioFeedback = (CheckBox) findViewById(R.id.checkbox_feedback);
        actOnFall = (RadioGroup) findViewById(R.id.radio_group);
        changeContact = (Button) findViewById(R.id.changeContactButton);
        currentContact = (TextView) findViewById(R.id.textViewContact);
        currentAudioValue = (TextView) findViewById(R.id.textViewAudioValue);
        audioSlider = (SeekBar) findViewById(R.id.audioSlider);
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
