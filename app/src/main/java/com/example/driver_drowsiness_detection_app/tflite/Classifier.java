package com.example.driver_drowsiness_detection_app.tflite;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.Size;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

public class Classifier {
    private static final String MODEL_NAME = "DrowsyDetect.tflite";
    private static final String LABEL_FILE = "labels.txt";

    Context context;
    Model model;
    int modelInputWidth, modelInputHeight, modelInputChannel;
    TensorBuffer outputBuffer;
    private List<String> labels;

    private boolean isInitialized = false;

    public Classifier(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        model = Model.createModel(context, MODEL_NAME);

        initModelShape();
        labels = FileUtil.loadLabels(context, LABEL_FILE);

        isInitialized = true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void initModelShape() {
        Tensor inputTensor = model.getInputTensor(0);
        int[] shape = inputTensor.shape();
        modelInputChannel = shape[0];
        modelInputWidth = shape[1];

        Tensor outputTensor = model.getOutputTensor(0);
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType());
    }

    public Size getModelInputSize() {
        if(!isInitialized)
            return new Size(0, 0);
        return new Size(modelInputWidth, modelInputHeight);
    }

    public Pair<String, Float> classify(Object[] coords) {
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        Log.d("classifyTest", String.valueOf(outputBuffer.getBuffer().rewind()));

        model.run(coords, outputs);

        Map<String, Float> output = new TensorLabel(labels, outputBuffer).getMapWithFloatValue();

        return new Pair("Drowsy", 80.0);
        //return argmax(output);
    }

    private Pair<String, Float> argmax(Map<String, Float> map) {
        String maxKey = "";
        float maxVal = -1;

        for(Map.Entry<String, Float> entry : map.entrySet()) {
            float f = entry.getValue();
            if(f > maxVal) {
                maxKey = entry.getKey();
                maxVal = f;
            }
        }

        return new Pair<>(maxKey, maxVal);
    }

    public void finish() {
        if(model != null) {
            model.close();
            isInitialized = false;
        }
    }
}
