package com.example.driver_drowsiness_detection_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driver_drowsiness_detection_app.Log.LogActivity;
import com.example.driver_drowsiness_detection_app.Main.DrowsyDetectActivity;
import com.example.driver_drowsiness_detection_app.Main.InitializationActivity;
import com.example.driver_drowsiness_detection_app.PHP.GetData_Driving;
import com.example.driver_drowsiness_detection_app.PHP.GetData_Drowsy;
import com.example.driver_drowsiness_detection_app.PHP.GetData_User;
import com.example.driver_drowsiness_detection_app.PHP.InsertData_User;
import com.example.driver_drowsiness_detection_app.Setting.SettingActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StartActivity extends AppCompatActivity {
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    private int user_id;
    private String user_name;
    private static String IP_ADDRESS = "52.79.176.182";
    private GetData_User getTask;
    private GetData_Driving getTask1;
    private GetData_Drowsy getTask2;
    private TextView tv_welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 0
        getTask = new GetData_User();
        getTask.execute( "http://" + IP_ADDRESS + "/android_user_select_php.php", "");
        getTask1 = new GetData_Driving();
        getTask2 = new GetData_Drowsy();

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        user_id = pref.getInt("user_id", -1);
        user_name = pref.getString("user_name", "");

        tv_welcome = findViewById(R.id.tv_welcome);
//        if(user_id < 0) {
            showCustomDialog();
//        }
//        else {
//            getTask1.execute( "http://" + IP_ADDRESS + "/android_driving_select_php.php", String.valueOf(user_id));
//            getTask2.execute( "http://" + IP_ADDRESS + "/android_drowsy_select_php.php", String.valueOf(user_id));
//            tv_welcome.setText(String.format("반가워요 %s(#%d)님!", user_name, user_id));
//        }

        // 1
        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(view -> {
            int isFirst = pref.getInt("Initialization", 0);
            Intent i;
            //if (isFirst == 0)
                i = new Intent(StartActivity.this, InitializationActivity.class);
            //else
            //    i = new Intent(StartActivity.this, DrowsyDetectActivity.class);
            startActivity(i);
        });

        Button logBtn = findViewById(R.id.logBtn);
        logBtn.setOnClickListener(view -> {
            editor = pref.edit();
            editor.putString("Driving_data", getTask1.getmJsonString());
            editor.putString("Drowsy_data", getTask2.getmJsonString());
            editor.apply();

            Intent i = new Intent(StartActivity.this, LogActivity.class);
            startActivity(i);
        });

        Button settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(view -> {
            Intent i = new Intent(StartActivity.this, SettingActivity.class);
            startActivity(i);
        });
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(StartActivity.this).inflate(R.layout.layout_pink_dialog, (LinearLayout)findViewById(R.id.layoutDialog));
        builder.setView(view);

        ((TextView) view.findViewById(R.id.textTitle)).setText("사용하실 이름을 작성해 주세요.");
        ((Button) view.findViewById(R.id.btnOk)).setText("등록");
        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String input_name = ((TextView) view.findViewById(R.id.textMessage)).getText().toString().trim();
            if(input_name.length() >= 2){
                int nextID = getID(input_name);
                if (nextID != -1) {
                    InsertData_User insTask = new InsertData_User();
                    insTask.execute("http://"+IP_ADDRESS+"/android_user_insert_php.php", input_name);

                    editor = pref.edit();
                    editor.putInt("user_id", nextID);
                    editor.putString("user_name", input_name);
                    editor.apply();

                    tv_welcome.setText(String.format("반가워요 %s(#%d)님!", input_name, nextID));

                    getTask1.execute( "http://" + IP_ADDRESS + "/android_driving_select_php.php", String.valueOf(user_id));
                    getTask2.execute( "http://" + IP_ADDRESS + "/android_drowsy_select_php.php", String.valueOf(user_id));

                    Toast toast = Toast.makeText(StartActivity.this, "등록되었습니다.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0,300);
                    toast.show();

                    alertDialog.dismiss();
                }
                else {
                    Toast toast = Toast.makeText(StartActivity.this, "이미 존재하는 이름입니다.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
            else {
                Toast toast = Toast.makeText(StartActivity.this, "2자리 이상 입력해주세요.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    private int getID(String input_name) {
        String Json = getTask.getmJsonString();
        String TAG_JSON="webnautes";
        String TAG_USER_ID = "user_id";
        String TAG_USER_NAME = "user_name";

        int nextID = -1;
        try {
            JSONObject jsonObject = new JSONObject(Json);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            int jsonSize = jsonArray.length();

            for(int i=0;i<jsonSize;i++){
                JSONObject item = jsonArray.getJSONObject(i);

                int item_id = item.getInt(TAG_USER_ID);
                String item_name = item.getString(TAG_USER_NAME);
                nextID = Math.max(item_id+1, nextID);

                if(item_name.equals(input_name)){
                    return -1;
                }
            }
        } catch (JSONException E) {
        }
        return nextID;
    }
}