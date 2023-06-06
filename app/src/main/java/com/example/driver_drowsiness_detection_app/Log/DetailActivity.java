package com.example.driver_drowsiness_detection_app.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.driver_drowsiness_detection_app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private String drowsy_data;
    private ArrayList<HashMap<String, String>>  drowsyList;
    private static final String TAG_RESULTS = "webnautes";
    private static final String TAG_ID = "driving_id";
    private static final String TAG_D = "drowsy_time";
    private static final String TAG_E = "elapsed_time";
    private ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent i = getIntent();
        String index = i.getStringExtra("index");

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        drowsy_data = pref.getString("Drowsy_data", "None");

        list = (ListView) findViewById(R.id.listView);
        drowsyList = new ArrayList<HashMap<String, String>>();
        showList(drowsy_data, Integer.valueOf(index));
    }

    private void showList(String string_data, int index) {
        try {
            JSONObject jsonObj = new JSONObject(string_data);
            JSONArray json_driving = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < json_driving.length(); i++) {
                JSONObject c = json_driving.getJSONObject(i);
                int driving_id = c.getInt(TAG_ID);

                if(driving_id == index){
                    String d_time = c.getString(TAG_D);
                    String e_time = c.getString(TAG_E);

                    HashMap<String, String> data = new HashMap<String, String>();

                    data.put(TAG_D, d_time);
                    data.put(TAG_E, e_time);

                    drowsyList.add(data);
                }
            }

            ListAdapter adapter = new SimpleAdapter(
                    DetailActivity.this, drowsyList, R.layout.list_item2,
                    new String[]{TAG_D, TAG_E},
                    new int[]{R.id.d_time, R.id.e_time}
            );

            list.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}