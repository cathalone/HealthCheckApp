package com.example.healthcheckapplication.ecg;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.example.healthcheckapplication.activities.RotationSensorEventListener;
import com.example.healthcheckapplication.signals.AxisNumericalSignal;
import com.example.healthcheckapplication.signals.NumericalSignal;

public class WriteECGTask implements Runnable {

    private final ECG ecg;
    private final SensorManager sensorManager;

    private final Axes[] axes;

    public WriteECGTask(ECG ecg, Axes[] axes, SensorManager sensorManager) {
        this.ecg = ecg;
        this.sensorManager = sensorManager;
        this.axes = axes;
    }

    private static int axisMapping(Axes axes) {
        switch (axes) {
            case Y:
                return 1;
            case Z:
                return 2;
            default:
                return 0;
        }
    }

    private static double[] calculateAngelVelocity(Axes[] axes, float[] orientations, int sensorUpdateTiming) throws InterruptedException {

        double[] angleOld = new double[axes.length];
        double[] angleNew = new double[axes.length];
        double[] velocity = new double[axes.length];

        for (int i = 0; i < axes.length; i++) {
            angleOld[i] = orientations[axisMapping(axes[i])];
        }
        Thread.sleep(sensorUpdateTiming);
        for (int i = 0; i < axes.length; i++) {
            angleNew[i] = orientations[axisMapping(axes[i])];
        }

        for (int i = 0; i < axes.length; i++) {
            velocity[i] = (angleNew[i] - angleOld[i]) / sensorUpdateTiming;
        }

        return velocity;

//        if (axis == Axes.X || axis == Axes.Y || axis == Axes.Z) {
//
//            double angleOld = orientations[axisMapping(axis)];
//            Thread.sleep(sensorUpdateTiming);
//            double angleNew = orientations[axisMapping(axis)];
//
//            return (angleNew - angleOld) / sensorUpdateTiming;
//
//        } else {
//
//            double XAngleOld = orientations[0];
//            double YAngleOld = orientations[1];
//            double ZAngleOld = orientations[2];
//            Thread.sleep(sensorUpdateTiming);
//            double XAngleNew = orientations[0];
//            double YAngleNew = orientations[1];
//            double ZAngleNew = orientations[2];
//
//            double angelVelocityX = (XAngleNew - XAngleOld) / sensorUpdateTiming;
//            double angelVelocityY = (YAngleNew - YAngleOld) / sensorUpdateTiming;
//            double angelVelocityZ = (ZAngleNew - ZAngleOld) / sensorUpdateTiming;
//
//            return Math.sqrt(angelVelocityX * angelVelocityX + angelVelocityY * angelVelocityY + angelVelocityZ * angelVelocityZ);
//
//        }
    }

    private static void writeECG(Axes[] axes, ECG ecg, float[] orientations) throws InterruptedException {

        double[][] signals = new double[axes.length][ecg.getSignalsLength()];
        double[] currentElement;

        for (int i = 0; i < ecg.getSignalsLength(); i++) {
            currentElement = calculateAngelVelocity(axes, orientations, ecg.getSensorUpdateTiming());
            for (int j = 0; j < axes.length; j++) {
                signals[j][i] = currentElement[j];
            }
        }

        AxisNumericalSignal[] axisNumericalSignals = new AxisNumericalSignal[axes.length];
        for (int j = 0; j < axes.length; j++) {
            axisNumericalSignals[j] = new AxisNumericalSignal(signals[j], axes[j]);
        }

        ecg.setSignals(axisNumericalSignals);

    }

    @Override
    public void run() {

        try {

            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            RotationSensorEventListener sensorEventListener = new RotationSensorEventListener();

            float[] orientations = sensorEventListener.getOrientations();

            this.sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);

            Thread.sleep(this.ecg.getExtraTimeForSensorCalibratingInMillis());

            writeECG(this.axes, this.ecg, orientations);

            this.sensorManager.unregisterListener(sensorEventListener);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}