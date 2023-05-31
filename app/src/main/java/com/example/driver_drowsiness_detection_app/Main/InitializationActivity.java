package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.driver_drowsiness_detection_app.R;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class InitializationActivity extends AppCompatActivity {
    private static final String TAG = "InitializationActivity";
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

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        frameLayout = findViewById(R.id.display_layout);

        setupLiveDemoUiComponents();
        showCustomDialog();
    }
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InitializationActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(InitializationActivity.this).inflate(R.layout.layout_green_dialog, (LinearLayout)findViewById(R.id.layoutDialog));
        builder.setView(view);

        ((TextView) view.findViewById(R.id.textTitle)).setText("초기화");
        ((TextView) view.findViewById(R.id.textMessage)).setText("지금부터 3분 동안 사용자의 얼굴 정보를 저장합니다.");
        ((Button) view.findViewById(R.id.btnOk)).setText("OK~");

        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            alertDialog.dismiss();
            sum_r = 0.0;
            sum_l = 0.0;
            sum_m = 0.0;
            cnt = 0;
        });
        alertDialog.show();
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

        if(cnt < 100) {
            double ear_r = calc_ear(LandmarkList, indexER);
            double ear_l = calc_ear(LandmarkList, indexEL);
            double ear_m = calc_awm(LandmarkList);

            sum_r += ear_r;
            sum_l += ear_l;
            sum_m += ear_m;
            cnt += 1;

            Log.i(TAG, String.format("ear_r =%f / sum_r =%f", ear_r, sum_r));
            Log.i(TAG, String.format("ear_l =%f / sum_l =%f", ear_l, sum_l));
            Log.i(TAG, String.format("ear_m =%f / sum_m =%f", ear_m, sum_m));
        }
        else if(cnt == 100) {
            avg_r = sum_r / cnt;
            avg_l = sum_l / cnt;
            avg_m = sum_m / cnt;

            Log.i(TAG, String.format("sum_r =%f / avg_r =%f", sum_r, avg_r));
            Log.i(TAG, String.format("sum_l =%f / avg_l =%f", sum_l, avg_l));
            Log.i(TAG, String.format("sum_m =%f / avg_m =%f", sum_m, avg_m));

            cnt += 1;

            startTime = System.currentTimeMillis();
            closeTime = 0;
            closeTimes = new ArrayList<Long>();
            mode = "Opened";
            isRecording = true;
        }
        else if(System.currentTimeMillis() - startTime < 30000) {
            double ear_r = calc_ear(LandmarkList, indexER);
            double ear_l = calc_ear(LandmarkList, indexEL);

            if(ear_r < avg_r * 0.7 && ear_l < avg_l * 0.7) {
                if(mode.equals("Opened"))
                    closeTime = System.currentTimeMillis();
                mode = "Closed";
            }
            else {
                if(mode.equals("Closed"))
                    closeTimes.add(System.currentTimeMillis() - closeTime);
                mode = "Opened";
            }

            Log.i(TAG, "CurrentMode : " + mode);
        }
        else if(isRecording) {
            isRecording = false;
            saveUserData();
        }
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

    private void saveUserData() {
        long total = 0;
        for(long cTime : closeTimes)
            total += cTime;
        avg_close = (total / closeTimes.size()) / 1000.0f;
        Log.i(TAG, String.format("Initilzation avg_close =%f", avg_close));

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt("Initialization", 1);
        editor.putFloat("avg_close", avg_close);
        editor.putFloat("avg_r", (float) avg_r);
        editor.putFloat("avg_l", (float) avg_l);
        editor.putFloat("avg_m", (float) avg_m);
        editor.apply();

        goNextActivity();
    }
    private void goNextActivity() {
        Intent i = new Intent(InitializationActivity.this, DrowsyDetectActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}