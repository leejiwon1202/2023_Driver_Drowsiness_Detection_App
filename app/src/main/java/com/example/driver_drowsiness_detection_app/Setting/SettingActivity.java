package com.example.driver_drowsiness_detection_app.Setting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;

import com.example.driver_drowsiness_detection_app.Main.DrowsyDetectActivity;
import com.example.driver_drowsiness_detection_app.Main.FaceMeshResultGlRenderer;
import com.example.driver_drowsiness_detection_app.Main.FaceMeshResultImageView;
import com.example.driver_drowsiness_detection_app.Main.InitializationActivity;
import com.example.driver_drowsiness_detection_app.R;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import org.tensorflow.lite.Interpreter;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity123";
    private FaceMesh facemesh;
    private static final boolean RUN_ON_GPU = true;
    private FaceMeshResultImageView imageView;
    private CameraInput cameraInput;
    private SolutionGlSurfaceView<FaceMeshResult> glSurfaceView;
    FrameLayout frameLayout;

    private int[] indexAll = new int[] {
            46, 53, 52, 55, 65, 276, 283, 282, 295, 285,
            130, 160, 158, 133, 153, 144,
            359, 387, 385, 362, 380, 373,
            168, 6, 195, 4, 61, 39, 0, 269, 291, 405, 17, 181, 234, 132, 152, 288, 454};
    private int[] indexER = new int[] {130, 160, 158, 133, 153, 144};
    private int[] indexEL = new int[] {359, 387, 385, 362, 380, 373};
    private int[] indexM = new int[] {61, 0, 291, 17};
    private double sum_r, sum_l, sum_m, avg_r, avg_l, avg_m;
    float avg_close;
    private long cnt, startTime, closeTime;
    private String mode;
    private ArrayList<Long> closeTimes;
    private boolean isRecording;
    private Interpreter interpreter;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    TextView tv;

    String message, cName, mode_e, mode_y, warning;
    int score1, score2, score3, score4;

    float time_close, avg_time, time_e, time_y;
    boolean flag_e, flag_y;
    ArrayList<Float> time_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        frameLayout = findViewById(R.id.display_layout);
        interpreter = getTfliteInterpreter("DrowsyDetect.tflite");
        tv = findViewById(R.id.txt_test);

        message = "";

        SharedPreferences pref;
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        avg_close = pref.getFloat("avg_close", 0.0f);
        avg_r = pref.getFloat("avg_r", 0.0f);
        avg_l = pref.getFloat("avg_l", 0.0f);
        avg_m = pref.getFloat("avg_m", 0.0f);

        // 1)
        cName = "Non-Drowsy";
        score1 = 0;

        // 2)
        mode_e = "Opened";
        warning = "";

        flag_e = true;
        time_close = 0;
        time_list = new ArrayList<Float>();

        score2 = 0;

        // 3)
        avg_time = 0.0f; ///
        score3 = 0;

        // 4)
        mode_y = "X";
        flag_y = false;
        score4 = 0;

        time_close = System.currentTimeMillis();
        time_e = System.currentTimeMillis();
        time_y = System.currentTimeMillis();

        setupLiveDemoUiComponents();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
        //glSurfaceView.post(this::startCamera);
        //glSurfaceView.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.setVisibility(View.GONE);
        cameraInput.close();
    }

    private void setupLiveDemoUiComponents() {
        stopCurrentPipeline();
        setupStreamingModePipeline();
    }
    private void setupStreamingModePipeline() {
        facemesh = new FaceMesh(this, FaceMeshOptions.builder().setStaticImageMode(false).setRefineLandmarks(true).setRunOnGpu(RUN_ON_GPU).build());
        facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));

        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));

        glSurfaceView = new SolutionGlSurfaceView<>(this, facemesh.getGlContext(), facemesh.getGlMajorVersion());
        glSurfaceView.setSolutionResultRenderer(new FaceMeshResultGlRenderer());
        glSurfaceView.setRenderInputImage(true);

        facemesh.setResultListener(faceMeshResult -> {
            RecordUserData(faceMeshResult, /*showPixelValues=*/ false);
            glSurfaceView.setRenderData(faceMeshResult);
            glSurfaceView.requestRender();
        });

        glSurfaceView.post(this::startCamera);

        imageView = new FaceMeshResultImageView(this);
        imageView.setVisibility(View.GONE);
        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(glSurfaceView);
        glSurfaceView.setVisibility(View.VISIBLE);
        frameLayout.requestLayout();
    }
    private void startCamera() {
        cameraInput.start(this, facemesh.getGlContext(), CameraInput.CameraFacing.FRONT,  glSurfaceView.getWidth(), glSurfaceView.getHeight());
    }
    private void stopCurrentPipeline() {
        if (cameraInput != null) {
            cameraInput.setNewFrameListener(null);
            cameraInput.close();
        }
        if (glSurfaceView != null) {
            glSurfaceView.setVisibility(View.GONE);
        }
        if (facemesh != null)
            facemesh.close();
    }

    private void RecordUserData(FaceMeshResult result, boolean showPixelValues) {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return;
        }
        // LandmarkProto.NormalizedLandmark noseLandmark = result.multiFaceLandmarks().get(0).getLandmarkList().get(1);
        List<LandmarkProto.NormalizedLandmark> LandmarkList =  result.multiFaceLandmarks().get(0).getLandmarkList();

        // 1)
        ByteBuffer input = ByteBuffer.allocateDirect(4 * 117).order(ByteOrder.nativeOrder());
        for (int i=0; i<indexAll.length; i++) {
            LandmarkProto.NormalizedLandmark landmark = LandmarkList.get(indexAll[i]);
            input.putFloat(landmark.getX());
            input.putFloat(landmark.getY());
            input.putFloat(landmark.getZ());
        }

        int bufferSize = 2 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);
        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();

        score1 = (int) Math.floor(probabilities.get(1)* 5) * 10;
        if (30 <= score1)
            cName = "Drowsy";
        else
            cName = "Non-Drowsy";

        // 2)
        double ear_r = calc_ear(LandmarkList, indexER);
        double ear_l = calc_ear(LandmarkList, indexEL);

        if ((ear_r < avg_r * 0.7) && (ear_l < avg_l * 0.7)) {
            if (mode_e == "Opened")
                time_close = System.currentTimeMillis();
            if (System.currentTimeMillis() - time_close >= 4)
                warning = "Wake Up!!!";
            else if (System.currentTimeMillis() - time_close >= 2 && flag_e) {
                score2 += 5;
                flag_e = false;
                mode_e = "Closed";
            } else {
                if (mode_e == "Closed") {
                    warning = "";
                    flag_e = true;
                    time_list.add(System.currentTimeMillis() - time_close);
                    mode_e = "Opened";
                }
            }
        }

        // 3)
        if (System.currentTimeMillis() - time_e >= 60){
            avg_time = getSum(time_list) / time_list.size();
            time_list = new ArrayList<Float>();
            time_e = System.currentTimeMillis();
            if (avg_time >= avg_close * 2)
                score3 = 10;
            else
                score3 = 0;
        }

        // 4)
        double width = calc_awm(LandmarkList);
        double ratio = width  / avg_m;

        if (ratio > 6 && System.currentTimeMillis() - time_y >= 2){
            flag_y = true;
            mode_y = "O";
        }
        else {
            if(flag_y) {
                flag_y = false;
                time_y = System.currentTimeMillis();
                score4 += 10;
                mode_y = "X";
            }
        }

        int total = score1 + score2 + score3 + score4;
        if (total >= 100)
            message = "I think the driver is drowsy";

        Log.d(TAG, cName + " / Eyes : " + String.valueOf(mode_e) + " / Avg : " + String.valueOf((int) avg_time * 1000) + " / Yawn : " + String.valueOf(mode_y));
        Log.d(TAG, "score1 : " + String.valueOf(score1) + " / score2 : " + String.valueOf((int) score2) + " / score3 : " + String.valueOf(score3) + " / score4 : " + String.valueOf(score4));
    }
    private float getSum(ArrayList<Float> arr) {
        float result = 0.0f;
        for(float x : arr)
            result += x;
        return result;
    }
    private double calc_ear(List<LandmarkProto.NormalizedLandmark> p_list, int[] idx) {
        double a = Math.pow((p_list.get(idx[0]).getX() - p_list.get(idx[3]).getX()), 2) * 10000 +
                Math.pow((p_list.get(idx[0]).getY() - p_list.get(idx[3]).getY()), 2) * 10000;

        double b = Math.pow((p_list.get(idx[1]).getX() - p_list.get(idx[5]).getX()), 2) * 10000 +
                Math.pow((p_list.get(idx[1]).getY() - p_list.get(idx[5]).getY()), 2) * 10000;

        double c = Math.pow((p_list.get(idx[2]).getX() - p_list.get(idx[4]).getX()), 2) * 10000 +
                Math.pow((p_list.get(idx[2]).getY() - p_list.get(idx[4]).getY()), 2) * 10000;

        return (b + c) / (2 * a);
    }
    private double calc_awm(List<LandmarkProto.NormalizedLandmark> p_list) {
        double a = Math.pow((p_list.get(indexM[0]).getX() - p_list.get(indexM[2]).getX()), 2) * 100 +
                Math.pow((p_list.get(indexM[0]).getY() - p_list.get(indexM[2]).getY()), 2) * 100;

        double b = Math.pow((p_list.get(indexM[1]).getX() - p_list.get(indexM[3]).getX()), 2) * 100 +
                Math.pow((p_list.get(indexM[1]).getY() - p_list.get(indexM[3]).getY()), 2) * 100;

        return a * b * Math.PI;
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(SettingActivity.this, modelPath));
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

}