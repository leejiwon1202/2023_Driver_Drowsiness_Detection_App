package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.R;
import com.example.driver_drowsiness_detection_app.StartActivity;

public class ExitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        Button exitBtn =  findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(view -> {
            finish();
        });

        Button mainBtn =  findViewById(R.id.mainBtn);
        mainBtn.setOnClickListener(view -> {
            Intent i = new Intent(ExitActivity.this, StartActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}