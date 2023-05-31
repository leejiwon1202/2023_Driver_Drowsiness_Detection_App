package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;

import com.example.driver_drowsiness_detection_app.R;

public class DrowsyDetectActivity extends AppCompatActivity {
    private TextView textView;
    SharedPreferences pref;
    float avg_close, avg_r, avg_l, avg_m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_drowsy_detect);

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(view -> {
            Intent i = new Intent(DrowsyDetectActivity.this, SaveActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        avg_close = pref.getFloat("avg_close", 0.0f);
        avg_r = pref.getFloat("avg_r", 0.0f);
        avg_l = pref.getFloat("avg_l", 0.0f);
        avg_m = pref.getFloat("avg_m", 0.0f);
    }

//    Float[] values = new Float[]{
//                        0.15961f, 0.06411f, 0.08845f,
//                        0.18891f, 0.04623f, 0.06253f,
//                        0.23152f, 0.03558f, 0.03913f,
//                        0.36978f, 0.04687f, 0.00760f,
//                        0.29022f, 0.03412f, 0.02085f,
//                        0.71790f, 0.06672f, 0.08905f,
//                        0.68945f, 0.04798f, 0.06279f,
//                        0.64726f, 0.03656f, 0.03938f,
//                        0.58828f, 0.03485f, 0.02073f,
//                        0.50769f, 0.04746f, 0.00759f,
//                        0.19023f, 0.10525f, 0.10033f,
//                        0.24130f, 0.09618f, 0.06237f,
//                        0.28932f, 0.09443f, 0.05387f,
//                        0.34208f, 0.10165f, 0.06749f,
//                        0.29293f, 0.10826f, 0.05354f,
//                        0.24359f, 0.10942f, 0.06264f,
//                        0.68080f, 0.10754f, 0.10024f,
//                        0.63074f, 0.09529f, 0.06227f,
//                        0.58071f, 0.09230f, 0.05378f,
//                        0.52843f, 0.10302f, 0.06703f,
//                        0.57739f, 0.11018f, 0.05328f,
//                        0.62696f, 0.11104f, 0.06255f,
//                        0.43662f, 0.07848f, -0.0091f,
//                        0.43620f, 0.09463f, -0.0406f,
//                        0.43508f, 0.12370f, -0.1132f,
//                        0.43320f, 0.15996f, -0.1727f,
//                        0.32270f, 0.29692f, -0.0458f,
//                        0.36853f, 0.26237f, -0.1049f,
//                        0.43909f, 0.25491f, -0.1266f,
//                        0.50700f, 0.26304f, -0.1046f,
//                        0.54777f, 0.29746f, -0.0461f,
//                        0.50033f, 0.30872f, -0.1081f,
//                        0.43736f, 0.31285f, -0.1212f,
//                        0.37333f, 0.30814f, -0.1079f,
//                        0.08060f, 0.18637f, 0.35090f,
//                        0.09698f, 0.26986f, 0.29321f,
//                        0.43218f, 0.42622f, -0.0773f,
//                        0.75329f, 0.31789f, 0.24118f,
//                        0.78945f, 0.19031f, 0.35378f
//                };
}