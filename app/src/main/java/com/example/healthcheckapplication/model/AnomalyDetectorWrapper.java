package com.example.healthcheckapplication.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AnomalyDetectorWrapper {

    private static final String TAG = "AnomalyDetectorWrapper";
    private static final String MODEL_FILE_NAME = "anomaly_detector.tflite";

    private Interpreter interpreter;
    private final int modelInputSize;
    private final float threshold;

    private GpuDelegate gpuDelegate = null;

    public AnomalyDetectorWrapper(Context context, int inputSize, float detectionThreshold) throws IOException {
        this.modelInputSize = inputSize;
        this.threshold = detectionThreshold;

        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();


        options.setNumThreads(4); // Например, 4 потока CPU
        Log.i(TAG, "GPU Delegate not supported. Using CPU with " + options.getNumThreads() + " threads.");



        try {
            MappedByteBuffer modelBuffer = loadModelFile(context.getAssets(), MODEL_FILE_NAME);
            interpreter = new Interpreter(modelBuffer, options);
            Log.i(TAG, "TensorFlow Lite Interpreter initialized successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing TensorFlow Lite Interpreter.", e);
            throw e;
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // ... (код загрузки модели остается без изменений) ...
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        inputStream.close();
        fileDescriptor.close();
        return mappedByteBuffer;
    }

    public boolean predict(float[] inputData) {
        // ... (код предсказания остается без изменений) ...
        if (inputData == null || inputData.length != modelInputSize) {
            Log.e(TAG, "Invalid input data. Expected size: " + modelInputSize + ", Got: " + (inputData == null ? "null" : inputData.length));
            throw new IllegalArgumentException("Input data size " + (inputData == null ? "null" : inputData.length) +
                    " does not match model input size " + modelInputSize);
        }

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(modelInputSize * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float value : inputData) {
            inputBuffer.putFloat(value);
        }
        inputBuffer.rewind();

        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(modelInputSize * 4);
        outputBuffer.order(ByteOrder.nativeOrder());

        try {
            interpreter.run(inputBuffer, outputBuffer);
        } catch (Exception e) {
            Log.e(TAG, "Error during model inference.", e);
            return false;
        }

        outputBuffer.rewind();
        float[] reconstructions = new float[modelInputSize];
        for (int i = 0; i < reconstructions.length; i++) {
            reconstructions[i] = outputBuffer.getFloat();
        }

        float mse = 0.0f;
        for (int i = 0; i < inputData.length; i++) {
            float diff = reconstructions[i] - inputData[i];
            mse += diff * diff;
        }
        mse /= inputData.length;

        Log.d(TAG, "Calculated MSE: " + mse + ", Threshold: " + threshold);

        return mse < threshold;
    }

    public void close() {
        // ... (код закрытия остается без изменений) ...
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            Log.i(TAG, "Interpreter closed.");
        }
        if (gpuDelegate != null) {
            gpuDelegate.close(); // Важно закрывать делегат
            gpuDelegate = null;
            Log.i(TAG, "GPU Delegate closed.");
        }
    }
}
