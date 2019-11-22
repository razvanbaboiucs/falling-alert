package com.example.fallingalerter.commons;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by bbuescu on 7/7/2016.
 */
public class ToastHelper {

    private Context context;

    public ToastHelper(Context context) {
        this.context = context;
    }

    public void showShortMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showLongMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
