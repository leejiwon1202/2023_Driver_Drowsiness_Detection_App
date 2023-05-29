package com.example.driver_drowsiness_detection_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.Log.LogActivity;
import com.example.driver_drowsiness_detection_app.Main.DrowsyDetectActivity;
import com.example.driver_drowsiness_detection_app.Main.InitializationActivity;
import com.example.driver_drowsiness_detection_app.Setting.SettingActivity;

public class StartActivity extends AppCompatActivity {
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 0
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        // 1
        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(view -> {
            int isFirst = pref.getInt("Initialization", 0);
            Intent i;
            if (isFirst == 0)
                i = new Intent(StartActivity.this, InitializationActivity.class);
            else
                i = new Intent(StartActivity.this, DrowsyDetectActivity.class);
            startActivity(i);
        });

        Button logBtn = findViewById(R.id.logBtn);
        logBtn.setOnClickListener(view -> {
            Intent i = new Intent(StartActivity.this, LogActivity.class);
            startActivity(i);
        });

        Button settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(view -> {
            Intent i = new Intent(StartActivity.this, SettingActivity.class);
            startActivity(i);
        });
    }
}