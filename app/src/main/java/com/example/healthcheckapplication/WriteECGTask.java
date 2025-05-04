package com.example.healthcheckapplication;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.example.healthcheckapplication.signals.NumericalSignal;

public class WriteECGTask implements Runnable {

    private final ECG ecg;
    private final SensorManager sensorManager;

    public WriteECGTask(ECG ecg, SensorManager sensorManager) {
        this.ecg = ecg;
        this.sensorManager = sensorManager;
    }

    private static int axisMapping(Axis axis) {
        switch (axis) {
            case Y:
                return 1;
            case Z:
                return 2;
            default:
                return 0;
        }
    }

    private static double calculateAngelVelocity(Axis axis, float[] orientations, int sensorUpdateTiming) throws InterruptedException {

        if (axis == Axis.X || axis == Axis.Y || axis == Axis.Z) {

            double angleOld = orientations[axisMapping(axis)];
            Thread.sleep(sensorUpdateTiming);
            double angleNew = orientations[axisMapping(axis)];

            return (angleNew - angleOld) / sensorUpdateTiming;

        } else {

            double XAngleOld = orientations[0];
            double YAngleOld = orientations[1];
            double ZAngleOld = orientations[2];
            Thread.sleep(sensorUpdateTiming);
            double XAngleNew = orientations[0];
            double YAngleNew = orientations[1];
            double ZAngleNew = orientations[2];

            double angelVelocityX = (XAngleNew - XAngleOld) / sensorUpdateTiming;
            double angelVelocityY = (YAngleNew - YAngleOld) / sensorUpdateTiming;
            double angelVelocityZ = (ZAngleNew - ZAngleOld) / sensorUpdateTiming;

            return Math.sqrt(angelVelocityX * angelVelocityX + angelVelocityY * angelVelocityY + angelVelocityZ * angelVelocityZ);

        }
    }

    private static void writeECG(ECG ecg, float[] orientations) throws InterruptedException {

        double[] signal = new double[ecg.getSignalLength()];

        for (int i = 0; i < ecg.getSignalLength(); i++) {
            signal[i] = calculateAngelVelocity(Axis.XYZ, orientations, ecg.getSensorUpdateTiming());
        }

        ecg.setSignal(new NumericalSignal(signal));

    }

    @Override
    public void run() {

        try {

            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            RotationSensorEventListener sensorEventListener = new RotationSensorEventListener();

            float[] orientations = sensorEventListener.getOrientations();

            this.sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);

            writeECG(this.ecg, orientations);

            this.sensorManager.unregisterListener(sensorEventListener);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}