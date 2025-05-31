package com.example.healthcheckapplication.activities;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthcheckapplication.R;
import com.example.healthcheckapplication.ecg.Axes;
import com.example.healthcheckapplication.ecg.ECG;
import com.example.healthcheckapplication.ecg.ECGChart;
import com.example.healthcheckapplication.ecg.WriteECGTask;
import com.example.healthcheckapplication.model.AnomalyDetectorWrapper;
import com.example.healthcheckapplication.model.PerceptronWrapper;
import com.example.healthcheckapplication.signals.ExtremesBinarySignal;
import com.example.healthcheckapplication.signals.FourierTransform;
import com.example.healthcheckapplication.signals.INumericalFormSignal;
import com.example.healthcheckapplication.signals.NumericalSignal;
import com.example.healthcheckapplication.signals.Values;
import com.example.healthcheckapplication.storage.InternalStorageFileHandler;
import com.github.mikephil.charting.charts.LineChart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LineChart[] lineCharts = new LineChart[]{findViewById(R.id.lineChart1), findViewById(R.id.lineChart2)};

        Button startButton = findViewById(R.id.button);
        TextView pulse = findViewById(R.id.pulseText);
        TextView arrhythmia = findViewById(R.id.arrhythmiaText);
        TextView height = findViewById(R.id.heightText);
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextDuration = findViewById(R.id.editTextDuration);

        InternalStorageFileHandler internalStorageFileHandler = new InternalStorageFileHandler(this);

        MainActivity context = this;


        OnClickListener oclStartButton = v -> {
            for (LineChart lineChart : lineCharts) {
                lineChart.clear();
                lineChart.invalidate();
            }

            SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            ECG ecg = new ECG(120, Integer.parseInt(editTextDuration.getText().toString()),500);
            WriteECGTask WriteECGTask = new WriteECGTask(ecg, new Axes[]{Axes.X, Axes.Y, Axes.Z}, sm);

            Thread thread = new Thread() {
                @Override
                public void run() {

                    try {

                        Thread getECGThread = new Thread(WriteECGTask);
                        getECGThread.start();
                        getECGThread.join();


                        //original signal
                        NumericalSignal s1 = NumericalSignal.getEuclideanMetricSignal(ecg.getSignals());


                        //signal processing
                        NumericalSignal originalSignal = s1
                                .getSignalReplacedAnomaly(Values.MEDIAN, Values.MEAN)
                                .getSignalFiltered(Values.MEAN, 3, Values.MEDIAN)
                                .getSignalFiltered(Values.MEAN, 10, Values.MEDIAN)
                                .getSignalNormalized(-1.0,1.0).splitSignal(500)[0];
                        originalSignal.setSignalName("PROCESSED ECG");

                        //neural network filtration
                        AnomalyDetectorWrapper anomalyDetectorWrapper = new AnomalyDetectorWrapper(context, 250, 0.00034651169219805585f);
                        PerceptronWrapper perceptronWrapper = new PerceptronWrapper(context, 250);
                        FourierTransform fourierTransform2 = new FourierTransform(originalSignal.splitSignal(500)[0]);
                        NumericalSignal amps3 = new NumericalSignal(Arrays.copyOfRange(fourierTransform2.getAmplitudes(), 0, 250));

                        boolean flag1 = anomalyDetectorWrapper.predictWithThreshold(amps3.getSignalAsFloatArray());
                        boolean flag2 = perceptronWrapper.predict(amps3.getSignalAsFloatArray())>0.55;

                        System.out.println(anomalyDetectorWrapper.predictWithThreshold(amps3.getSignalAsFloatArray()));
                        System.out.println(perceptronWrapper.predict(amps3.getSignalAsFloatArray()));
                        //neural network filtration end

                        NumericalSignal processedSignal = originalSignal.getSignalApproximated(0.03);
                        processedSignal.setSignalName("FILTERED ECG");

                        NumericalSignal filteredSignal = processedSignal.getMostCorrelatedComponent();
                        filteredSignal.setSignalName("MOST_CORR");

                        ExtremesBinarySignal extremes = processedSignal
                                .getExtremesBinarySignalFiltered(Values.MAX, 60);
                        extremes.setSignalName("EXTREMES");

                        ECGChart ecgChart = new ECGChart(lineCharts[0], new INumericalFormSignal[] {
                                originalSignal,
                                processedSignal,
                                filteredSignal,
                                extremes

                        });
                        ecgChart.drawECGChart();

                        //pulse calculation
                        if (flag2) {
                            String pulseText = String.format("PULSE: %s",
                                    ECG.calculatePulseFromExtremesDistance(processedSignal
                                                    .findAggregatedXDistanceBetweenExtremes(extremes ,Values.MEAN),
                                            ecg.getSensorUpdateTiming()));
                            String arrhythmiaText = String.format("ARRHYTHMIA: %s",
                                    (processedSignal.findAggregatedXDistanceBetweenExtremes(extremes ,Values.SIGMA)>10));
                            String heightText = String.format("R-PEAKS: %s", (processedSignal.getSignalOfYDistancesBetweenExtremes(extremes).findValue(Values.SIGMA)>0.5));

                            pulse.post(() -> pulse.setText(pulseText));

                            arrhythmia.post(() -> arrhythmia.setText(arrhythmiaText));

                            height.post(() -> height.setText(heightText));


                        } else {
                            String pulseText = "PULSE: BAD ATTEMPT";
                            String arrhythmiaText = "ARRHYTHMIA: BAD ATTEMPT";
                            String heightText = "R-PEAKS: BAD ATTEMPT";

                            pulse.post(() -> pulse.setText(pulseText));

                            arrhythmia.post(() -> arrhythmia.setText(arrhythmiaText));

                            height.post(() -> height.setText(heightText));
                        }



                        FourierTransform fourierTransform = new FourierTransform(originalSignal);
                        NumericalSignal amps = new NumericalSignal(Arrays.copyOfRange(fourierTransform.getAmplitudes(), 0, originalSignal.getSignalLength()/2));

                        NumericalSignal ampsReconstructed = new NumericalSignal(NumericalSignal.floatArrayToDoubleArray(anomalyDetectorWrapper.predict(amps.getSignalAsFloatArray())));
                        ampsReconstructed.setSignalName("SIGNAL RECONSTRUCTED");

                        ECGChart ecgChart1 = new ECGChart(lineCharts[1], new INumericalFormSignal[] {amps, ampsReconstructed});
                        ecgChart1.drawECGChart();

                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            };

            thread.start();

        };

        startButton.setOnClickListener(oclStartButton);

    }

    public static NumericalSignal[] getAmpsFromSignals(NumericalSignal[] signals) {
        int signalQuantity = signals.length;
        NumericalSignal[] amps = new NumericalSignal[signalQuantity];
        for (int i = 0; i < signalQuantity; i++) {
            FourierTransform fourierTransform = new FourierTransform(signals[i]);
            amps[i] = new NumericalSignal(Arrays.copyOfRange(fourierTransform.getAmplitudes(), 0, signals[i].getSignalLength()/2));
        }
        return amps;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}