package com.example.driver_drowsiness_detection_app.Setting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.driver_drowsiness_detection_app.R;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

    }

    class InsertData extends AsyncTask<String,Void,String> { // 통신을 위한 InsertData 생성
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SettingActivity.this,"Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss(); //onPostExcute 에 오게되면 진행 다이얼로그 취소
            // Toast.makeText(SettingActivity.this, result, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String) params[0];
            String username = (String)params[1];
            String postParameters ="user_name="+username;

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST"); //요청 방식을 POST로 한다.
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                Log.d("postParameters 데이터 : ",postParameters);
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode(); //응답을 읽는다.

                InputStream inputStream;
                if(responseStatusCode == httpURLConnection.HTTP_OK){
                    inputStream=httpURLConnection.getInputStream();
                    Log.d("php정상: ","정상적으로 출력");
                }
                else {
                    inputStream = httpURLConnection.getErrorStream();
                    Log.d("php비정상: ","비정상적으로 출력");
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) !=null ) {
                    sb.append(line);
                }
                bufferedReader.close();

                Log.d("php 값 :", sb.toString());
                return  sb.toString();
            }
            catch (Exception e) {
                return  new String("Error " + e.getMessage());
            }
        }
    }
}