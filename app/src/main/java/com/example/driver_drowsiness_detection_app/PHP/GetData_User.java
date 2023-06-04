package com.example.driver_drowsiness_detection_app.PHP;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetData_User extends AsyncTask<String,Void,String>  {
    private String mJsonString;
    private String findName;
    private int findID;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            mJsonString = result;
            showResult();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String serverURL = params[0];
        String postParameters = params[1];
        findName = params[1];

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
            return null;
        }
    }
    public String getmJsonString() {
        return mJsonString;
    }
    private void showResult(){
        String TAG_JSON="webnautes";
        String TAG_USER_ID = "user_id";
        String TAG_USER_NAME = "user_name";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){
                JSONObject item = jsonArray.getJSONObject(i);

                String user_id = item.getString(TAG_USER_ID);
                String user_name = item.getString(TAG_USER_NAME);

//                if(user_name.equals(findName)){
//                    findID = Integer.valueOf(user_id);
//                    Log.d("StartActivityTest", String.valueOf(findID) + " " + user_name);
//                    break;
//                }
            }
        } catch (JSONException e) {
        }
    }
    public int getFindID() {
        return findID;
    }
}
