package com.example.driver_drowsiness_detection_app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import android.widget.TextView;
import android.widget.Toast;

import com.example.driver_drowsiness_detection_app.tflite.Classifier;
import com.example.driver_drowsiness_detection_app.camera.CameraFragment;
import com.example.driver_drowsiness_detection_app.utils.YuvToRgbConverter;

import java.io.IOException;
import java.util.Locale;

public class DrowsyDetectActivity extends AppCompatActivity {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Classifier cls;
    private TextView textView;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private int sensorOrientation = 0;

    private Bitmap rgbFrameBitmap = null;

    private HandlerThread handlerThread;
    private Handler handler;

    private boolean isProcessingFrame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_drowsy_detect);

        textView = findViewById(R.id.textView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cls = new Classifier(this);
        try {
            cls.init();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if(checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setFragment();
        }
        else {
            requestPermissions(new String[]{CAMERA_PERMISSION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected synchronized void onDestroy() {
        cls.finish();
        super.onDestroy();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        handlerThread = new HandlerThread("InferenceThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
    }

    protected void setFragment() {
        // Size inputSize = cls.getModelInputSize();
        Size inputSize = new Size(720, 720);
        String cameraId = chooseCamera();

        if(inputSize.getWidth() > 0 && inputSize.getHeight() > 0 && !cameraId.isEmpty()) {
            Fragment fragment = CameraFragment.newInstance(
                    (size, rotation) -> {
                        previewWidth = size.getWidth();
                        previewHeight = size.getHeight();
                        sensorOrientation = rotation - getScreenOrientation();
                    },
                    reader->processImage(reader),
                    inputSize,
                    cameraId);

            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
        } else {
            Toast.makeText(this, "Can't find camera", Toast.LENGTH_SHORT).show();
        }
    }

    private String chooseCamera() {
        final CameraManager manager =
                (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return "";
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:  return 270;
            case Surface.ROTATION_180:  return 180;
            case Surface.ROTATION_90:   return 90;
            default:                    return 0;
        }
    }

    protected void processImage(ImageReader reader) {
        if (previewWidth == 0 || previewHeight == 0)
            return;

        if (rgbFrameBitmap == null)
            rgbFrameBitmap = Bitmap.createBitmap( previewWidth, previewHeight, Bitmap.Config.ARGB_8888);

        if (isProcessingFrame)
            return;

        isProcessingFrame = true;

        final Image image = reader.acquireLatestImage();
        if (image == null) {
            isProcessingFrame = false;
            return;
        }

        YuvToRgbConverter.yuvToRgb(this, image, rgbFrameBitmap);

        runInBackground(() -> {
            if (cls != null && cls.isInitialized()) {
                Float[] values = new Float[]{
                        0.15961f, 0.06411f, 0.08845f,
                        0.18891f, 0.04623f, 0.06253f,
                        0.23152f, 0.03558f, 0.03913f,
                        0.36978f, 0.04687f, 0.00760f,
                        0.29022f, 0.03412f, 0.02085f,
                        0.71790f, 0.06672f, 0.08905f,
                        0.68945f, 0.04798f, 0.06279f,
                        0.64726f, 0.03656f, 0.03938f,
                        0.58828f, 0.03485f, 0.02073f,
                        0.50769f, 0.04746f, 0.00759f,
                        0.19023f, 0.10525f, 0.10033f,
                        0.24130f, 0.09618f, 0.06237f,
                        0.28932f, 0.09443f, 0.05387f,
                        0.34208f, 0.10165f, 0.06749f,
                        0.29293f, 0.10826f, 0.05354f,
                        0.24359f, 0.10942f, 0.06264f,
                        0.68080f, 0.10754f, 0.10024f,
                        0.63074f, 0.09529f, 0.06227f,
                        0.58071f, 0.09230f, 0.05378f,
                        0.52843f, 0.10302f, 0.06703f,
                        0.57739f, 0.11018f, 0.05328f,
                        0.62696f, 0.11104f, 0.06255f,
                        0.43662f, 0.07848f, -0.0091f,
                        0.43620f, 0.09463f, -0.0406f,
                        0.43508f, 0.12370f, -0.1132f,
                        0.43320f, 0.15996f, -0.1727f,
                        0.32270f, 0.29692f, -0.0458f,
                        0.36853f, 0.26237f, -0.1049f,
                        0.43909f, 0.25491f, -0.1266f,
                        0.50700f, 0.26304f, -0.1046f,
                        0.54777f, 0.29746f, -0.0461f,
                        0.50033f, 0.30872f, -0.1081f,
                        0.43736f, 0.31285f, -0.1212f,
                        0.37333f, 0.30814f, -0.1079f,
                        0.08060f, 0.18637f, 0.35090f,
                        0.09698f, 0.26986f, 0.29321f,
                        0.43218f, 0.42622f, -0.0773f,
                        0.75329f, 0.31789f, 0.24118f,
                        0.78945f, 0.19031f, 0.35378f
                };

                Object[] coords = new Object[117];
                for(int i=0; i<117; i++){
                    coords[i] = values[i];
                }

                final Pair<String, Float> output = cls.classify(coords);
                Log.d("runInBackground", "output123");
                runOnUiThread(() -> {
                    String resultStr = String.format(Locale.ENGLISH,
                            "class : %s, prob : %.2f%%",
                            output.first, output.second * 100);
                    textView.setText(resultStr);
                });
            }
            image.close();
            isProcessingFrame = false;
        });

    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }
}