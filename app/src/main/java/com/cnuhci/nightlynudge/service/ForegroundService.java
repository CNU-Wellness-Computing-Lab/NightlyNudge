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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForegroundService extends Service {
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "ForegroundServiceChannelId";

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;

    private BackgroundTask task;

//    private int count = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        task = new BackgroundTask();
        updateNotification(getCurrentDateTime());

        task.execute();

        return START_NOT_STICKY;
    }


    private void updateNotification(String date){
        if(notificationManager.getNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID) == null){
            createNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID);
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), FOREGROUND_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("App is Running: " + date )
                .setContentTitle("NightlyNudge")
                .setChannelId(FOREGROUND_SERVICE_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setNotificationSilent()
                .build();

        notificationManager.notify(1, notification);
        startForeground(1, notification);
    }

    private void createNotificationChannel(String notificationChannelId){
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannel = new NotificationChannel(notificationChannelId, notificationChannelId, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public static String getCurrentDateTime() {
        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "yyyy-MM-dd HH:mm:ss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        return formatter.format(today).toString();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackgroundTask extends AsyncTask<Integer, String, Integer>{
        @Override
        protected Integer doInBackground(Integer... integers) {
            while(isCancelled() == false){
                try{
                    Log.d("TEST", "running in background...");
                    Thread.sleep(1000);
                    updateNotification(getCurrentDateTime());
                } catch (InterruptedException ex){
                }
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


}