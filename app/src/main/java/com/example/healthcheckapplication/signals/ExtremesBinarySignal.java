package com.example.healthcheckapplication.signals;

public class ExtremesBinarySignal extends BinarySignal{

    public ExtremesBinarySignal(Boolean[] signalData) {
        super(signalData);
    }

    public ExtremesBinarySignal(Boolean[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public ExtremesBinarySignal(Signal<Boolean> signal) {
        super(signal);
    }

    public ExtremesBinarySignal(boolean[] signalData) {
        super(signalData);
    }

    public ExtremesBinarySignal(boolean[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public ExtremesBinarySignal(double[] signalData) {
        super(signalData);
    }

    public ExtremesBinarySignal(double[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public int findNumberOfExtremes() {
        int numberOfExtremes = 0;
        for (Boolean element : this.signalData) {
            if (element) {
                numberOfExtremes += 1;
            }
        }
        return numberOfExtremes;
    }

    public int[] findExtremesIndexes() {
        int[] extremesIndexes = new int[this.findNumberOfExtremes()];
        int currentExtremaIndex = 0;
        for (int i = 0; i < this.signalLength; i++) {
            if (this.signalData[i]) {
                extremesIndexes[currentExtremaIndex] = i;
                currentExtremaIndex++;
            }
        }
        return extremesIndexes;
    }

    public ExtremesBinarySignal logicAndSignal(ExtremesBinarySignal other) {
        return new ExtremesBinarySignal(super.logicAndSignal(other));
    }

}


