package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;

import com.example.driver_drowsiness_detection_app.PHP.GetData_Driving;
import com.example.driver_drowsiness_detection_app.PHP.InsertData_Driving;
import com.example.driver_drowsiness_detection_app.PHP.InsertData_Drowsy;
import com.example.driver_drowsiness_detection_app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrowsyDetectActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "52.79.176.182";
    private SharedPreferences pref;
    private float avg_close, avg_r, avg_l, avg_m;
    private Interpreter interpreter;
    private int user_id, driving_id;
    private String s_time;
    private GetData_Driving getTask;
    private SimpleDateFormat format;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_drowsy_detect);

        getTask = new GetData_Driving();
        getTask.execute( "http://" + IP_ADDRESS + "/android_driving_select_php.php", "");

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        user_id = pref.getInt("user_id", -1);

        interpreter = getTfliteInterpreter("DrowsyDetect.tflite");

        Button btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(view -> {
            s_time = format.format(new Date());

            InsertData_Driving insTask = new InsertData_Driving();
            insTask.execute("http://"+IP_ADDRESS+"/android_driving_insert_php.php", String.valueOf(user_id), s_time, s_time);

            driving_id = getID(getTask.getmJsonString());
        });

        Button btn_stop = findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(view -> {
            InsertData_Driving insTask = new InsertData_Driving();
            insTask.execute("http://"+IP_ADDRESS+"/android_driving_update_php.php", String.valueOf(user_id), s_time, format.format(new Date()));

            //Intent i = new Intent(DrowsyDetectActivity.this, SaveActivity.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(i);
        });

        Button btn_drowsy = findViewById(R.id.btn_drowsy);
        btn_drowsy.setOnClickListener(view -> {
            int temp = 21;
            InsertData_Drowsy insTask = new InsertData_Drowsy();
            Log.d(("DrowsyDetectInsert "), String.valueOf(driving_id)+String.valueOf(user_id)+format.format(new Date())+String.valueOf(temp));
            insTask.execute("http://"+IP_ADDRESS+"/android_drowsy_insert_php.php", String.valueOf(driving_id), String.valueOf(user_id), format.format(new Date()), String.valueOf(temp));
        });

        avg_close = pref.getFloat("avg_close", 0.0f);
        avg_r = pref.getFloat("avg_r", 0.0f);
        avg_l = pref.getFloat("avg_l", 0.0f);
        avg_m = pref.getFloat("avg_m", 0.0f);
    }

    private boolean isDrowsy() {
        float[] inputs = new float[]{0.867472f, 0.957693f, 0.780214f, 0.594963f, 0.591247f, 0.900008f, 0.112998f, 0.297898f, 0.487909f, 0.439365f, 0.934108f, 0.078781f, 0.540245f, 0.443055f, 0.779837f, 0.364080f, 0.764681f, 0.152206f, 0.102818f, 0.937937f, 0.995813f, 0.502016f, 0.982011f, 0.991649f, 0.854641f, 0.677497f, 0.332676f, 0.007113f, 0.793076f, 0.210241f, 0.648541f, 0.635861f, 0.088794f, 0.047339f, 0.742066f, 0.886561f, 0.683194f, 0.906738f, 0.710377f, 0.077227f, 0.705649f, 0.817459f, 0.470080f, 0.680814f, 0.057251f, 0.577675f, 0.920323f, 0.974217f, 0.293278f, 0.550637f, 0.900114f, 0.604802f, 0.833485f, 0.017986f, 0.583661f, 0.232412f, 0.902437f, 0.768435f, 0.467879f, 0.056156f, 0.626132f, 0.635685f, 0.690674f, 0.566053f, 0.323473f, 0.342204f, 0.124710f, 0.792990f, 0.821292f, 0.113590f, 0.966676f, 0.658183f, 0.942617f, 0.374754f, 0.055238f, 0.485900f, 0.017242f, 0.615865f, 0.375297f, 0.145617f, 0.864069f, 0.106706f, 0.919611f, 0.235706f, 0.235095f, 0.739020f, 0.622990f, 0.258536f, 0.160082f, 0.673858f, 0.313847f, 0.571240f, 0.036692f, 0.526417f, 0.776149f, 0.337847f, 0.198942f, 0.082001f, 0.413588f, 0.028557f, 0.915082f, 0.797611f, 0.404119f, 0.692338f, 0.829679f, 0.386381f, 0.084491f, 0.330018f, 0.969385f, 0.611673f, 0.491474f, 0.140900f, 0.500174f, 0.238397f, 0.725098f, 0.333640f, 0.121161f, };
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 117).order(ByteOrder.nativeOrder());
        for (float f : inputs) {
            input.putFloat(f);
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());

        interpreter.run(input, modelOutput);

        modelOutput.rewind();

        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        //tv_output.setText(String.format("%1.4f",probabilities.get(0)) + " " + String.format("%1.4f",probabilities.get(1)));
        return true;
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(DrowsyDetectActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private int getID(String Json) {
        String TAG_JSON="webnautes";
        String TAG_DRIVING_ID = "driving_id";

        int nextID = -1;
        try {
            JSONObject jsonObject = new JSONObject(Json);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            int jsonSize = jsonArray.length();

            for(int i=0;i<jsonSize;i++){
                JSONObject item = jsonArray.getJSONObject(i);

                int item_id = item.getInt(TAG_DRIVING_ID);
                nextID = Math.max(item_id+1, nextID);
            }
        } catch (JSONException E) {}
        return nextID;
    }
}