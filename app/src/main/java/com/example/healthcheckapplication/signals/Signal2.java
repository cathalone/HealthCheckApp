package com.example.healthcheckapplication.signals;

import androidx.annotation.NonNull;

public class Signal2<T> {
    private final T[] signalData;
    private final int signalLength;
    private String signalName = "SIGNAL";

    public Signal2(T[] signalData) {

        this.signalData = signalData;
        this.signalLength = signalData.length;

    }

    public Signal2(T[] signalData, String signalName) {

        this.signalData = signalData;
        this.signalLength = signalData.length;
        this.signalName = signalName;

    }

    public T[] getSignalData() {
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

    public Signal2<T> getSignalShifted(int range, Values shiftWith) {

        if (shiftWith != Values.SIGNAL) {

            return this.getSignalShifted(range, this.findValue(shiftWith));

        } else {
            T[] newSignal = (T[]) new Object[this.signalLength];

            for (int i = 0; i < this.signalLength; i++) {
                if (i + range >= this.signalLength) {
                    newSignal[i + range - this.signalLength] = this.signalData[i];
                } else if (i + range < 0) {
                    newSignal[i + range + this.signalLength] = this.signalData[i];
                } else {
                    newSignal[i + range] = this.signalData[i];
                }
            }
            return new Signal2<T>(newSignal);
        }

    }

    public Signal2<T> getSignalShifted(int range, T shiftWith) {
        T[] newSignal = (T[]) new Object[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {

            if (i + range >= this.signalLength) {
                newSignal[i + range - this.signalLength] = shiftWith;
            } else if (i + range < 0) {
                newSignal[i + range + this.signalLength] = shiftWith;
            } else {
                newSignal[i + range] = this.signalData[i];
            }

        }

        return new Signal2<T>(newSignal);
    }

    private String signalToString(int maxArrayLengthToPrint, String separator) {
        StringBuilder signalString = new StringBuilder();
        signalString.append("[");

        if (this.signalLength <= maxArrayLengthToPrint) {
            for (int i = 0; i < this.signalLength; i++) {
                signalString.append(this.signalData[i]);
                if (i < this.signalLength - 1) {
                    signalString.append(", ");
                }
            }
        } else {

            int numberOfStartElements = (maxArrayLengthToPrint - 1) / 2;
            int numberOfEndElements = maxArrayLengthToPrint - numberOfStartElements - 1;

            for (int i = 0; i < numberOfStartElements; i++) {
                signalString.append(this.signalData[i]);
                if (i < numberOfStartElements - 1) {
                    signalString.append(", ");
                } else {
                    signalString.append(" ");
                    signalString.append(separator);
                    signalString.append(" ");
                }
            }

            for (int i = 0; i < numberOfEndElements; i++) {
                signalString.append(this.signalData[i+(this.signalLength-numberOfEndElements)]);
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

        return String.format(TEMPLATE, this.signalToString(MAX_ARRAY_LENGTH_TO_PRINT, SEPARATOR),
                this.signalLength);
    }

    public T findValue(Values value) {
        switch (value) {
            default:
                return null;
        }
    }

}
