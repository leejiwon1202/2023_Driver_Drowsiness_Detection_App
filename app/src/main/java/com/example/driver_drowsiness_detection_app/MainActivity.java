package com.example.driver_drowsiness_detection_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, DrowsyDetectActivity.class);
            startActivity(i);
        });

        Button logBtn = findViewById(R.id.logBtn);
        logBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, LogActivity.class);
            startActivity(i);
        });

        Button settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(i);
        });
    }
}