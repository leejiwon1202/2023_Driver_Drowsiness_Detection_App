package com.example.driver_drowsiness_detection_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.driver_drowsiness_detection_app.R;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

public class InitializationActivity extends AppCompatActivity {
    private static final String TAG = "InitializationActivity";
    private FaceMesh facemesh;
    private static final boolean RUN_ON_GPU = true;
    private FaceMeshResultImageView imageView;
    private CameraInput cameraInput;
    private SolutionGlSurfaceView<FaceMeshResult> glSurfaceView;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        setupLiveDemoUiComponents();

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(view -> {
            initilzation();
            Intent i = new Intent(InitializationActivity.this, DrowsyDetectActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void initilzation() {
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt("Initialization", 1);
        editor.apply();
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
        Button startCameraButton = findViewById(R.id.button_start_camera);
        startCameraButton.setOnClickListener(v -> {
            stopCurrentPipeline();
            setupStreamingModePipeline();
        });
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
            logNoseLandmark(faceMeshResult, /*showPixelValues=*/ false);
            glSurfaceView.setRenderData(faceMeshResult);
            glSurfaceView.requestRender();
        });

        glSurfaceView.post(this::startCamera);

        FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
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
    private void logNoseLandmark(FaceMeshResult result, boolean showPixelValues) {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.NormalizedLandmark noseLandmark = result.multiFaceLandmarks().get(0).getLandmarkList().get(1);

        if (showPixelValues) {
            int width = result.inputBitmap().getWidth();
            int height = result.inputBitmap().getHeight();
            Log.i(TAG, String.format("MediaPipe Face Mesh nose coordinates (pixel values): x=%f, y=%f", noseLandmark.getX() * width, noseLandmark.getY() * height));
        } else {
            Log.i(TAG, String.format("MediaPipe Face Mesh nose normalized coordinates (value range: [0, 1]): x=%f, y=%f", noseLandmark.getX(), noseLandmark.getY()));
        }
    }
}