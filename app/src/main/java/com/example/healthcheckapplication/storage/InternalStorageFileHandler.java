package com.example.healthcheckapplication.storage;

import android.content.Context;

import com.example.healthcheckapplication.signals.INumericalFormSignal;
import com.example.healthcheckapplication.signals.Signals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageFileHandler {

    private final Context context;

    public InternalStorageFileHandler(Context context) {
        this.context = context;
    }

    public void saveSignal(String filename, INumericalFormSignal signal) throws IOException{
        File newFile = new File(context.getFilesDir(), filename);
        double[] signalData = signal.getSignalAsDoubleArray();
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(newFile))) {
            outputStream.writeInt(Signals.getIdBySignal(signal));
            outputStream.writeUTF(signal.getSignalName());
            outputStream.writeInt(signalData.length);
            for (double value : signalData) {
                outputStream.writeDouble(value);
            }
        }
    }

    public INumericalFormSignal readData(String filename) throws IOException{
        File newFile = new File(context.getFilesDir(), filename);
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(newFile))) {
            int signalType = inputStream.readInt();
            String signalName = inputStream.readUTF();
            double[] signalData = new double[inputStream.readInt()];
            for (int i = 0; i < signalData.length; i++) {
                signalData[i] = inputStream.readDouble();
            }
            return Signals.getSignalById(signalType, signalData, signalName);
        }
    }
}