package com.doublebrain.kiosker.free;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by AlexShredder on 06.04.2016.
 */
public class Logger {
    public static void logd(String tag, String mess, boolean toast){
        if (App.APPDEBUG) {
            Log.d(tag, mess);
            if (toast) toast(mess);
        }
    }

    public static void toast(String mess){
        if (App.APPDEBUG) Toast.makeText(App.context, mess, Toast.LENGTH_SHORT).show();
    }

}
