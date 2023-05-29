package com.example.driver_drowsiness_detection_app.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.driver_drowsiness_detection_app.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent i = getIntent();
        int index = i.getIntExtra("index", -1);
        TextView test = findViewById(R.id.test);
        test.setText("detail " + String.valueOf(index));
    }
}