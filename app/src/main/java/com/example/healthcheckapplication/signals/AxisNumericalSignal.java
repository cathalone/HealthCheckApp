package com.example.healthcheckapplication.signals;

import com.example.healthcheckapplication.ecg.Axes;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

public class AxisNumericalSignal extends NumericalSignal {
    private final Axes axis;
    public AxisNumericalSignal(Double[] signalData, Axes axis) {
        super(signalData);
        this.axis = axis;
    }

    public AxisNumericalSignal(Double[] signalData, String signalName, Axes axis) {
        super(signalData, signalName);
        this.axis = axis;
    }

    public AxisNumericalSignal(Signal<Double> signal, Axes axis) {
        super(signal);
        this.axis = axis;
    }

    public AxisNumericalSignal(double[] signalData, Axes axis) {
        super(signalData);
        this.axis = axis;
    }

    public AxisNumericalSignal(double[] signalData, String signalName, Axes axis) {
        super(signalData, signalName);
        this.axis = axis;
    }

    public AxisNumericalSignal(NumericalSignal numericalSignal, Axes axis) {
        super(numericalSignal);
        this.axis = axis;
    }

    public Axes getAxis() {
        return axis;
    }

    public static NumericalSignal euclideanMetric(AxisNumericalSignal[] axisNumericalSignals) {
        int signalLength = axisNumericalSignals[0].signalLength;
        int axesLength = axisNumericalSignals.length;
        double[][] signalData = new double[axesLength][signalLength];
        double[] newSignalData = new double[signalLength];
        double tempSum;

        for (int j = 0; j < axesLength; j++) {
            signalData[j] = NumericalSignal.valueOf(axisNumericalSignals[j].getSignalData());
        }

        for (int i = 0; i < signalLength; i++) {
            tempSum = 0;
            for (int j = 0; j < axesLength; j++) {
                tempSum += pow(signalData[j][i], 2);
            }
            newSignalData[i] = sqrt(tempSum);
        }

        return new NumericalSignal(newSignalData);

    }

}
