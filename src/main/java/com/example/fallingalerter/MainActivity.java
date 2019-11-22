package com.example.fallingalerter;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.fallingalerter.commons.SensorManagerHelperImpl;
import com.example.fallingalerter.commons.ToastHelper;
import com.example.fallingalerter.impl.SettingsReaderWriterHelperImpl;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_CONTACT = 1;
    private static final int PERMISSION_TO_USE_PHONE_AND_SMS = 2;
    private SettingsReaderWriterHelperImpl settingsReaderWriterHelper;
    private Button sensorMonitoringButton;
    private Button startStopButton;
    private Button showLocationButton;
    private Button sensorListButton;
    private Button aboutButton;
    private TextView emergencyPersonText;
    private ToastHelper toastHelper;
    private CheckBox dontShowAgain;
    private SensorManagerHelperImpl sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsReaderWriterHelper = new SettingsReaderWriterHelperImpl(this);
        toastHelper = new ToastHelper(this);
        sensorManager = new SensorManagerHelperImpl(this);

        setupUI();
        setupListeners();

    }

    @Override
    protected void onStart() {
        super.onStart();

        checkForEmergencyPerson();
        if (SensorService.isServiceRunning(this, SensorService.class)) {
            startStopButton.setText(getResources().getString(R.string.startStopButton, "Stop"));
            startStopButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_stop_service, 0, 0, 0);
        } else {
            startStopButton.setText(getResources().getString(R.string.startStopButton, "Start"));
            startStopButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_start, 0, 0, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CALL_PHONE, android.Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_TO_USE_PHONE_AND_SMS);
            return;
        }

    }

    @SuppressLint("StringFormatInvalid")
    private void checkForEmergencyPerson() {
        String phoneNumber = settingsReaderWriterHelper.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            if (!settingsReaderWriterHelper.readBooleanValue(SettingsReaderWriterHelperImpl.NO_EMERGENCY_CONTACT)) {
                showWarningMessageForMissingPhone();
            }
        } else {
            String personName = settingsReaderWriterHelper.getPersonName();
            emergencyPersonText.setText(getResources().getString(R.string.emergencyContact, personName, phoneNumber));
        }
    }

    private void showWarningMessageForMissingPhone() {
        AlertDialog.Builder lertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.check_box, null);


        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        lertDialogBuilder.setView(eulaLayout);
        lertDialogBuilder.setTitle(getResources().getString(R.string.noPhoneMessage));
        lertDialogBuilder.setMessage(Html.fromHtml(getResources().getString(R.string.noPhoneQuestion)));

        lertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                settingsReaderWriterHelper.writeBooleanValue(SettingsReaderWriterHelperImpl.NO_EMERGENCY_CONTACT, dontShowAgain.isChecked());
                showContactDialog();
            }
        });

        lertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                settingsReaderWriterHelper.writeBooleanValue(SettingsReaderWriterHelperImpl.NO_EMERGENCY_CONTACT, dontShowAgain.isChecked());
                dialog.dismiss();
            }
        });

        lertDialogBuilder.show();

    }

    private void showContactDialog() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void showSensorList() {
        List<Sensor> sensorList = sensorManager.getSensorList();
        StringBuilder sensorsText = new StringBuilder();

        for (Sensor s : sensorList) {
            sensorsText.append(s.getName()).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.sensorListTitle))
                .setMessage(sensorsText)
                .setIcon(R.drawable.ic_setting_light)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();


    }

    private boolean isServiceRunning() {
        return SensorService.isServiceRunning(this, SensorService.class);
    }

    private void setupListeners() {
        sensorMonitoringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SensorActivity.class);
                startActivity(myIntent);

            }
        });
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    stopSensorService();
                } else {
                    startSensorService();
                }
            }
        });
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(MainActivity.this, MyLocationActivity.class);
                startActivity(locationIntent);
            }
        });
        sensorListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSensorList();

            }
        });
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (SensorService.isServiceRunning(this, SensorService.class) && !settingsReaderWriterHelper.readBooleanValue(SettingsReaderWriterHelperImpl.SERVICE_STATUS)) {
            stopSensorService();
        }
        super.onDestroy();
    }

    private void startSensorService() {
        startService(new Intent(getBaseContext(), SensorService.class));
        startStopButton.setText(getResources().getString(R.string.startStopButton, "Stop"));
        startStopButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_stop_service, 0, 0, 0);
    }

    private void stopSensorService() {
        stopService(new Intent(getBaseContext(), SensorService.class));
        startStopButton.setText(getResources().getString(R.string.startStopButton, "Start"));
        startStopButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_start, 0, 0, 0);
    }

    private void showAboutDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle("About " + getResources().getString(R.string.app_name));


        Button aboutOK = (Button) dialog.findViewById(R.id.aboutOK);
        // if button is clicked, close the custom dialog
        aboutOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void setupUI() {
        sensorMonitoringButton = (Button) findViewById(R.id.sensorMonitoringButton);
        emergencyPersonText = (TextView) findViewById(R.id.emergencyPersonText);
        startStopButton = (Button) findViewById(R.id.startStopButton);
        showLocationButton = (Button) findViewById(R.id.showLocationButton);
        sensorListButton = (Button) findViewById(R.id.sensorListButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
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
            emergencyPersonText.setText(getResources().getString(R.string.emergencyContact, name, phone));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.app_name)
                    .setMessage(getResources().getString(R.string.quit_text_message, getResources().getString(R.string.app_name)))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_TO_USE_PHONE_AND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        showDetailPhoneCallPermission();
                    }
                    if (grantResults.length > 1 && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        showDetailSMSPermission();
                    }
                    if (grantResults.length > 2 && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        showDetailGPSPermission();
                    }

                } else {
                    toastHelper.showLongMessage(getResources().getString(R.string.locationPersmissionDenied));
                }
                break;
            }


        }
    }

    private void showDetailGPSPermission() {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(getResources().getString(R.string.gpsPermission))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showDetailPhoneCallPermission() {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(getResources().getString(R.string.callPermission))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askForPermission(android.Manifest.permission.CALL_PHONE);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void askForPermission(String permission) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                PERMISSION_TO_USE_PHONE_AND_SMS);
    }

    private void showDetailSMSPermission() {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(getResources().getString(R.string.smsPermission))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askForPermission(Manifest.permission.SEND_SMS);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


}
