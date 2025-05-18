package com.example.healthcheckapplication.signals;

import com.example.healthcheckapplication.ecg.Axes;

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

}
