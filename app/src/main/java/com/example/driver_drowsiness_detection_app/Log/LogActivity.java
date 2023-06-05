package com.example.driver_drowsiness_detection_app.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.PHP.GetData_Driving;
import com.example.driver_drowsiness_detection_app.PHP.GetData_Drowsy;
import com.example.driver_drowsiness_detection_app.R;

public class LogActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "52.79.176.182";
    private SharedPreferences pref;
    private int user_id;
    private GetData_Driving getTask1;
    private GetData_Drowsy getTask2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        user_id = pref.getInt("user_id", -1);

        getTask1 = new GetData_Driving();
        getTask1.execute( "http://" + IP_ADDRESS + "/android_driving_select_php.php", String.valueOf(user_id));

        getTask2 = new GetData_Drowsy();
        getTask2.execute( "http://" + IP_ADDRESS + "/android_drowsy_select_php.php", String.valueOf(user_id));

        Button btn_list = findViewById(R.id.btn_list);
        btn_list.setOnClickListener(view -> {
            //Intent i = new Intent(LogActivity.this, ListActivity.class);
            //startActivity(i);

            Log.d("LogActivity getTask1", getTask1.getmJsonString());
            Log.d("LogActivity getTask2", getTask2.getmJsonString());
        });

        Button btn_detail = findViewById(R.id.btn_detail);
        btn_detail.setOnClickListener(view -> {
            Intent i = new Intent(LogActivity.this, DetailActivity.class);
            i.putExtra("index", 1);
            startActivity(i);
        });
    }
}