package com.example.healthcheckapplication.signals;

import androidx.annotation.NonNull;

public class BinarySignal {
    private final boolean[] signalData;
    private final int signalLength;
    private String signalName = "SIGNAL";

    public BinarySignal(boolean[] signalData) {

        this.signalData = signalData;
        this.signalLength = signalData.length;

    }

    public BinarySignal(boolean[] signalData, String signalName) {

        this.signalData = signalData;
        this.signalLength = signalData.length;
        this.signalName = signalName;

    }

    public boolean[] getSignalData() {
        return signalData;
    }

    public int getSignalLength() {
        return signalLength;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public BinarySignal getSignalShifted(int range, Values shiftWith) {

        if (shiftWith != Values.SIGNAL) {

            return this.getSignalShifted(range, this.findValue(shiftWith));

        } else {

            boolean[] newSignal = new boolean[this.signalLength];

            for (int i = 0; i < this.signalLength; i++) {
                if (i + range >= this.signalLength) {
                    newSignal[i + range - this.signalLength] = this.signalData[i];
                } else if (i + range < 0) {
                    newSignal[i + range + this.signalLength] = this.signalData[i];
                } else {
                    newSignal[i + range] = this.signalData[i];
                }
            }
            return new BinarySignal(newSignal);
        }

    }

    public BinarySignal getSignalShifted(int range, boolean shiftWith) {
        boolean[] newSignal = new boolean[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {

            if (i + range >= this.signalLength) {
                newSignal[i + range - this.signalLength] = shiftWith;
            } else if (i + range < 0) {
                newSignal[i + range + this.signalLength] = shiftWith;
            } else {
                newSignal[i + range] = this.signalData[i];
            }

        }

        return new BinarySignal(newSignal);
    }

    public Signal getSignalDistancesBetweenUnits() {

        int numOfUnits = 0;
        for (boolean element : this.signalData) {
            if (element) {
                numOfUnits += 1;
            }
        }

        double[] distancesSignal = new double[numOfUnits-1];
        int currentUnitIndex = 0;

        for (int i = 0; i < this.signalLength; i++) {
            if (this.signalData[i]) {
                currentUnitIndex += 1;
            }

            if (currentUnitIndex > 0 && currentUnitIndex < numOfUnits) {
                distancesSignal[currentUnitIndex-1] += 1;
            }
        }

        return new Signal(distancesSignal);
    }

    public double findAggregatedDistancesBetweenUnits(Values distanceAggregation) {
        return this.getSignalDistancesBetweenUnits().findValue(distanceAggregation);
    }

    public Signal toNumericalSignal() {
        double[] newSignal = new double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {
            if (this.signalData[i]) {
                newSignal[i] = 1;
            }
        }

        return new Signal(newSignal);
    }

    public BinarySignal logicAndSignal(BinarySignal other) {
        boolean[] newSignal = new boolean[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {
            if (this.signalData[i] && other.signalData[i]) {
                newSignal[i] = true;
            }
        }

        return new BinarySignal(newSignal);
    }

    private static String signalToString(boolean[] signal, int maxArrayLenthToPrint, String separator) {
        int signalLength = signal.length;

        StringBuilder signalString = new StringBuilder();
        signalString.append("[");

        if (signalLength <= maxArrayLenthToPrint) {
            for (int i = 0; i < signalLength; i++) {
                signalString.append(signal[i]);
                if (i < signalLength - 1) {
                    signalString.append(", ");
                }
            }
        } else {

            int numberOfStartElements = (maxArrayLenthToPrint - 1) / 2;
            int numberOfEndElements = maxArrayLenthToPrint - numberOfStartElements - 1;

            for (int i = 0; i < numberOfStartElements; i++) {
                signalString.append(signal[i]);
                if (i < numberOfStartElements - 1) {
                    signalString.append(", ");
                } else {
                    signalString.append(" ");
                    signalString.append(separator);
                    signalString.append(" ");
                }
            }

            for (int i = 0; i < numberOfEndElements; i++) {
                signalString.append(signal[i+(signalLength-numberOfEndElements)]);
                if (i < numberOfEndElements - 1) {
                    signalString.append(", ");
                }
            }
        }

        signalString.append("]");

        return signalString.toString();
    }

    @NonNull
    @Override
    public String toString() {
        final int MAX_ARRAY_LENGTH_TO_PRINT = 7;
        final String SEPARATOR = "...";
        final String TEMPLATE = "SIGNAL: %s \nLENGTH: %s \n";

        return String.format(TEMPLATE, signalToString(this.signalData, MAX_ARRAY_LENGTH_TO_PRINT, SEPARATOR),
                this.signalLength);
    }

    public boolean findValue(Values value) {
        switch (value) {
            default:
                return false;
        }
    }

}
