package org.apache.cordova.core;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseAnalytics;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;

public class ParseReceiver extends ParsePushBroadcastReceiver
{
    private static final String TAG = "ParsePluginReceiver";
    private static final String RECEIVED_IN_FOREGROUND = "receivedInForeground";
    private static int badgeCount = 0;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);

        if (pushData != null) {
            if (ParsePlugin.isInForeground()) {
                ParsePlugin.javascriptEventCallback(pushData);
            } else {
                super.onPushReceive(context, intent);
            }
        }
        badgeCount += 1;
        setBadge(context, badgeCount);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);

        if (pushData != null) {
            if (ParsePlugin.isInForeground()) {
                ParseAnalytics.trackAppOpened(intent);
                ParsePlugin.javascriptEventCallback(pushData);
            } else {
                super.onPushOpen(context, intent);
                ParsePlugin.setLaunchNotification(pushData);
            }
        }
    }

    private static JSONObject getPushData(Intent intent){
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            pushData.put(RECEIVED_IN_FOREGROUND, ParsePlugin.isInForeground());
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while parsing push data:", e);
        } finally{
            return pushData;
        }
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }
}
