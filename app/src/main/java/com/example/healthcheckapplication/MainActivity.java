package com.example.healthcheckapplication;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthcheckapplication.signals.Signal;
import com.example.healthcheckapplication.signals.Values;
import com.github.mikephil.charting.charts.LineChart;

public class MainActivity extends AppCompatActivity {
    private SensorManager sm;
    private LineChart lCh;

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

        lCh = findViewById(R.id.lineChart);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ECG ecg = new ECG(120, 8000);

        WriteECGTask WriteECGTask = new WriteECGTask(ecg, sm);

        Thread thread = new Thread() {
            @Override
            public void run() {

                try {

                    Thread getECGThread = new Thread(WriteECGTask);
                    getECGThread.start();
                    getECGThread.join();

                    Signal originalSignal = ecg.getSignal().getSignalReplacedAnomaly(Values.MEDIAN, Values.MEAN).getSignalFiltered(Values.MEAN, 3, Values.MEDIAN).getSignalFiltered(Values.MEAN, 10, Values.MEDIAN).getSignalNormalized(-1,1);
                    originalSignal.setSignalName("PROCESSED ECG");
                    Signal processedSignal = originalSignal.getSignalFiltered(Values.MEAN, 15, Values.MEDIAN);
                    processedSignal.setSignalName("FILTERED ECG");
                    Signal filteredSignal = processedSignal.getSignalAutoCorrelation(Values.MEAN);
                    filteredSignal.setSignalName("AUTOCORRELATION");
                    Signal extremes = filteredSignal.getSignalExtremesWithBuffer(30,Values.MAX).toNumericalSignal();
                    extremes.setSignalName("EXTREMES");
                    System.out.println(filteredSignal.findAggregatedXDistanceBetweenExtremes(50, Values.MAX, Values.MEAN));

                    ECGChart ecgChart = new ECGChart(lCh, new Signal[] {originalSignal, processedSignal, filteredSignal, extremes});
                    ecgChart.drawECGChart();

                    System.out.println(ECG.calculatePulseFromExtremesDistance(filteredSignal.findAggregatedXDistanceBetweenExtremes(50, Values.MAX, Values.MEAN), ecg.getSensorUpdateTiming()));

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };

        thread.start();
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