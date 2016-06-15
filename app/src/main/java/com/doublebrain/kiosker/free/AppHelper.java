package com.doublebrain.kiosker.free;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by AlexShredder on 07.04.2016.
 */
public class AppHelper {
    /** first app user */
    public static final int AID_APP = 10000;

    /** offset for uid ranges for each user */
    public static final int AID_USER = 100000;
    private static final String TAG = "4ls-AppHelper";

    public static boolean isAnyActive(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            String foregroundTaskPackageName = AppHelper.getForegroundApp();

            if (foregroundTaskPackageName == null) foregroundTaskPackageName = "nop";
            Logger.logd(TAG, foregroundTaskPackageName, false);

            return (foregroundTaskPackageName.equals(App.thisAppPackage) || foregroundTaskPackageName.equals(App.app2watch));
        } else return isAnyActiveForDrova();

    }


    public static boolean isAnyActiveForDrova(){
        ActivityManager am = (ActivityManager) App.context.getSystemService(App.ACTIVITY_SERVICE);
        // The first in the list of RunningTasks is always the foreground task.
        //ActivityManager.RunningAppProcessInfo foregroundTaskInfo = am.getRunningAppProcesses().get(0);

        boolean isActive=false;
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        int id1c=-1;
        for (int i = 0; i < procs.size(); i++) {
            ActivityManager.RunningAppProcessInfo o = procs.get(i);
            if (o.processName.equals(App.app2watch) || o.processName.equals(App.thisAppPackage)){
                id1c = o.uid;
                isActive = isActive || (o.importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
                if (isActive) break;
            }
        }
        Logger.logd(TAG, "Check is our pets active (old variant): "+String.valueOf(isActive), false);

        return isActive;
    }

    public synchronized static String getForegroundApp() {
        File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;
        String foregroundProcess = null;

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            int pid;
            try {
                pid = Integer.parseInt(file.getName());
            } catch (NumberFormatException e) {
                continue;
            }

            try {
                String cgroup = read(String.format("/proc/%d/cgroup", pid));

                String[] lines = cgroup.split("\n");

                if (lines.length < 2 || lines.length >5) {
                    continue;
                }

                int cS=-1,caS=-1;
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains("cpuacct:")) caS=i;
                    else if (lines[i].contains("cpu:")) cS=i;
                }

                if (caS<0 || cS<0) continue;

                String cpuSubsystem = lines[cS];
                String cpuaccctSubsystem = lines[caS];

                if (!cpuaccctSubsystem.endsWith(Integer.toString(pid))) {
                    // not an application process
                    continue;
                }

                if (cpuSubsystem.endsWith("bg_non_interactive")) {
                    // background policy
                    continue;
                }

                String cmdline = read(String.format("/proc/%d/cmdline", pid)).trim();

                if (cmdline.contains("com.android.systemui")) {
                    continue;
                }

                int uid = Integer.parseInt(
                        cpuaccctSubsystem.split(":")[2].split("/")[1].replace("uid_", ""));
                if (uid >= 1000 && uid <= 1038) {
                    // system process
                    continue;
                }

                int appId = uid - AID_APP;
                int userId = 0;
                // loop until we get the correct user id.
                // 100000 is the offset for each user.
                while (appId > AID_USER) {
                    appId -= AID_USER;
                    userId++;
                }

                if (appId < 0) {
                    continue;
                }

                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead()) {
                    int oomAdj = Integer.parseInt(read(oomScoreAdj.getAbsolutePath()));
                    if (oomAdj != 0) {
                        continue;
                    }
                }

                int oomscore = Integer.parseInt(read(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore) {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return foregroundProcess;
    }

    private static String read(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        output.append(reader.readLine());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            output.append('\n').append(line);
        }
        reader.close();
        return output.toString();
    }

    public static String getDefaultLauncher() {

        PackageManager pm = App.context.getPackageManager();
        Intent i = new Intent("android.intent.action.MAIN");
        i.addCategory("android.intent.category.HOME");
        List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);
        if (lst != null)
        {
            for (ResolveInfo resolveInfo : lst) {
                if (!resolveInfo.activityInfo.packageName.equals(App.context.getPackageName())){
                    Logger.logd(TAG, "Resolved activities: " + resolveInfo.activityInfo.packageName, false);
                    return resolveInfo.activityInfo.packageName;
                 }
            }
        }
        return "";
    }

    public static boolean isAppInstalled(String uri) {
        PackageManager pm = App.context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}
