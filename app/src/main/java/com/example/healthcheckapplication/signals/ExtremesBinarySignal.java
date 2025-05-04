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

//    public Signal getExtremesSignalFiltratedByYDistance(Signal originalSignal) {
//        Double[] newSignal = new Double[this.signalLength];
//        Double sigma = originalSignal.signalSigma;
//
//        Double previousElement = 0;
//        int previousElementIndex = 0;
//        Double currentElement = 0;
//        int currentElementNumber = 0;
//
//        for (int i = 0; i < this.signalLength; i++) {
//            if (this.signal[i] == 1) {
//                currentElement = originalSignal.signal[i];
//                if (currentElementNumber > 0) {
//                    if (abs(previousElement - currentElement) > sigma) {
//                        previousElement = currentElement;
//                        newSignal[i] = 1;
//                    } else {
//                        continue;
//                    }
//                } else {
//                    previousElement = currentElement;
//                    newSignal[i] = 1;
//                }
//                currentElementNumber += 1;
//            }
//
//        }
//        return new Signal(newSignal);
//    }

}


