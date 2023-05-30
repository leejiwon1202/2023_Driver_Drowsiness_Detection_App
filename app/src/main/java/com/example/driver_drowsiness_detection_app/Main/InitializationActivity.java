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
    private long beforeTime;

    private int[] indexAll = new int[] {
            46, 53, 52, 55, 65, 276, 283, 282, 295, 285,
            130, 160, 158, 133, 153, 144,
            359, 387, 385, 362, 380, 373,
            168, 6, 195, 4, 61, 39, 0, 269, 291, 405, 17, 181, 234, 132, 152, 288, 454};
    private int[] indexER = new int[] {130, 160, 158, 133, 153, 144};
    private int[] indexEL = new int[] {359, 387, 385, 362, 380, 373};
    private int[] indexM = new int[] {61, 0, 291, 17};
    private double sum_r, sum_l, sum_m;
    private double avg_r, avg_l, avg_m;
    private long cnt;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        frameLayout = findViewById(R.id.display_layout);

        showCustomDialog();
    }
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InitializationActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(InitializationActivity.this).inflate(R.layout.layout_green_dialog, (LinearLayout)findViewById(R.id.layoutDialog));
        builder.setView(view);

        ((TextView) view.findViewById(R.id.textTitle)).setText("Title");
        ((TextView) view.findViewById(R.id.textMessage)).setText("text text text text");
        ((Button) view.findViewById(R.id.btnOk)).setText("OK~");

        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            setupLiveDemoUiComponents();
            alertDialog.dismiss();
        });
        alertDialog.show();

        beforeTime = System.currentTimeMillis();
        sum_r = 0.0;
        sum_l = 0.0;
        sum_m = 0.0;
        cnt = 0;
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

        if(System.currentTimeMillis() - beforeTime < 60000) {
            List<LandmarkProto.NormalizedLandmark> LandmarkList =  result.multiFaceLandmarks().get(0).getLandmarkList();

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
        else {
            avg_r = sum_r / cnt;
            avg_l = sum_l / cnt;
            avg_m = sum_m / cnt;

            Log.i(TAG, String.format("sum_r =%f / avg_r =%f", sum_r, avg_r));
            Log.i(TAG, String.format("sum_l =%f / avg_l =%f", sum_l, avg_l));
            Log.i(TAG, String.format("sum_m =%f / avg_m =%f", sum_m, avg_m));

            initilzation();
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




    private void initilzation() {
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt("Initialization", 1);
        editor.apply();
    }
    private void saveUserData() {

        goNextActivity();
    }
    private void goNextActivity() {

    }
}