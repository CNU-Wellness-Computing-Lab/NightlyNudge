package com.cnuhci.nightlynudge;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.cnuhci.nightlynudge.service.ForegroundService;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SleepSettingActivity extends AppCompatActivity {
    private Intent serviceIntent;
    final int PERMISSION_REQUEST_CODE = 100;

    public static SharedPreferences timeData;

    //컴포넌트 선언
    private TimePicker sleepTimePicker;
    private TimePicker wakeTimePicker;
    private Button saveBtn;
    private Button cancelBtn;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // night mode 차단

        setContentView(R.layout.activity_sleep_setting);

        // 컴포넌트 선언
        sleepTimePicker = findViewById(R.id.sleepTimePicker);
        wakeTimePicker = findViewById(R.id.wakeTimePicker);
        timeTextView = findViewById(R.id.timeTextView);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);


        // 캐시 데이터 불러오기
        timeData = getSharedPreferences("timeData", MODE_PRIVATE);

        if(timeData.getInt("myWakeHour", -1) != -1){
            // 저장되어있는 취침 시각 load
            loadTime();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 알람 권한 설정
            checkPermission();
        }

        // 저장 버튼에 대한 event 등록
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "starting foreground service...", Toast.LENGTH_LONG).show();
                setSleepTime(); // 취침 시각 설정
                startForegroundService();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "초기화 완료", Toast.LENGTH_LONG).show();
                eraseSetting();
            }
        });
    }

    private void startForegroundService() {
        serviceIntent = new Intent(this, ForegroundService.class);
        startService(serviceIntent);
    }

    private boolean eraseSetting(){
        SharedPreferences.Editor editor = timeData.edit();
        serviceIntent = new Intent(this, ForegroundService.class);

        if (editor == null) {
            return false;
        }
        // 저장된 value 들 default 값으로 초기화
        editor.putInt("myWakeHour", -1);        // 지정된 기상시각 중 hour 초기화
        editor.putInt("myWakeMin", -1);         // 지정된 기상시각 중 minute 초기화
        editor.putInt("mySleepHour", -1);       // 지정된 수면시간 중 hour 초기화
        editor.putInt("mySleepMin", -1);        // 지정된 수면시간 중 minute 초기화
        editor.putBoolean("isSet", false);
        editor.apply();

        timeTextView.setText("설정된 시간이 없는 상태입니다.");

        stopService(serviceIntent);

        return true;
    }

    /**
     * 취침 시각을 설정
     * @return 설정 정상 시 True
     */
    private boolean setSleepTime(){
        SharedPreferences.Editor editor = timeData.edit();

        int sleepHour24 = sleepTimePicker.getHour();
        int sleepMin = sleepTimePicker.getMinute();

        int wakeHour24 = wakeTimePicker.getHour();
        int wakeMin = wakeTimePicker.getMinute();

        if(editor == null){
            return false;
        }

        calcSleepDuration(new int[]{sleepHour24, sleepMin}, new int[]{wakeHour24, wakeMin});

        //TimePicker 에서 설정된 시각 저장
        editor.putInt("mySleepHour", sleepHour24);  // 지정된 취침시간 중 hour 저장
        editor.putInt("mySleepMin", sleepMin);      // 지정된 취침시간 중 minute 저장
        editor.putInt("myWakeHour", wakeHour24);    // 지정된 기상시각 중 hour 저장
        editor.putInt("myWakeMin", wakeMin);        // 지정된 기상시각 중 minute 저장
        editor.putBoolean("isSet", true);
        editor.apply();

//        if (wakeMin < 10){
//            timeTextView.setText("매일 " + sleepHour24 + "시 0" + wakeMin +"분");
//        }else{
//            timeTextView.setText("매일 " + sleepHour24 + "시 " + wakeMin +"분");
//        }

        return true;
    }


    /**
     * 이전에 저장된 취침 시각을 케시 데이터에서 불러옴
     * @return 반환 정상 시 True
     */
    private boolean loadTime(){
        // 기상 시각 정보 load
        int wakeHour24 = timeData.getInt("myWakeHour", -1);
        int wakeMin = timeData.getInt("myWakeMin", -1);

        // 취침 시간 정보 load
        int sleepHour24 = timeData.getInt("mySleepHour", -1);
        int sleepMin = timeData.getInt("mySleepMin", -1);

        // 총 수면 시간 정보 load
        long totalHour = timeData.getLong("totalSleepHour", -1l);
        long totalMin = timeData.getLong("totalSleepMin", -1l);

        timeTextView.setText("총 수면 시간: " + totalHour + "시간 " + totalMin + "분");

//        if (wakeMin < 10){
//            timeTextView.setText("매일 " + wakeHour24 + "시 0" + wakeMin +"분");
//        }else{
//            timeTextView.setText("매일 " + wakeHour24 + "시 " + wakeMin +"분");
//        }

        sleepTimePicker.setHour(sleepHour24);
        sleepTimePicker.setMinute(sleepMin);

        wakeTimePicker.setHour(wakeHour24);
        wakeTimePicker.setMinute(wakeMin);

        return true;
    }

    /**
     * 총 수면 시간을 계산하는 함수
     * @param sleepArr [0]: 수면 hour, [1]: 수면 minute
     * @param wakeArr  [0]: 기상 hour, [1]: 기상 minute
     */
    private void calcSleepDuration(int[] sleepArr, int[] wakeArr) {
        SharedPreferences.Editor editor = timeData.edit();

        String wakeTime = wakeArr[0] + ":" + wakeArr[1];
        String sleepTime = sleepArr[0] + ":" + sleepArr[1];
        Date wakeDate;
        Date sleepDate;
        long diff = 0l;

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            sleepDate = format.parse(sleepTime); // 취침 시간 (시작 시간)
            wakeDate = format.parse(wakeTime); // 기상 시간 (종료 시간)

            if(wakeDate.getTime() >= sleepDate.getTime()) {
                diff = wakeDate.getTime() - sleepDate.getTime();
            } else{
                diff = wakeDate.getTime() + (24*60*60*1000) - sleepDate.getTime();
            }

            long totalHour =  diff / (60 * 60 * 1000) % 24;
            long totalMin = diff / (60 * 1000) % 60;

            editor.putLong("totalSleepTime", diff);
            editor.putLong("wakeTime", wakeDate.getTime());
            editor.putLong("sleepTime", sleepDate.getTime());
            editor.putLong("totalSleepHour", totalHour);
            editor.putLong("totalSleepMin", totalMin);
            editor.apply();

            Toast.makeText(this, "시간이 설정되었습니다", Toast.LENGTH_LONG).show();
            timeTextView.setText("총 수면 시간: " + totalHour + "시간 " + totalMin + "분");
        } catch (ParseException e){
        }
    }

    /**
     * 현재 시간이 취침 시간 내 인지 확인하는 메소드
     * @param sleepDate 설정된 취침 시각
     * @param wakeDate  설정된 기상 시각
     * @return
     */
//    private boolean isInSleepTime(Date sleepDate, Date wakeDate){
//        // 현재 HH : mm의 getTime
//        Calendar calendar = Calendar.getInstance();
//        try {
//            long currentTime = new SimpleDateFormat("HH:mm", new Locale("KOREAN", "KOREA"))
//                    .parse(calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)).getTime();
//
//            if( currentTime)
//
//        }catch (ParseException e){
//        }
//        return false;
//    }


    /**
     * Android 13 이후로 알람에 대해 권한 부여해야함
     * 이후 권한이 부여되어야 foreground 서비스 사용 가능
      * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "Permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
        return true;
    }
}
