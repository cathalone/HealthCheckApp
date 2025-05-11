package com.example.healthcheckapplication.activities;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthcheckapplication.R;
import com.example.healthcheckapplication.ecg.ECG;
import com.example.healthcheckapplication.ecg.ECGChart;
import com.example.healthcheckapplication.ecg.WriteECGTask;
import com.example.healthcheckapplication.signals.ExtremesBinarySignal;
import com.example.healthcheckapplication.signals.INumericalFormSignal;
import com.example.healthcheckapplication.signals.NumericalSignal;
import com.example.healthcheckapplication.signals.Values;
import com.example.healthcheckapplication.storage.InternalStorageFileHandler;
import com.github.mikephil.charting.charts.LineChart;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
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

        InternalStorageFileHandler internalStorageFileHandler = new InternalStorageFileHandler(this);

        lCh = findViewById(R.id.lineChart);
        Button startButton = findViewById(R.id.button);
        TextView pulse = findViewById(R.id.pulseText);
        OnClickListener oclStartButton = new OnClickListener() {
            @Override
            public void onClick(View v) {
                lCh.clear();
                lCh.invalidate();

                SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                ECG ecg = new ECG(120, 2000);
                WriteECGTask WriteECGTask = new WriteECGTask(ecg, sm);

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        try {

                            Thread getECGThread = new Thread(WriteECGTask);
                            getECGThread.start();
                            getECGThread.join();

                            NumericalSignal originalSignal = ecg.getSignal()
                                    .getSignalReplacedAnomaly(Values.MEDIAN, Values.MEAN)
                                    .getSignalFiltered(Values.MEAN, 3, Values.MEDIAN)
                                    .getSignalFiltered(Values.MEAN, 10, Values.MEDIAN)
                                    .getSignalNormalized(-1.0,1.0);
                            originalSignal.setSignalName("PROCESSED ECG");

                            NumericalSignal processedSignal = originalSignal
                                    .getSignalFiltered(Values.MEAN, 15, Values.MEDIAN);
                            processedSignal.setSignalName("FILTERED ECG");

                            try {
                                internalStorageFileHandler.saveSignal("test.txt", processedSignal);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            NumericalSignal filteredSignal = processedSignal
                                    .getSignalAutoCorrelation(Values.MEAN);
                            filteredSignal.setSignalName("AUTO-CORRELATION");

                            ExtremesBinarySignal extremes = filteredSignal
                                    .getExtremesBinarySignalFiltered(filteredSignal
                                            .getSignalExtremesWithBuffer(2), filteredSignal.findSigma())
                                    .logicAndSignal(filteredSignal
                                            .getSignalExtremesWithBuffer(2,Values.MAX));
                            extremes.setSignalName("EXTREMES");

                            try {
                                internalStorageFileHandler.saveSignal("test1.txt", extremes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ECGChart ecgChart = new ECGChart(lCh, new INumericalFormSignal[] {
                                    originalSignal,
                                    processedSignal,
                                    filteredSignal,
                                    extremes
                            });
                            ecgChart.drawECGChart();

                            pulse.post(() -> pulse.setText(String.format("PULSE: %s",
                                    ECG.calculatePulseFromExtremesDistance(filteredSignal
                                            .findAggregatedXDistanceBetweenExtremes(extremes ,Values.MEAN),
                                            ecg.getSensorUpdateTiming()))));

                            Log.d("s","start");
                            try {
                                System.out.println(internalStorageFileHandler.readData("test.txt"));
                                System.out.println(internalStorageFileHandler.readData("test1.txt"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d("s","end");

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                };

                thread.start();

            }
        };

        startButton.setOnClickListener(oclStartButton);


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