package com.example.healthcheckapplication.signals;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Arrays;
import java.util.Comparator;

public class FourierTransform {
    private final ComplexNumber[] complexAmplitudes;
    private final double[] originalSignal;

    public FourierTransform(double[] originalSignal) {
        this.complexAmplitudes = fourierTransform(originalSignal);
        this.originalSignal = originalSignal;
    }

    public static ComplexNumber[] fourierTransform(double[] x) {
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

    public static double[] inverseFourierTransform(ComplexNumber[] X) {
        int N = X.length;
        double[] x = new double[N];

        for (int k = 0; k < N; k++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int n = 0; n < N; n++) {
                ComplexNumber temp = new ComplexNumber(cos(2 * PI * k * n / N), sin(2 * PI * k * n / N));
                sum = sum.add(temp.multiply(X[n]));
            }
            x[k] = sum.getReal() / N;
        }
        return x;
    }

    public double[] inverseFourierTransform(int kQuantity) {
        int[] kIndices = argsort(this.getAmplitudes());

        int N = this.complexAmplitudes.length;
        double[] x = new double[N];

        for (int n = 0; n < N; n++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int k = 0; k < N; k++) {
                for (int i = kIndices.length - kQuantity; i < kIndices.length; i++) {
                    if (k == kIndices[i]) {
                        ComplexNumber temp = new ComplexNumber(cos(2 * PI * k * n / N), sin(2 * PI * k * n / N));
                        sum = sum.add(temp.multiply(this.complexAmplitudes[k]));
                    }
                }
            }
            x[n] = sum.getReal() / N;
        }
        return x;
    }

    public double[] inverseFourierTransform() {
        int N = this.complexAmplitudes.length;
        double[] x = new double[N];

        for (int n = 0; n < N; n++) {
            ComplexNumber sum = new ComplexNumber(0,0);
            for (int k = 0; k < N; k++) {
                ComplexNumber temp = new ComplexNumber(cos(2 * PI * k * n / N), sin(2 * PI * k * n / N));
                sum = sum.add(temp.multiply(this.complexAmplitudes[k]));
            }
            x[n] = sum.getReal() / N;
        }
        return x;
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
