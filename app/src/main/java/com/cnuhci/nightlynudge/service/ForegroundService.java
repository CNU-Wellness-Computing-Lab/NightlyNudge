package com.cnuhci.nightlynudge.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cnuhci.nightlynudge.CSV;
import com.cnuhci.nightlynudge.Data;
import com.cnuhci.nightlynudge.R;
import com.cnuhci.nightlynudge.SleepSettingActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForegroundService extends Service {
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "ForegroundServiceChannelId";
    public static final String STATUS_NOTIFY_CHANNEL_ID = "StatusNotificationChannelId";
    public static final String INITIATION_NOTIFY_CHANNEL_ID = "InitiationNotificationChannelId";

    private NotificationManager notificationManager;

    public static SharedPreferences timeData;

    private BackgroundTask task;

    public static int C = 100; // 배터리 최대 용량으로, 상태 변화 시, 가변적으로 변화
    public static float T = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeData = getSharedPreferences("timeData", MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        task = new BackgroundTask();
        updateNotification("green", 100);

        task.execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        task.cancel(true);
    }

    /**
     * Foreground service 의 내용 갱신을 위한 함수
     *
     * @param status   배터리의 상태 정보
     * @param capacity 배터리의 용량 정보
     */
    private void updateNotification(String status, int capacity) {
        if (notificationManager.getNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID) == null) {
            createNotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID);
        }

        String imgName = status + "_" + capacity;

        int iconimage = getApplicationContext().getResources().getIdentifier(imgName, "drawable",
                getApplicationContext().getPackageName());


        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), iconimage);

        Intent intent = new Intent(this, SleepSettingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this,
                0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), FOREGROUND_SERVICE_CHANNEL_ID)
                .setSmallIcon(iconimage)
                .setLargeIcon(iconBitmap)
                .setContentText(getBattStatusText(status, capacity))
                .setContentTitle("예상 수면 및 상태")
                .setContentIntent(pendingIntent)
                .setChannelId(FOREGROUND_SERVICE_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setNotificationSilent()
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notification);
        startForeground(1, notification);
    }

    /**
     * 배터리의 정보에 변화(상태 또는 용량)가 있을 경우 알림 발생
     */
    private void notifyBatteryChange(@Nullable String _message) {
        if (notificationManager.getNotificationChannel(STATUS_NOTIFY_CHANNEL_ID) == null) {
            createNotificationChannel(STATUS_NOTIFY_CHANNEL_ID);
        }

        String message = "배터리에 변화가 있습니다.";

        if (_message != null) {
            message = _message;
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), STATUS_NOTIFY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("SleepBattery")
                .setContentText(message)
                .setChannelId(STATUS_NOTIFY_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        notificationManager.notify(2, notification);
    }

    private void notifyBatteryInitiation(@Nullable String _message) {
        if (notificationManager.getNotificationChannel(INITIATION_NOTIFY_CHANNEL_ID) == null) {
            createNotificationChannel(INITIATION_NOTIFY_CHANNEL_ID);
        }

        String message = "배터리가 활성화 됩니다.";

        if (_message != null) {
            message = _message;
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), INITIATION_NOTIFY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("sleepBattery")
                .setContentText(message)
                .setChannelId(INITIATION_NOTIFY_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        notificationManager.notify(3, notification);
    }


    /**
     * Notification channel 생성 함수
     *
     * @param notificationChannelId 생성하려는 chennel의 고유 id 값
     */
    private void createNotificationChannel(String notificationChannelId) {


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(
                notificationChannelId, notificationChannelId,
                NotificationManager.IMPORTANCE_HIGH);

        notificationManager.createNotificationChannel(notificationChannel);
    }

    /**
     * 배터리 상태 정보 mapping하여 foreground service에 적용될 text
     *
     * @param status   상태 정보
     * @param capacity 용량 정보
     * @return 매핑 결과
     */
    private String getBattStatusText(String status, int capacity) {
        String text = "";
        switch (status) {
            case "green":
                text += "좋음 /";
                break;
            case "yellow":
                text += "보통 /";
                break;
            case "orange":
                text += "나쁨 /";
                break;
            case "red":
                text += "매우 나쁨 /";
                break;
        }

        return text + " " + capacity + "%";
    }


    /**
     * 현재 시간 정보에 대해 계산
     *
     * @return 현재 시간 정보
     */
    public static long getCurrentDateTime() {
        Date today = new Date();
        Date currentDate;
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "HH:mm:ss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);

        Log.d("TEST", "current time from Locale: " + formatter.format(today).toString());

        // csv 데이터 타임 스템프 기록
        Data.TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", currentLocale).format(today);

        try {
            currentDate = formatter.parse(formatter.format(today).toString());
            long currentTime = currentDate.getTime();
            Log.d("TEST", "current time :" + currentTime);
            return currentTime;
        } catch (ParseException e) {
        }

        return -1l;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackgroundTask extends AsyncTask<Integer, String, Integer> {

        long mySleepTimer = timeData.getLong("totalSleepTime", -1l);
        long currentDiff = -1l;             // 현재 시간과 기상 시간의 차이를 봄

        long totalUsageTime = -1l;         // 수면 준비 시간과 취침 시간에 휴대폰 사용량
        long usageTimeInSleepTime = -1l;    // 취침 시간에서의 휴대폰 사용량

        boolean isInitiated = false;
        boolean isStateChanged = false;


//        long totalUsageTime =  30l * 60 + 43; //test
//        long usageTimeInSleepTime = 30l * 60; // test

        @Override
        protected Integer doInBackground(Integer... integers) {
            long currentDateTime = -1l;     // 현재 시간 정보
            long totalSleepTime = -1l;      // 수면 시간 정보 (수면 시작 ~ 수면 끝 시간대)
            long preparationTime = -1l;     // 수면 준비 시간 (준비 시작 ~ 수면 시작 시간대)
            String prevStatus = "green";    // 이전 수면의 상태 기록
            int prevCapacity = 100;         // 이전 수면의 잔량 기록

            while (isCancelled() == false) {
                try {

                    // check 초기화
                    if (!timeData.getBoolean("isSet", true)) {
                        cancel(true);
                        break;
                    }

                    if (checkDisplayOn()){
                        Data.WINDOW_ON = "on";
                    }

                    Log.d("TEST", "running in background... | time: " + mySleepTimer);
                    currentDateTime = getCurrentDateTime();
                    totalSleepTime = timeData.getLong("totalSleepTime", -1l);
                    preparationTime = totalSleepTime + (60 * 60 * 1000);

//                    Log.d("TEST", "current time : " + currentDateTime/ (60 * 60 * 1000) % 24 + ":" + currentDateTime /  (60 * 1000) % 60 + ":" + currentDateTime /  (1000) % 60);

                    if (timeData.getLong("wakeTime", -1l) >= currentDateTime) {
                        currentDiff = timeData.getLong("wakeTime", -1l) - currentDateTime;
                    } else {
                        currentDiff = timeData.getLong("wakeTime", -1) + (24 * 60 * 60 * 1000) - currentDateTime;
                    }

                    Log.d("TEST", "current diff : " + currentDiff);
                    Log.d("TEST", "sleep time   : " + timeData.getLong("totalSleepTime", -1l));


                    if (currentDiff <= preparationTime) {
                        if (checkDisplayOn()) {
//                            Data.WINDOW_ON = "on";
                            totalUsageTime += 1;
                        }
                    } else {
                        totalUsageTime = 0;
                        usageTimeInSleepTime = 0;
                    }

                    if (totalSleepTime < currentDiff && currentDiff <= preparationTime) {
                        Log.d("TEST", "현재는 수면 준비 시간입니다. 전체 사용 시간: " + totalUsageTime
                                + " | 수면 시간에 사용 시간: " + usageTimeInSleepTime
                                + " | 상태: " + prevStatus
                                + " | 용량: " + prevCapacity);

                        Data.ACTION = "preparation";

                        if (!isInitiated) {
                            // 수면 배터리 활성화 시작 알림
                            Data.ALARM = "activated";
                            notifyBatteryInitiation("취침 준비 시간입니다.");
                            isInitiated = true;
                        }

                        if (!prevStatus.equals(checkBattStatus(totalUsageTime))) {
                            Data.ALARM = "quality";
                            notifyBatteryChange("당신의 수면 품질이 감소하였습니다.");
                        }
                        prevStatus = checkBattStatus(totalUsageTime);
                        updateNotification(prevStatus, 100);

                    } else if (currentDiff <= totalSleepTime) {

                        // 사용자의 시간 변동에 의하여 활성화 알림이 없는 경우, 알림
                        if (!isInitiated) {
                            Data.ALARM = "activated";
                            notifyBatteryInitiation(null);
                            isInitiated = true;
                        }

                        if (checkDisplayOn()) {
//                            Data.WINDOW_ON = "on";
                            usageTimeInSleepTime += 1;
                        }

                        if (!prevStatus.equals(checkBattStatus(totalUsageTime))
                                || prevCapacity != calcBattCapacity(usageTimeInSleepTime * 1000,
                                totalSleepTime, prevStatus, isStateChanged, prevCapacity)) {

                            // 배터리가 5% 다를때 마다 또는 배터리 상태가 변활할 때마다 알림
                            if (calcBattCapacity(usageTimeInSleepTime * 1000,
                                    totalSleepTime, prevStatus, isStateChanged, prevCapacity) % 5 == 0
                                    || !prevStatus.equals(checkBattStatus(totalUsageTime))) {

                                if(!prevStatus.equals(checkBattStatus(totalUsageTime)) && !checkBattStatus(totalUsageTime).equals("green"))
                                {
                                    Data.ALARM = "quality";
                                    notifyBatteryChange("수면의 품질이 감소하였습니다.");
                                }
                                if(calcBattCapacity(usageTimeInSleepTime * 1000,
                                        totalSleepTime, prevStatus, isStateChanged, prevCapacity) % 5 == 0){
                                    notifyBatteryChange("당신의 예상 스태미나는 " + calcBattCapacity(usageTimeInSleepTime * 1000, +
                                                                                    totalSleepTime, prevStatus, isStateChanged, prevCapacity)
                                            + "% 입니다.");
                                    if (Data.ALARM.equals("quality")){
                                        Data.ALARM = "quality and capacity";
                                    }else{
                                        Data.ALARM = "capacity";
                                    }
                                }
                            }
                            // 배터리의 상태가 변화될 때 새로운 그래프 연산을 위해 부울 변수 변화
                            if(!prevStatus.equals(checkBattStatus(totalUsageTime))){
                                isStateChanged = true;
                            }
                        }

                        prevStatus = checkBattStatus(totalUsageTime);
                        prevCapacity = calcBattCapacity(usageTimeInSleepTime * 1000,
                                totalSleepTime, prevStatus, isStateChanged, prevCapacity);

                        Log.d("TEST", "현재는 수면 시간입니다. 전체 사용 시간: " + totalUsageTime
                                + " | 수면 시간에 사용 시간: " + usageTimeInSleepTime
                                + " | 상태: " + prevStatus
                                + " | 용량: " + prevCapacity);
                        Data.ACTION = "sleep";

                        updateNotification(prevStatus, prevCapacity);

                    } else {
                        if (isInitiated) {
                            isInitiated = false;
                        }

                        Log.d("TEST", "현재는 활동 시간입니다. 전체 사용 시간: " + totalUsageTime
                                + " | 수면 시간에 사용 시간: " + usageTimeInSleepTime
                                + " | 상태: " + prevStatus
                                + " | 용량: " + prevCapacity);
                        Data.ACTION = "awake";

                        C = 100;
                        updateNotification(prevStatus, prevCapacity);
                    }

                    if (mySleepTimer > 0) {
                        mySleepTimer -= 1;
                    }

                    isStateChanged = false;
                    saveCSV(totalUsageTime, usageTimeInSleepTime, prevStatus, prevCapacity);
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
            return 0;
        }


        private boolean saveCSV(long _totalUsageTime, long _usageTimeInSleepTime, String _status, int _capacity) {
            Data.SLEEP_TIME = timeData.getInt("mySleepHour", -1) + ":" + timeData.getInt("mySleepMin", -1);
            Data.WAKE_TIME = timeData.getInt("myWakeHour", -1) + ":" + timeData.getInt("myWakeMin", -1);
            Data.TOTAL_USAGE_TIME = _totalUsageTime + "";
            Data.BEDTIME_USAGE_TIME = _usageTimeInSleepTime + "";
            Data.BATTERY_STATUS = _status;
            Data.BATTERY_PERCENTAGE = _capacity + "";
            CSV.writeCSV(null);
            changeCSVDefault();

            return true;
        }


        private boolean changeCSVDefault() {
            Data.TIMESTAMP = "";
            Data.SLEEP_TIME = "";
            Data.WAKE_TIME = "";
            Data.BATTERY_STATUS = "";
            Data.BATTERY_PERCENTAGE = "";
            Data.BEDTIME_USAGE_TIME = "";
            Data.TOTAL_USAGE_TIME = "";
            Data.WINDOW_ON = "off";
            Data.ALARM = "none";
            Data.ACTION = "none";

            return true;
        }


        /**
         * 스마트폰 화면이 켜져있는 것 판별
         *
         * @return 화면 켜져있는 경우 True
         */
        private boolean checkDisplayOn() {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            boolean result = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && powerManager.isInteractive()
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && powerManager.isScreenOn();
            return result;
        }

        /**
         * 배터리의 상태 (수면 퀄리티)를 계산하는 함수
         * 0  ~ 30분     : 좋음, green
         * 31 ~ 40분     : 보통, yellow
         * 41 ~ 50분     : 나쁨, orange
         * 51 ~ 60분 over: 매우 나쁨, red
         *
         * @param _usageTime 스마트폰 사용 시간 (초 * 1000)
         * @return 배터리의 상태 정보, string값
         */
        private String checkBattStatus(long _usageTime) {
            long usage_min = _usageTime / 60;

            if (0 <= usage_min && usage_min <= 30) {
                return "green";
            } else if (30 < usage_min && usage_min <= 40) {
                return "yellow";
            } else if (40 < usage_min && usage_min <= 50) {
                return "orange";
            } else {
                return "red";
            }
        }

        /**
         * 남은 배터리의 잔량을 계산하는 함수
         *
         * @param _usageTimeInSleepTime 취침 시간에 휴대폰 사용한 시간
         * @param _totalSleepTime       사용자 지정 전체 수면 시간
         * @param _status               배터리의 성능
         * @return 배터리의 잔량
         */
        private int calcBattCapacity(long _usageTimeInSleepTime, long _totalSleepTime, String _status, boolean _isStatusChanged, int _prevCapacity) {
            // 기울기 구하기
            // 기울기 = 100 / 전체 수면 시간 (소수)
            long h = _totalSleepTime / (60 * 60 * 1000) % 24;
            long m = _totalSleepTime / (60 * 1000) % 60;
            long s = _totalSleepTime / 1000 % 60;
            float slop = 100 / (h + ((float) m / 60) + ((float) s / (60 * 60)));

            // 수면 시간에 사용한 시간에 대해 계산
            long u_h = _usageTimeInSleepTime / (60 * 60 * 1000) % 24;
            long u_m = _usageTimeInSleepTime / (60 * 1000) % 60;
            long u_s = _usageTimeInSleepTime / 1000 % 60;
            float u_t = u_h + ((float) u_m / 60) + ((float) u_s / (60 * 60));

            float grad = 1; // 수면의 상태에 따른 가중치

            switch (_status) {
                case "yellow":
                    grad = 1.5F;
                    break;
                case "orange":
                    grad = 2.0F;
                    break;
                case "red":
                    grad = 2.5F;
                    break;
                default:
                    grad = 1.0F;
                    C = 100;
                    T = 0;
            }

            if(_isStatusChanged){
                // 상태 변화가 일어났기 때문에 그래프 수정이 일어난다
                C = (int)(_prevCapacity);
                T = u_t;
                Log.d("TEST", "상태 변화가 일어났습니다. 용량: " + C + "| 당시 시간: " + T);
            }

//            int returnVal = (int) (Math.ceil(capacity - grad * slop * u_t));
            int returnVal = (int) exp(grad * slop, u_t, T, C);

            if (returnVal < 0) {
                returnVal = 0;
            }else if (returnVal > 100){
                returnVal = 100;
            }

            return returnVal;
        }

        /**
         * 용량 계산을 위한 수식
         * @param slop  기울기
         * @param x     사용 시간 (취침 시간 내)
         * @param t     정해진 좌표 값 (c 잔량일 때 당시 t값)
         * @param c     정해진 좌표 값 (상태 변화 시 c의 값)
         * @return
         */
        private int exp(float slop,float x, float t, float c){
            return (int)(Math.ceil(-1 * slop * (x - t) + c));
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