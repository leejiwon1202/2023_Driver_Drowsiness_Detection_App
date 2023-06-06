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

import com.example.driver_drowsiness_detection_app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ListActivity extends AppCompatActivity {
    private static final String TAG_RESULTS = "webnautes";
    private static final String TAG_ID = "driving_id";
    private static final String TAG_S = "s_time";
    private static final String TAG_E = "e_time";
    private SharedPreferences pref;
    private String driving_data;
    ListView list;
    ArrayList<HashMap<String, String>> drivingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        driving_data = pref.getString("Driving_data", "None");

        list = (ListView) findViewById(R.id.listView);
        drivingList = new ArrayList<HashMap<String, String>>();
        showList(driving_data);
    }
    protected void showList(String string_data) {
        try {
            JSONObject jsonObj = new JSONObject(string_data);
            JSONArray json_driving = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < json_driving.length(); i++) {
                JSONObject c = json_driving.getJSONObject(i);
                String driving_id = c.getString(TAG_ID);
                String s_time = c.getString(TAG_S);
                String e_time = c.getString(TAG_E);

                HashMap<String, String> data = new HashMap<String, String>();

                data.put(TAG_ID, driving_id);
                data.put(TAG_S, s_time);
                data.put(TAG_E, e_time);

                drivingList.add(data);
            }

            ListAdapter adapter = new SimpleAdapter(
                    ListActivity.this, drivingList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_S, TAG_E},
                    new int[]{R.id.driving_id, R.id.s_time, R.id.e_time}
            );

            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override   // position 으로 몇번째 것이 선택됐는지 값을 넘겨준다
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(ListActivity.this, DetailActivity.class);
                    i.putExtra("index", drivingList.get(position).get(TAG_ID));
                    startActivity(i);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}