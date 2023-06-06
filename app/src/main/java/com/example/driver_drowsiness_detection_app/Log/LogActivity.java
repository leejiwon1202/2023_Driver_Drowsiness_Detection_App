package com.example.driver_drowsiness_detection_app.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.example.driver_drowsiness_detection_app.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class LogActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private int user_id;
    private String driving_data;
    private String drowsy_data;
    private ListView list;
    private ArrayList<HashMap<String, String>> drivingList;
    private static final String TAG_RESULTS = "webnautes";
    private static final String TAG_ID = "driving_id";
    private static final String TAG_S = "s_time";
    private static final String TAG_E = "e_time";
    private BarChart barChart;

    private PieChart pieChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        user_id = pref.getInt("user_id", -1);
        driving_data = pref.getString("Driving_data", "None");
        drowsy_data = pref.getString("Drowsy_data", "None");

        list = (ListView) findViewById(R.id.listView);
        drivingList = new ArrayList<HashMap<String, String>>();
        showList(driving_data);

        Button btn_list = findViewById(R.id.btn_list);
        btn_list.setOnClickListener(view -> {
            Intent i = new Intent(LogActivity.this, ListActivity.class);
            startActivity(i);
        });

        barChart = (BarChart) findViewById(R.id.chart1);
        invalidate();

        pieChart = (PieChart)findViewById(R.id.piechart);
        invalidate2();
    }
    protected void showList(String string_data) {
        try {
            JSONObject jsonObj = new JSONObject(string_data);
            JSONArray json_driving = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < 3; i++) {
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
                    LogActivity.this, drivingList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_S, TAG_E},
                    new int[]{R.id.driving_id, R.id.s_time, R.id.e_time}
            );

            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(LogActivity.this, DetailActivity.class);
                    i.putExtra("index", drivingList.get(position).get(TAG_ID));
                    startActivity(i);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void invalidate() {
        ArrayList<BarEntry> chart1 = new ArrayList<>();

        BarData barData = new BarData();

        //chart1.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.

        int[] arr = new int[24];
        try {
            JSONObject jsonObj = new JSONObject(drowsy_data);
            JSONArray json_drowsy = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < json_drowsy.length(); i++) {
                JSONObject c = json_drowsy.getJSONObject(i);
                String d_time = c.getString("drowsy_time");
                int hour = Integer.valueOf(d_time.split(" ")[1].split(":")[0]);
                Log.d("ChartTest", String.valueOf(hour));
                arr[hour]++;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0; i<24; i++) {
            chart1.add(new BarEntry(i, arr[i]));
        }

        BarDataSet barDataSet = new BarDataSet(chart1, "bardataset");
        barDataSet.setColor(Color.BLUE);
        barData.addDataSet(barDataSet);
        barChart.setData(barData);
        barChart.setDrawGridBackground(false);
        barChart.invalidate();
        barChart.setTouchEnabled(false);

        YAxis yAxis;
        yAxis = barChart.getAxisLeft();
        barChart.getAxisRight().setEnabled(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(false);

        XAxis xAxis;
        xAxis = barChart.getXAxis();
        barChart.getAxisRight().setEnabled(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
    }

    private void invalidate2() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.BLACK);
        pieChart.setTransparentCircleRadius(61f);

        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();

        int[] arr = new int[9];
        try {
            JSONObject jsonObj = new JSONObject(drowsy_data);
            JSONArray json_drowsy = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < json_drowsy.length(); i++) {
                JSONObject c = json_drowsy.getJSONObject(i);
                String e_time = c.getString("elapsed_time");
                int minute = Integer.valueOf(e_time);
                int idx = (int) minute / 30;
                arr[Math.min(idx, 8)]++;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0; i<9; i++) {
            yValues.add(new PieEntry((float) arr[i], String.format("%d ~ %d", i*30, (i+1) * 30)));
        }

        Description description = new Description();
        description.setText("운전 경과 시간"); //라벨
        description.setTextSize(15);
        pieChart.setDescription(description);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

        PieDataSet dataSet = new PieDataSet(yValues,"");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);

        //pieChart.invalidate(); // 회전 및 터치 효과 사라짐
        //pieChart.setTouchEnabled(false);

        pieChart.setData(data);
    }
}