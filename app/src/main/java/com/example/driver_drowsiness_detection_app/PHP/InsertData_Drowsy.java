package com.example.driver_drowsiness_detection_app.PHP;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InsertData_Drowsy extends AsyncTask<String,Void,String> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
    @Override
    protected String doInBackground(String... params) {
        String serverURL = (String) params[0];
        String driving_id = (String)params[1];
        String user_id = (String)params[2];
        String drowsy_time = (String)params[3];
        String elapsed_time = (String)params[4];

        String postParameters ="driving_id="+driving_id+"&user_id="+user_id+"&drowsy_time="+drowsy_time+"&elapsed_time="+elapsed_time;

        try{
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST"); //요청 방식을 POST로 한다.
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseStatusCode = httpURLConnection.getResponseCode(); //응답을 읽는다.
            InputStream inputStream;
            if(responseStatusCode == httpURLConnection.HTTP_OK){
                inputStream=httpURLConnection.getInputStream();
            }
            else {
                inputStream = httpURLConnection.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) !=null ) {
                sb.append(line);
            }
            bufferedReader.close();

            return  sb.toString();
        }
        catch (Exception e) {
            return  new String("Error " + e.getMessage());
        }
    }
}