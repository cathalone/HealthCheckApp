package com.example.healthcheckapplication.signals;

public enum Signals {
    NUMERICAL_SIGNAL("NumericalSignal", 0),
    BINARY_SIGNAL("BinarySignal", 1),
    EXTREMES_BINARY_SIGNAL("ExtremesBinarySignal", 2)
    ;
    private final String className;
    private final int id;

    Signals(String className, int id) {
        this.className = className;
        this.id = id;
    }

    public String getClassName() {
        return this.className;
    }

    public int getId() {
        return this.id;
    }

    public static int getIdBySignal(INumericalFormSignal signal) {
        String fullClassName = signal.getClass().getName();
        String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        for (Signals signals : Signals.values()) {
            if (signals.className.equals(className)) {
                return signals.id;
            }
        }
        return 0;
    }

    public static INumericalFormSignal getSignalById(int id, double[] signalData, String signalName) {
        for (Signals signals : Signals.values()) {
            if (signals.id == id) {
                switch (signals) {
                    case NUMERICAL_SIGNAL:
                        return new NumericalSignal(signalData, signalName);
                    case BINARY_SIGNAL:
                        return new BinarySignal(signalData, signalName);
                    case EXTREMES_BINARY_SIGNAL:
                        return new ExtremesBinarySignal(signalData, signalName);
                }
            }
        }
        return new NumericalSignal(signalData, signalName);
    }
}
