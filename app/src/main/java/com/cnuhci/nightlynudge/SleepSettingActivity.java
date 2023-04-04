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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

public class SleepSettingActivity extends AppCompatActivity {
    private Intent serviceIntent;
    final int PERMISSION_REQUEST_CODE = 100;

    public static SharedPreferences timeData;

    //컴포넌트 선언
    private TimePicker timePicker;
    private Button settingBtn;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // night mode 차단

        setContentView(R.layout.activity_sleep_setting);

        // 컴포넌트 선언
        timePicker = findViewById(R.id.timePicker);
        timeTextView = findViewById(R.id.timeTextView);
        settingBtn = findViewById(R.id.serviceBtn);

        // 캐시 데이터 불러오기
        timeData = getSharedPreferences("timeData", MODE_PRIVATE);

        if(timeData.getInt("mySleepHour", -1) != -1){
            // 저장되어있는 취침 시각 load
            loadTime();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 알람 권한 설정
            checkPermission();
        }

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "starting foreground service...", Toast.LENGTH_LONG).show();
                setSleepTime(); // 취침 시각 설정
                startForegroundService();
            }
        });
    }

    private void startForegroundService() {
        serviceIntent = new Intent(this, ForegroundService.class);
        startService(serviceIntent);
    }

    /**
     * 취침 시각을 설정
     * @return 설정 정상 시 True
     */
    private boolean setSleepTime(){
        SharedPreferences.Editor editor = timeData.edit();

        int hour24 = timePicker.getHour();
        int minute = timePicker.getMinute();

        if(editor == null){
            return false;
        }

        //TimePicker 에서 설정된 시각 저장
        editor.putInt("mySleepHour", hour24);     // 지정된 Hour 저장
        editor.putInt("mySleepMin", minute);    // 지정된 Minute 저장
        editor.apply();

        if (minute < 10){
            timeTextView.setText("매일 " + hour24 + "시 0" + minute +"분");
        }else{
            timeTextView.setText("매일 " + hour24 + "시 " + minute +"분");
        }

        return true;
    }


    /**
     * 이전에 저장된 취침 시각을 케시 데이터에서 불러옴
     * @return 반환 정상 시 True
     */
    private boolean loadTime(){

        int hour24 = timeData.getInt("mySleepHour", -1);
        int minute = timeData.getInt("mySleepMin", -1);

        if (minute < 10){
            timeTextView.setText("매일 " + hour24 + "시 0" + minute +"분");
        }else{
            timeTextView.setText("매일 " + hour24 + "시 " + minute +"분");
        }

        return true;
    }


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
