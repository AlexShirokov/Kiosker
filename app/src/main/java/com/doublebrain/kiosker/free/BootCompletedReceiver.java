package com.doublebrain.kiosker.free;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootCompletedReceiver extends BroadcastReceiver {
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.logd("4ls-receiver", "onRecieve: "+intent.getAction(), false);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            App.justBooted = true;
            Logger.logd("4ls-receiver", "boot completed", false);

            App.context.startService(new Intent(App.context, MyService.class));

        } else if (intent.getAction().equals(Intent.CATEGORY_HOME)){
            Logger.logd("4ls-receiver", "CATEGORY_HOME", false);
        }
    }
}
