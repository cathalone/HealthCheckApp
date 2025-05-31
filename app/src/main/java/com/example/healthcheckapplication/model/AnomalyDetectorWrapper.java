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

    public float[] predict(float[] inputData) {
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
            return new float[0];
        }

        outputBuffer.rewind();
        float[] reconstructions = new float[modelInputSize];
        for (int i = 0; i < reconstructions.length; i++) {
            reconstructions[i] = outputBuffer.getFloat();
        }

        return reconstructions;
    }

    public boolean predictWithThreshold(float[] inputData) {
        float[] reconstructions = this.predict(inputData);
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
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            Log.i(TAG, "Interpreter closed.");
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
            Log.i(TAG, "GPU Delegate closed.");
        }
    }
}
