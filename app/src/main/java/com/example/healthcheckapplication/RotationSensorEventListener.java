package com.example.healthcheckapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationSensorEventListener implements SensorEventListener {

    private final float[] orientations;

    public RotationSensorEventListener() {
        this.orientations = new float[3];
    }

    public float[] getOrientations() {
        return orientations;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);
        SensorManager.getOrientation(remappedRotationMatrix, this.orientations);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
