package com.example.driver_drowsiness_detection_app.PHP;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetData_Driving extends AsyncTask<String,Void,String>  {
    private String mJsonString;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mJsonString = "";
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            mJsonString = result;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String serverURL = params[0];
        String user_id = (String)params[1];
        String postParameters ="user_id="+user_id;

        try {
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseStatusCode = httpURLConnection.getResponseCode();
            InputStream inputStream;
            if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
            else{
                inputStream = httpURLConnection.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            bufferedReader.close();

            return sb.toString().trim();

        } catch (Exception e) {
            Log.d("PHPTest", e.getMessage());
            return null;
        }
    }
    public String getmJsonString() {
        return mJsonString;
    }
}
