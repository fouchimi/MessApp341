package com.social.messapp34.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParseUser;
import com.social.messapp34.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by ousmane on 1/16/17.
 */

public class ParsePushBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ParsePushBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.d(TAG, "Receiver intent null");
        } else {
            // Parse push message and handle accordingly
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        String alert = "", title="";
        if (action.equals(intentAction)) {
            String channel = intent.getExtras().getString("com.parse.Channel");
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                // Iterate the parse keys if needed
                Iterator<String> itr = json.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    String value = json.getString(key);
                    Log.d(TAG, "..." + key + " => " + value);
                    if(key.equals("title")){
                        title = value;
                    }else if(key.equals("alert")){
                        alert = value;
                    }
                }
                 showNotification(context, title, alert);

            } catch (JSONException ex) {
                Log.d(TAG, "JSON failed!");
            }
        }
    }

    public void showNotification(Context context, String sender,  String message) {
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(sender)
                .setContentText(message)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setNumber(1)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
