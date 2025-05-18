package com.example.healthcheckapplication.storage;

import android.content.Context;

import com.example.healthcheckapplication.ecg.Axes;
import com.example.healthcheckapplication.ecg.ECG;
import com.example.healthcheckapplication.signals.AxisNumericalSignal;
import com.example.healthcheckapplication.signals.INumericalFormSignal;
import com.example.healthcheckapplication.signals.Signals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageFileHandler {

    private final Context context;

    public InternalStorageFileHandler(Context context) {
        this.context = context;
    }

    public String generateFileName(String name) {
        String FILE_NAME_TEMPLATE = "s_%s_%s.txt";
        int greatestIndex = 0;
        for (File file : this.getAllFiles()) {
            String filename = file.getName();
            if (filename.contains(String.format("_%s_", name))) {
                String index = filename.substring(filename.lastIndexOf('_') + 1, filename.lastIndexOf('.'));
                int indexInt = Integer.parseInt(index);
                if (indexInt > greatestIndex) {
                    greatestIndex = indexInt;
                }
            }
        }
        return String.format(FILE_NAME_TEMPLATE, name, greatestIndex + 1);
    }

    public void deleteAllFiles() throws FileNotFoundException {
        for (File file : this.getAllFiles()) {
            if (!file.delete()) {
                throw new FileNotFoundException();
            }
        }
    }

    public File[] getAllFiles() {
        return context.getFilesDir().listFiles();
    }

    public void saveECG(String filename, ECG ecg) throws IOException {
        File newFile = new File(context.getFilesDir(), filename);
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(newFile))) {

            outputStream.writeInt(ecg.getSignals().length);
            outputStream.writeInt(ecg.getRefreshRate());
            outputStream.writeInt(ecg.getECGDurationInMillis());
            outputStream.writeInt(ecg.getExtraTimeForSensorCalibratingInMillis());

            for (AxisNumericalSignal axisNumericalSignal : ecg.getSignals()) {
                double[] signalData = axisNumericalSignal.getSignalAsDoubleArray();

                outputStream.writeInt(axisNumericalSignal.getAxis().getId());
                outputStream.writeUTF(axisNumericalSignal.getSignalName());
                outputStream.writeInt(signalData.length);
                for (double value : signalData) {
                    outputStream.writeDouble(value);
                }

            }
        }
    }

    public ECG readECG(String filename) throws IOException {
        File newFile = new File(context.getFilesDir(), filename);
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(newFile))) {

            int signalsQuantity = inputStream.readInt();
            int duration = inputStream.readInt();
            int refreshRate = inputStream.readInt();
            int extraTime = inputStream.readInt();

            AxisNumericalSignal[] axisNumericalSignals = new AxisNumericalSignal[signalsQuantity];
            for (int i = 0; i < signalsQuantity; i++) {

                int signalAxis = inputStream.readInt();
                String signalName = inputStream.readUTF();
                double[] signalData = new double[inputStream.readInt()];
                for (int j = 0; j < signalData.length; j++) {
                    signalData[j] = inputStream.readDouble();
                }
                axisNumericalSignals[i] = new AxisNumericalSignal(signalData, signalName, Axes.getAxesById(signalAxis));
            }
            return new ECG(duration, refreshRate, extraTime, axisNumericalSignals);
        }
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