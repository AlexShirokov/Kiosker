package com.doublebrain.kiosker.free;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    private static final String TAG = "4ls-MYSERVICE";

    private static boolean mStarted = false;

    private static int interval = 5;

    public MyService() {
     }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.logd(TAG, "Service created...", false);

        setNotifyIcon();
        if (!mStarted){
            startMon();
        }
        mStarted = true;

        App.serviceActive = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.logd(TAG, "Starting service...", false);

        return Service.START_STICKY_COMPATIBILITY;
    }

    private void startMon() {

        new Thread(new Runnable() {
            int count=0;
            public void run() {

                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(interval);
                        if (count==15){
                            Logger.logd(TAG,"Too much count exec in time",false);
                            App.isError = true;
                            continue;
                        }

                        /*
                        if (App.testVersion && AppHelper.isAppInstalled(App.thisAppPackageFull)) {
                            Logger.logd(TAG,"Full version installed... service not ran",false);
                            continue;
                        }
                        if (!App.testVersion && AppHelper.isAppInstalled(App.thisAppPackageFree)) {
                            Logger.logd(TAG,"Test version installed... service not ran",false);
                            continue;
                        }*/
                        if (App.app2watch.isEmpty() || !AppHelper.isAppInstalled(App.app2watch)) {
                            Logger.logd(TAG,"App2Watch not found... service not ran",false);
                            continue;
                        }
                        interval = 1;
                        App.serviceActive = true;
                        //if (App.justBooted) continue;;
                        //Log.d(LOG_TAG, "update run");com.e1c.mobile
                        PowerManager powerManager = (PowerManager)App.context.getSystemService(Context.POWER_SERVICE);
                        boolean isScreenAwake = false;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            //isScreenAwake = (Build.VERSION.SDK_INT < 20? powerManager.isScreenOn():powerManager.isInteractive());
                            isScreenAwake = powerManager.isInteractive();
                        }else isScreenAwake = powerManager.isScreenOn();
                        //isScreenAwake = true;

                        if (isScreenAwake && App.blocked){

                            if (!AppHelper.isAnyActive()){
                                Logger.logd(TAG, "!!!!!!!!!!            Starting app...", false);
                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(App.app2watch);
                                startActivity(launchIntent);
                                count++;
                            } else count = 0;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setNotifyIcon() {

        Context context = App.context;

        Intent notificationIntent = new Intent(context, LoginActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.hungrycat)) //большая картинка
                .setTicker(res.getString(R.string.app_name)) // текст в строке состояния
                //.setTicker(String.format(getString(R.string.warningHostNotResponding),param.name))
                .setWhen(System.currentTimeMillis())
                //.setSound(Uri.parse(Application.notifications_ringtone))
                //.setVibrate(vibrate)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(res.getString(R.string.app_name))
         ; // Текст уведомления

        // Notification notification = builder.getNotification(); // до API 16
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        startForeground(Notification.FLAG_ONGOING_EVENT, notification);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Logger.logd(TAG, "onTaskRemoved", false);
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000, restartServicePI);

    }



}
