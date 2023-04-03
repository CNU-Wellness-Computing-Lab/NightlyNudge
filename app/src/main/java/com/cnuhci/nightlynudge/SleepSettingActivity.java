package com.cnuhci.nightlynudge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.cnuhci.nightlynudge.service.ForegroundService;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cnuhci.nightlynudge.databinding.ActivitySleepSettingBinding;

public class SleepSettingActivity extends AppCompatActivity {
    Intent serviceIntent;
    final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_setting);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission();
        }

        Button button = findViewById(R.id.serviceBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "starting foreground service...", Toast.LENGTH_LONG).show();
                startForegroundService();
            }
        });
    }

    private void startForegroundService() {
        serviceIntent = new Intent(this, ForegroundService.class);
        startService(serviceIntent);
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
