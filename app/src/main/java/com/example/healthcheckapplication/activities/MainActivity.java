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

        LineChart[] lineCharts = new LineChart[]{findViewById(R.id.lineChart1), findViewById(R.id.lineChart2), findViewById(R.id.lineChart3)};

        Button startButton = findViewById(R.id.button);
        TextView pulse = findViewById(R.id.pulseText);
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

                        NumericalSignal s1 = NumericalSignal.getEuclideanMetricSignal(ecg.getSignals());

                        NumericalSignal originalSignal = s1
                                .getSignalReplacedAnomaly(Values.MEDIAN, Values.MEAN)
                                .getSignalFiltered(Values.MEAN, 3, Values.MEDIAN)
                                .getSignalFiltered(Values.MEAN, 10, Values.MEDIAN)
                                .getSignalNormalized(-1.0,1.0).splitSignal(500)[0];
                        originalSignal.setSignalName("PROCESSED ECG");

                        NumericalSignal processedSignal = originalSignal.getSignalApproximated(0.03);
                        processedSignal.setSignalName("FILTERED ECG");

                        NumericalSignal filteredSignal = processedSignal.getMostCorrelatedComponent();
                        filteredSignal.setSignalName("MOST_CORR");

                        System.out.println(processedSignal);

                        ExtremesBinarySignal extremes = filteredSignal
                                .getExtremesBinarySignalFiltered(Values.MAX);
                        extremes.setSignalName("EXTREMES");

                        ECGChart ecgChart = new ECGChart(lineCharts[0], new INumericalFormSignal[] {
                                originalSignal,
                                processedSignal,
                                filteredSignal,
                                extremes

                        });
                        ecgChart.drawECGChart();

                        pulse.post(() -> pulse.setText(String.format("PULSE: %s",
                                ECG.calculatePulseFromExtremesDistance(processedSignal
                                        .findAggregatedXDistanceBetweenExtremes(extremes ,Values.MEAN),
                                        ecg.getSensorUpdateTiming()))));


                        FourierTransform fourierTransform = new FourierTransform(processedSignal);
                        NumericalSignal amps = new NumericalSignal(Arrays.copyOfRange(fourierTransform.getAmplitudes(), 0, processedSignal.getSignalLength()/2));
                        ECGChart ecgChart1 = new ECGChart(lineCharts[1], new INumericalFormSignal[] {amps});
                        ecgChart1.drawECGChart();

                        FourierTransform fourierTransform1 = new FourierTransform(originalSignal);
                        NumericalSignal amps1 = new NumericalSignal(Arrays.copyOfRange(fourierTransform1.getAmplitudes(), 0, originalSignal.getSignalLength()/2));
                        ECGChart ecgChart2 = new ECGChart(lineCharts[2], new INumericalFormSignal[] {amps1});
                        ecgChart2.drawECGChart();

                        NumericalSignal originalSignal2 = s1
                                .getSignalReplacedAnomaly(Values.MEDIAN, Values.MEAN)
                                .getSignalFiltered(Values.MEAN, 3, Values.MEDIAN)
                                .getSignalFiltered(Values.MEAN, 10, Values.MEDIAN)
                                .getSignalNormalized(-1.0,1.0);
                        originalSignal.setSignalName("PROCESSED ECG");

                        AnomalyDetectorWrapper anomalyDetectorWrapper = new AnomalyDetectorWrapper(context, 250, 0.00034651169219805585f);
                        PerceptronWrapper perceptronWrapper = new PerceptronWrapper(context, 250);
                        FourierTransform fourierTransform2 = new FourierTransform(originalSignal2.splitSignal(500)[0]);
                        NumericalSignal amps3 = new NumericalSignal(Arrays.copyOfRange(fourierTransform2.getAmplitudes(), 0, 250));

                        System.out.println(anomalyDetectorWrapper.predict(amps3.getSignalAsFloatArray()));
                        System.out.println(perceptronWrapper.predict(amps3.getSignalAsFloatArray()));

                        NumericalSignal[] numericalSignals = originalSignal2.splitSignal(500);
                        internalStorageFileHandler.writeSignalToCsv("signals.csv", numericalSignals);
                        NumericalSignal[] amps2 = getAmpsFromSignals(numericalSignals);
                        internalStorageFileHandler.writeSignalToCsv("amps.csv", amps2);



//                        FourierTransform fourierTransform = new FourierTransform(processedSignal);
//                        double[] sortedAmplitudes = Arrays.copyOf(fourierTransform.getAmplitudes(), fourierTransform.getAmplitudes().length);
//                        Arrays.sort(sortedAmplitudes);
//                        NumericalSignal amps = new NumericalSignal(Arrays.copyOfRange(sortedAmplitudes,processedSignal.getSignalLength() - (int)(processedSignal.getSignalLength()*0.03), processedSignal.getSignalLength()));
//                        ECGChart ecgChart1 = new ECGChart(lineCharts[1], new INumericalFormSignal[] {amps});
//                        ecgChart1.drawECGChart();

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