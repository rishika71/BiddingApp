package com.example.biddingapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String notificationTitle = null, notificationBody = null;

        if (remoteMessage.getNotification() != null) {

            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }
        Log.d("demo", "onMessageReceived: " + notificationTitle);
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATIONS";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setSmallIcon(R.drawable.flag)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(this).notify(1, notification.build());
        }


    }


}