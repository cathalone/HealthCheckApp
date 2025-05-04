package com.example.healthcheckapplication.signals;

import java.util.Arrays;

public class ExtremesBinarySignal extends BinarySignal{

    public ExtremesBinarySignal(Boolean[] signalData) {
        super(signalData);
    }

    public ExtremesBinarySignal(Boolean[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public ExtremesBinarySignal(BinarySignal binarySignal) {
        super(Arrays.copyOf(binarySignal.getSignalData(), binarySignal.getSignalLength()), binarySignal.getSignalName());
    }

    public ExtremesBinarySignal logicAndSignal(ExtremesBinarySignal other) {
        return new ExtremesBinarySignal(super.logicAndSignal(other));
    }

}


