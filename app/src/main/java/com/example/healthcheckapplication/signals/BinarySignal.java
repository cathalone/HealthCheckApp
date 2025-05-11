package com.example.healthcheckapplication.signals;

public class BinarySignal extends Signal<Boolean> implements INumericalFormSignal{

    public BinarySignal(Boolean[] signalData) {
        super(signalData);
    }

    public BinarySignal(Boolean[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public BinarySignal(Signal<Boolean> signal) {
        super(signal);
    }

    public BinarySignal(boolean[] signalData) {
        super(valueOf(signalData));
    }

    public BinarySignal(boolean[] signalData, String signalName) {
        super(valueOf(signalData), signalName);
    }

    public BinarySignal(double[] signalData) {
        super(valueOf(signalData));
    }

    public BinarySignal(double[] signalData, String signalName) {
        super(valueOf(signalData), signalName);
    }

    @Override
    public double[] getSignalAsDoubleArray() {
        double[] doubleArray = new double[this.signalLength];
        for (int i = 0; i < this.signalLength; i++) {
            if (this.signalData[i]) {
                doubleArray[i] = 1;
            }
        }
        return doubleArray;
    }

    public static Boolean[] valueOf (boolean[] array) {
        Boolean[] wrappedSignalData = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            wrappedSignalData[i] = array[i];
        }
        return wrappedSignalData;
    }

    public static Boolean[] valueOf (double[] array) {
        Boolean[] wrappedSignalData = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            wrappedSignalData[i] = (array[i] > 0);
        }
        return wrappedSignalData;
    }

    public static boolean[] valueOf (Boolean[] array) {
        boolean[] unwrappedSignalData = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            unwrappedSignalData[i] = array[i];
        }
        return unwrappedSignalData;
    }

    @Override
    public BinarySignal getSignalShifted(int range, Values shiftWith) {
        return new BinarySignal(super.getSignalShifted(range, shiftWith));
    }

    @Override
    public BinarySignal getSignalShifted(int range, Boolean shiftWith) {
        return new BinarySignal(super.getSignalShifted(range, shiftWith));
    }

    public NumericalSignal getSignalDistancesBetweenUnits() {

        int numOfUnits = 0;
        for (Boolean element : this.signalData) {
            if (element) {
                numOfUnits += 1;
            }
        }

        if (numOfUnits == 0) {
            return new NumericalSignal(new double[]{0});
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

        return new NumericalSignal(distancesSignal);
    }

    public Double findAggregatedDistancesBetweenUnits(Values distanceAggregation) {
        return this.getSignalDistancesBetweenUnits().findValue(distanceAggregation);
    }

    public NumericalSignal toNumericalSignal() {
        return new NumericalSignal(this.getSignalAsDoubleArray());
    }

    public BinarySignal logicAndSignal(BinarySignal other) {
        Boolean[] newSignal = new Boolean[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {
            newSignal[i] = this.signalData[i] && other.signalData[i];
        }

        return new BinarySignal(newSignal);
    }

    public Boolean findValue(Values value) {
        return value == Values.MAX;
    }

}
