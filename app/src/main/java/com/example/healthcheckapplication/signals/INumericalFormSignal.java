package com.example.healthcheckapplication.signals;

public interface INumericalFormSignal {

    double[] getSignalAsDoubleArray();

    int getSignalLength();

    String getSignalName();

}
