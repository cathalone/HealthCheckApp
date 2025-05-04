package com.example.healthcheckapplication;

import android.graphics.Color;

import com.example.healthcheckapplication.signals.NumericalSignal;
import com.example.healthcheckapplication.signals.Signal;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class ECGChart {

    private final int[] COLORS = new int[] {Color.rgb(139, 219, 86),
            Color.rgb(73, 211, 230),
            Color.rgb(199, 82, 235),
            Color.rgb(49, 222, 63)
    };
    private final LineChart ECGChart;
    private final NumericalSignal[] signals;
    private final int numberOfSignals;
    private final ILineDataSet[] dataSets;

    public ECGChart(LineChart ECGChart, NumericalSignal[] signals) {
        this.ECGChart = ECGChart;
        this.signals = signals;
        this.numberOfSignals = signals.length;
        this.dataSets = new ILineDataSet[this.numberOfSignals];
    }

    private LineDataSet makeDataSet(NumericalSignal signal, String label, int color) {
        ArrayList<Entry> data = new ArrayList<>();
        double[] x = NumericalSignal.valueOf(signal.getSignalData());
        for (int j = 0; j < x.length; j++) {
            data.add(new Entry(j, (float) x[j]));
        }
        LineDataSet dataset = new LineDataSet(data, label);
        dataset.setDrawCircles(false);
        dataset.setColor(color, 255);
        return dataset;
    }

    private int getCyclicColor(int i) {
        int colorsLength = COLORS.length;
        return COLORS[i%colorsLength];
    }

    public void drawECGChart() {
        for (int i = 0; i < this.numberOfSignals; i++) {
            this.dataSets[i] = makeDataSet(this.signals[i], this.signals[i].getSignalName(), getCyclicColor(i));
        }

        LineData lineData = new LineData(this.dataSets);

        ECGChart.setData(lineData);
        ECGChart.invalidate();
    }
}