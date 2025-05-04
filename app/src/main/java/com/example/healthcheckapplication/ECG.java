package com.example.healthcheckapplication;

import com.example.healthcheckapplication.signals.Signal;

public class ECG {

    private Signal signal;
    private final int refreshRate;
    private final int ECGDurationInMillis;
    private final int sensorUpdateTiming;
    private final int signalLength;

    public ECG(int refreshRate, int ECGDurationInMillis) {
        this.refreshRate = refreshRate;
        this.ECGDurationInMillis = ECGDurationInMillis;
        this.sensorUpdateTiming = calculateTiming(refreshRate);
        this.signalLength = calculateSignalLength(this.sensorUpdateTiming, ECGDurationInMillis);
    }

    public Signal getSignal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public int getSignalLength() {
        return this.signalLength;
    }

    public int getSensorUpdateTiming() {
        return this.sensorUpdateTiming;
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