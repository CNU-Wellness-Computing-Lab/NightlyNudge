package com.cnuhci.nightlynudge.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.loader.content.AsyncTaskLoader;

import com.cnuhci.nightlynudge.MainActivity;
import com.cnuhci.nightlynudge.R;
import com.cnuhci.nightlynudge.SleepSettingActivity;

public class ForegroundService extends Service {
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "ForegroundServiceChannelId";

    NotificationManager notificationManager;
    NotificationChannel notificationChannel;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initNotification();

        return START_NOT_STICKY;
    }


    public void initNotification(){
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannel = new NotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID, FOREGROUND_SERVICE_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), FOREGROUND_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("App is Running")
                .setContentTitle("NightlyNudge")
                .setChannelId(FOREGROUND_SERVICE_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        notificationManager.notify(1, notification);
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}