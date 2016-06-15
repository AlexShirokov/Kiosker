package com.doublebrain.kiosker.free;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by AlexShredder on 06.04.2016.
 */
public class App extends android.app.Application {
    public static final String KEY_USER_PASS = "user_pass";
    public static final String KEY_KIOSK_APP = "kiosk_app";
    public static final String KEY_OLD_LAUNCHER = "old_launcher";
    public static final boolean APPDEBUG = false;
    public static boolean testVersion = false;
    public static boolean isError = false;
    public static String app2watch = "";
    public static String thisAppPackage = "";
    private static final String TAG = "4ls-App";

    public static String user_pass;
    public static String old_launcher;

    private static SharedPreferences preferences;
    public static Context context;
    public static boolean blocked=false;
    public static boolean serviceActive=false;
    public static boolean justBooted=false;

    public App() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        testVersion = false;//getPackageName().toLowerCase().contains("free");
        thisAppPackage = getPackageName();

        Logger.logd(TAG, "OnCreate...", false);
        Logger.logd(TAG, "Package name..."+ thisAppPackage, false);
        Logger.logd(TAG, "Demo..."+String.valueOf(testVersion), false);


        context = getApplicationContext();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        getAllValues();
        blocked = !user_pass.isEmpty() && !app2watch.isEmpty() && AppHelper.isAppInstalled(app2watch);

        startService(new Intent(this,MyService.class));
    }


    public static void getAllValues() {
        user_pass = preferences.getString(KEY_USER_PASS,"");
        old_launcher = preferences.getString(KEY_OLD_LAUNCHER,"");
        app2watch = preferences.getString(KEY_KIOSK_APP,"");
     }

    public static void setValue(String key, String value) {
        preferences.edit().putString(key, value).commit();
        getAllValues();
    }

}
