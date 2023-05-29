package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.R;

public class InitializationActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(view -> {
            initilzation();

            Intent i = new Intent(InitializationActivity.this, DrowsyDetectActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void initilzation() {
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt("Initialization", 1);
        editor.apply();
    }
}