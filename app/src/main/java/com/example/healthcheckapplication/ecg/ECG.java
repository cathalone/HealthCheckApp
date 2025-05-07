package com.example.healthcheckapplication.ecg;

import com.example.healthcheckapplication.signals.NumericalSignal;

public class ECG {

    private final int extraTimeForSensorCalibratingInMillis;
    private NumericalSignal signal;
    private final int refreshRate;
    private final int ECGDurationInMillis;
    private final int sensorUpdateTiming;
    private final int signalLength;

    public ECG(int refreshRate, int ECGDurationInMillis) {
        this.refreshRate = refreshRate;
        this.ECGDurationInMillis = ECGDurationInMillis;
        this.sensorUpdateTiming = calculateTiming(refreshRate);
        this.signalLength = calculateSignalLength(this.sensorUpdateTiming, ECGDurationInMillis);
        this.extraTimeForSensorCalibratingInMillis = 500;
    }

    public ECG(int refreshRate, int ECGDurationInMillis, int extraTimeForSensorCalibratingInMillis) {
        this.refreshRate = refreshRate;
        this.ECGDurationInMillis = ECGDurationInMillis;
        this.sensorUpdateTiming = calculateTiming(refreshRate);
        this.signalLength = calculateSignalLength(this.sensorUpdateTiming, ECGDurationInMillis);
        this.extraTimeForSensorCalibratingInMillis = extraTimeForSensorCalibratingInMillis;
    }

    public NumericalSignal getSignal() {
        return signal;
    }

    public void setSignal(NumericalSignal signal) {
        this.signal = signal;
    }

    public int getSignalLength() {
        return this.signalLength;
    }

    public int getSensorUpdateTiming() {
        return this.sensorUpdateTiming;
    }

    public int getExtraTimeForSensorCalibratingInMillis() {
        return extraTimeForSensorCalibratingInMillis;
    }

    private static int calculateTiming(int refreshRate) {
        return 1000 / refreshRate;
    }

    private static int calculateSignalLength(int sensorUpdateTiming, int ECGDurationInMillis) {
        return ECGDurationInMillis / sensorUpdateTiming;
    }

    public static int calculatePulseFromExtremesDistance(double distance, int sensorUpdateTiming) {
        return (int) (60000 / (distance*sensorUpdateTiming));
    }


}