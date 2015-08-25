package com.smssender;

import android.app.Application;
import android.content.SharedPreferences;

public class SmsSenderApplication extends Application {

    public static SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("SmsSender", MODE_PRIVATE);
    }
}
