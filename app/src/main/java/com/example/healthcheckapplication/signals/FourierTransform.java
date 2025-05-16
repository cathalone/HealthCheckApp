package com.example.healthcheckapplication.signals;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Arrays;
import java.util.Comparator;

public class FourierTransform {
    private final ComplexNumber[] complexAmplitudes;
    private final INumericalFormSignal signal;

    public FourierTransform(INumericalFormSignal signal) {
        this.signal = signal;
        this.complexAmplitudes = fourierTransform(signal);
    }

    private static ComplexNumber[] fourierTransform(INumericalFormSignal signal) {
        double[] x = signal.getSignalAsDoubleArray();
        int N = x.length;
        ComplexNumber[] X = new ComplexNumber[N];

        for (int k = 0; k < N; k++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int n = 0; n < N; n++) {
                ComplexNumber temp = new ComplexNumber(cos(2 * PI * k * n / N), -sin(2 * PI * k * n / N));
                sum = sum.add(temp.multiply(x[n]));
            }
            X[k] = sum;
        }
        return X;
    }

    private static NumericalSignal inverseFourierTransform(ComplexNumber[] complexAmplitudes, int[] kIndices) {
        int N = complexAmplitudes.length;
        double[] x = new double[N];

        for (int n = 0; n < N; n++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int k : kIndices) {
                ComplexNumber temp = new ComplexNumber(cos(2 * PI * k * n / N), sin(2 * PI * k * n / N));
                sum = sum.add(temp.multiply(complexAmplitudes[k]));
            }
            x[n] = sum.getReal() / N;
        }
        return new NumericalSignal(x);
    }

    public NumericalSignal getSignalApproximated() {
        int[] kIndices = new int[this.signal.getSignalLength()];
        for (int i = 0; i < kIndices.length; i++) {
            kIndices[i] = i;
        }
        return inverseFourierTransform(this.complexAmplitudes, kIndices);
    }

    public NumericalSignal getSignalApproximated(int numberOfDominantAmplitudes) {
        int[] sortedKIndicesAll = argsort(this.getAmplitudes());
        int N = sortedKIndicesAll.length;
        int[] kIndices = new int[numberOfDominantAmplitudes];
        for (int i = N - numberOfDominantAmplitudes; i < N; i++) {
            kIndices[i - N + numberOfDominantAmplitudes] = sortedKIndicesAll[i];
        }
        return inverseFourierTransform(this.complexAmplitudes, kIndices);
    }

    public NumericalSignal getSignalApproximated(int[] amplitudesIndices) {
        return inverseFourierTransform(this.complexAmplitudes, amplitudesIndices);
    }

    public double[] getAmplitudes() {
        int N = this.complexAmplitudes.length;
        double[] amplitudes = new double[N];

        for (int n = 0; n < N; n++) {
            amplitudes[n] = this.complexAmplitudes[n].abs() / N;
        }

        return amplitudes;
    }

    public static int[] argsort(double[] array) {
        Integer[] indices = new Integer[array.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparingDouble(i -> array[i]));

        return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
    }


}
