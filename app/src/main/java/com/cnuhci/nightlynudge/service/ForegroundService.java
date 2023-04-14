package com.cnuhci.nightlynudge.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cnuhci.nightlynudge.R;
import com.cnuhci.nightlynudge.SleepSettingActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ForegroundService extends Service {
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "ForegroundServiceChannelId";

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;

    public static SharedPreferences timeData;

    private BackgroundTask task;

//    private int count = 0;

    String statusText = "현재는 활동시간입니다.";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeData = getSharedPreferences("timeData", MODE_PRIVATE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        task = new BackgroundTask();
        updateNotification("green_100_");

        task.execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        task.cancel(true);
    }

    private void updateNotification(String imgName){
        if(notificationManager.getNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID) == null){
            createNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID);
        }

        int iconimage = getApplicationContext().getResources().getIdentifier(imgName, "drawable",
                getApplicationContext().getPackageName());



        Bitmap bigPictureBitmap = BitmapFactory.decodeResource(getResources(),iconimage);
//        if(bigPictureBitmap!=null) {
//            bigPictureBitmap = bigPictureBitmap.createScaledBitmap(bigPictureBitmap, 30, 30, true);
//        }else{
//            iconimage = R.drawable.green_100_;
//            bigPictureBitmap = BitmapFactory.decodeResource(getResources(),iconimage);
//            bigPictureBitmap = bigPictureBitmap.createScaledBitmap(bigPictureBitmap, 30, 30, true);
//        }


        Intent intent = new Intent(this, SleepSettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this,
                0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), FOREGROUND_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.green_100_)
                .setLargeIcon(bigPictureBitmap)
                .setContentText(statusText)
                .setContentTitle("NightlyNudge 실행중...")
                .setContentIntent(pendingIntent)
                .setChannelId(FOREGROUND_SERVICE_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setNotificationSilent()
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notification);
        startForeground(1, notification);
    }


    private void createNotificationChannel(String notificationChannelId){
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannel = new NotificationChannel(notificationChannelId, notificationChannelId, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);
    }


    public static long getCurrentDateTime() {
        // TODO: 현재 시간의 getTime 음수 값 출력 문제 해결
        Date today = new Date();
        Date currentDate;
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "HH:mm:ss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);

        Log.d("TEST", "current time from Locale: " + formatter.format(today).toString());

        try {
            currentDate = formatter.parse(formatter.format(today).toString());
            long currentTime = currentDate.getTime();
            Log.d("TEST", "current time :" + currentTime);
            return currentTime;
        }catch (ParseException e){
        }

        return -1l;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackgroundTask extends AsyncTask<Integer, String, Integer>{

        long mySleepTimer = timeData.getLong("totalSleepTime", -1l);
        long currentDiff = -1l;     // 현재 시간과 기상 시간의 차이를 봄
        long usageTime = -1l;

        @Override
        protected Integer doInBackground(Integer... integers) {
            long currentDateTime = -1l;     // 현재 시간 정보
            long totalSleepTime = -1l;      // 수면 시간 정보 (수면 시작 ~ 수면 끝 시간대)
            long preparationTime = -1l;     // 수면 준비 시간 (준비 시작 ~ 수면 시작 시간대)

            while(isCancelled() == false){
                try{
                    // check 초기화
                    if(!timeData.getBoolean("isSet", true)){
                        cancel(true);
                        break;
                    }

                    Log.d("TEST", "running in background... | time: " + mySleepTimer);
                    currentDateTime = getCurrentDateTime();
                    totalSleepTime = timeData.getLong("totalSleepTime", -1l);
                    preparationTime = totalSleepTime + (60*60*1000);

//                    Log.d("TEST", "current time : " + currentDateTime/ (60 * 60 * 1000) % 24 + ":" + currentDateTime /  (60 * 1000) % 60 + ":" + currentDateTime /  (1000) % 60);

                    if( timeData.getLong("wakeTime", -1l) >= currentDateTime){
                        currentDiff = timeData.getLong("wakeTime", -1l) - currentDateTime;
                    } else {
                        currentDiff = timeData.getLong("wakeTime", -1) + (24*60*60*1000) - currentDateTime;
                    }

                    Log.d("TEST", "current diff : " + currentDiff);
                    Log.d("TEST", "sleep time   : " + timeData.getLong("totalSleepTime", -1l));


                    if( currentDiff <= preparationTime){
                        if(checkDisplayOn()) {
                            usageTime += 1;
                        }
                    }else{
                        usageTime = -1l;
                    }


                    if(totalSleepTime < currentDiff && currentDiff <= preparationTime){
                        Log.d("TEST", "현재는 수면 준비시간입니다.사용 시간: " + usageTime);
                        statusText = "현재는 수면 준비시간입니다. 사용 시간: " + usageTime ;
                    }else if(currentDiff <= totalSleepTime){
                        Log.d("TEST", "현재는 수면 시간입니다. 사용 시간: " + usageTime);
                        statusText = "현재는 수면시간입니다. 사용 시간: " + usageTime;
                    }else{
                        Log.d("TEST", "현재는 활동 시간입니다. 사용 시간: " + usageTime);
                        statusText = "현재는 활동 시간입니다.";
                    }

                    if (mySleepTimer > 0){
                        mySleepTimer -= 1;
                    }

                    updateNotification("green_100_"); // TODO: 수면 상태에 따라 이름과 번호 mapping

                    Thread.sleep(1000);
                } catch (InterruptedException ex){
                }
            }
            return 0;
        }

        /**
         * 스마트폰 화면이 켜져있는 것 판별
         * @return 화면 켜져있는 경우 True
         */
        private boolean checkDisplayOn(){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            boolean result= Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()
                    || Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();
            return result;
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
            task.cancel(true);

        }
    }


}