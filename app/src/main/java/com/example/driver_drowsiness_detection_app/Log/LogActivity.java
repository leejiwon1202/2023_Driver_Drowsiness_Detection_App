package com.example.driver_drowsiness_detection_app.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.R;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Button listBtn = findViewById(R.id.listBtn);
        listBtn.setOnClickListener(view -> {
            Intent i = new Intent(LogActivity.this, ListActivity.class);
            startActivity(i);
        });

        Button detailBtn = findViewById(R.id.detailBtn);
        detailBtn.setOnClickListener(view -> {
            Intent i = new Intent(LogActivity.this, DetailActivity.class);
            i.putExtra("index", 1);
            startActivity(i);
        });
    }
}