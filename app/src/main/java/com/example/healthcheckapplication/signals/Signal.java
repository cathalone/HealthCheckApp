package com.example.healthcheckapplication.signals;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Signal<T>{

    protected static int MAX_ARRAY_LENGTH_TO_PRINT = 20;
    protected static String SEPARATOR = "...";
    protected static String TEMPLATE_OF_ELEMENT = "%s";
    protected static String TEMPLATE = "SIGNAL: %s \nLENGTH: %s \n";

    protected final T[] signalData;
    protected final int signalLength;
    protected String signalName = "SIGNAL";

    public Signal(T[] signalData) {

        this.signalData = signalData;
        this.signalLength = signalData.length;

    }

    public Signal(T[] signalData, String signalName) {

        this(signalData);
        this.signalName = signalName;

    }

    public Signal(Signal<T> signal) {

        this(Arrays.copyOf(signal.getSignalData(), signal.getSignalLength()), signal.getSignalName());

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

    public Signal<T> getSignalShifted(int range, Values shiftWith) {

        if (shiftWith != Values.SIGNAL) {

            return this.getSignalShifted(range, this.findValue(shiftWith));

        } else {
            T[] newSignal = Arrays.copyOf(this.signalData, this.signalLength);

            for (int i = 0; i < this.signalLength; i++) {
                if (i + range >= this.signalLength) {
                    newSignal[i + range - this.signalLength] = this.signalData[i];
                } else if (i + range < 0) {
                    newSignal[i + range + this.signalLength] = this.signalData[i];
                } else {
                    newSignal[i + range] = this.signalData[i];
                }
            }
            return new Signal<>(newSignal);
        }

    }

    public Signal<T> getSignalShifted(int range, T shiftWith) {
        T[] newSignal = Arrays.copyOf(this.signalData, this.signalLength);

        for (int i = 0; i < this.signalLength; i++) {

            if (i + range >= this.signalLength) {
                newSignal[i + range - this.signalLength] = shiftWith;
            } else if (i + range < 0) {
                newSignal[i + range + this.signalLength] = shiftWith;
            } else {
                newSignal[i + range] = this.signalData[i];
            }

        }
        return new Signal<>(newSignal);
    }

    protected String signalToString(int maxArrayLengthToPrint, String templateOfElement, String separator) {
        StringBuilder signalString = new StringBuilder();
        signalString.append("[");

        if (this.signalLength <= maxArrayLengthToPrint) {
            for (int i = 0; i < this.signalLength; i++) {
                signalString.append(String.format(templateOfElement, this.signalData[i]));
                if (i < this.signalLength - 1) {
                    signalString.append(", ");
                }
            }
        } else {

            int numberOfStartElements = (maxArrayLengthToPrint - 1) / 2;
            int numberOfEndElements = maxArrayLengthToPrint - numberOfStartElements - 1;

            for (int i = 0; i < numberOfStartElements; i++) {
                signalString.append(String.format(templateOfElement, this.signalData[i]));
                if (i < numberOfStartElements - 1) {
                    signalString.append(", ");
                } else {
                    signalString.append(" ");
                    signalString.append(separator);
                    signalString.append(" ");
                }
            }

            for (int i = 0; i < numberOfEndElements; i++) {
                signalString.append(String.format(templateOfElement, this.signalData[i+(this.signalLength-numberOfEndElements)]));
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
        return String.format(TEMPLATE, this.signalToString(MAX_ARRAY_LENGTH_TO_PRINT, TEMPLATE_OF_ELEMENT, SEPARATOR),
                this.signalLength);
    }

    public T findValue(Values value) {
        return null;
    }

}


