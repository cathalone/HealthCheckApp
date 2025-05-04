package com.example.healthcheckapplication.signals;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import androidx.annotation.NonNull;

public class Signal {

    private final double[] signalData;
    private final int signalLength;
    private String signalName = "SIGNAL";

    public Signal(double[] signalData) {

        this.signalData = signalData;
        this.signalLength = signalData.length;

    }

    public Signal(double[] signalData, String signalName) {

        this.signalData = signalData;
        this.signalLength = signalData.length;
        this.signalName = signalName;

    }

    public double[] getSignalData() {
        return signalData;
    }

    public int getSignalLength() {
        return signalLength;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public double findMin() {
        double min = this.signalData[0];
        for (double element : this.signalData) {
            if (element < min) {
                min = element;
            }
        }
        return min;
    }

    public double findMax() {
        double max = this.signalData[0];
        for (double element : this.signalData) {
            if (element > max) {
                max = element;
            }
        }
        return max;
    }

    public double findMean() {
        double signalSum = 0;
        for (double element : this.signalData) {
            signalSum += element;
        }

        return signalSum / this.signalLength;
    }

    public double findSigma() {
        double signalMean = this.findMean();
        double signalSum = 0;
        for (double element : this.signalData) {
            double signalDiff = element - signalMean;
            signalSum += signalDiff * signalDiff;
        }

        return sqrt(signalSum / this.signalLength);
    }

    public double findMedian() {
        double[] sortedSignal = Arrays.copyOf(this.signalData, this.signalLength);
        Arrays.sort(sortedSignal);
        if (this.signalLength % 2 == 1) {
            return sortedSignal[this.signalLength / 2];
        } else {
            return (sortedSignal[(this.signalLength / 2) - 1] + sortedSignal[(this.signalLength / 2)]) / 2;
        }
    }

    public double findCovariance(Signal other) {
        double signalXMean = this.findMean();
        double signalYMean = other.findMean();
        double signalSum = 0;
        for (int i = 0; i < this.signalLength; i++) {
            double signalXDiff = this.signalData[i] - signalXMean;
            double signalYDiff = other.signalData[i] - signalYMean;
            signalSum += signalXDiff * signalYDiff;
        }

        return signalSum / this.signalLength;
    }

    public double findCorrelation(Signal other) {
        return this.findCovariance(other) / (this.findSigma() * other.findSigma());
    }

    public BinarySignal getSignalOfAnomalyPosition3Sigma(Values sigmaFrom) {
        boolean[] newBinarySignal = new boolean[this.signalLength];
        double from = this.findValue(sigmaFrom);
        double sigma = this.findSigma();

        for (int i = 0; i < this.signalLength; i++) {
            if (abs(this.signalData[i] - from) > 3*sigma) {
                newBinarySignal[i] = true;
            }
        }
        return new BinarySignal(newBinarySignal);
    }

    public Signal getSignalReplacedAnomaly(Values replaceWith, Values sigmaFrom) {
        double[] processedSignal = new double[this.signalLength];
        boolean[] isAnomalyIndex = this.getSignalOfAnomalyPosition3Sigma(sigmaFrom).getSignalData();
        double with = this.findValue(replaceWith);

        for (int i = 0; i < this.signalLength; i++) {
            if (isAnomalyIndex[i]) {
                processedSignal[i] = with;
            } else {
                processedSignal[i] = this.signalData[i];
            }
        }
        return new Signal(processedSignal);
    }

    public Signal getSignalFiltered(Values replaceWith, int bufferSize, Values startSignal) {
        double[] buffer = new double[bufferSize];
        Signal bufferSignal = new Signal(buffer);

        double[] newSignal = new double[this.signalLength];
        double start = this.findValue(startSignal);

        for (int i = 0; i < this.signalLength; i++) {
            for (int j = 0; j < bufferSize; j++) {
                int k = i - (bufferSize - j);
                if (k < 0) {
                    buffer[j] = start;
                } else {
                    buffer[j] = this.signalData[k];
                }
            }
            newSignal[i] = bufferSignal.findValue(replaceWith);
        }
        return new Signal(newSignal);
    }

    public Signal getSignalNormalized(double from, double to) {
        double min = this.findMin();
        double delta = this.findMax() - min;
        double newDelta = to - from;
        double[] newSignal = new double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {
            newSignal[i] = (((this.signalData[i] - min) / delta) * newDelta) + from;
        }

        return new Signal(newSignal);
    }

    public Signal getSignalShifted(int range, Values shiftWith) {

        if (shiftWith != Values.SIGNAL) {

            return this.getSignalShifted(range, this.findValue(shiftWith));

        } else {

            double[] newSignal = new double[this.signalLength];

            for (int i = 0; i < this.signalLength; i++) {
                if (i + range >= this.signalLength) {
                    newSignal[i + range - this.signalLength] = this.signalData[i];
                } else if (i + range < 0) {
                    newSignal[i + range + this.signalLength] = this.signalData[i];
                } else {
                    newSignal[i + range] = this.signalData[i];
                }
            }
            return new Signal(newSignal);
        }

    }

    public Signal getSignalShifted(int range, double shiftWith) {
        double[] newSignal = new double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {

            if (i + range >= this.signalLength) {
                newSignal[i + range - this.signalLength] = shiftWith;
            } else if (i + range < 0) {
                newSignal[i + range + this.signalLength] = shiftWith;
            } else {
                newSignal[i + range] = this.signalData[i];
            }

        }

        return new Signal(newSignal);
    }

    public Signal getSignalAutoCorrelation(Values shiftWith) {
        double[] newSignal = new double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {

            newSignal[i] = this.findCorrelation(this.getSignalShifted(i, shiftWith));

        }

        return new Signal(newSignal);
    }

    public ExtremesBinarySignal getSignalExtremesWithBuffer(int bufferSize, Values extrema) {
        double[] buffer = new double[bufferSize];
        boolean[] newSignal = new boolean[this.signalLength];
        boolean newSignalGrowth = true;
        boolean oldSignalGrowth;

        for (int i = 0; i < this.signalLength; i++) {
            for (int j = 0; j < bufferSize; j++) {
                int k = i - (bufferSize - j);
                if (k < 0) {
                    buffer[j] = this.signalData[0];
                } else {
                    buffer[j] = this.signalData[k];
                }
            }
            oldSignalGrowth = newSignalGrowth;
            newSignalGrowth = (buffer[bufferSize - 1] - buffer[0]) > 0;
            if (i != 0) {
                if (extrema == Values.MAX) {
                    if (oldSignalGrowth && !newSignalGrowth) {
                        newSignal[i] = true;
                    }
                } else {
                    if (!oldSignalGrowth && newSignalGrowth) {
                        newSignal[i] = true;
                    }
                }
            }
        }
        return new ExtremesBinarySignal(new BinarySignal(newSignal).getSignalShifted(-bufferSize/2-1, Values.ZERO));
    }

    public ExtremesBinarySignal getSignalExtremesWithBuffer(int bufferSize) {
        double[] buffer = new double[bufferSize];
        boolean[] newSignal = new boolean[this.signalLength];
        boolean newSignalGrowth = true;
        boolean oldSignalGrowth;

        for (int i = 0; i < this.signalLength; i++) {
            for (int j = 0; j < bufferSize; j++) {
                int k = i - (bufferSize - j);
                if (k < 0) {
                    buffer[j] = this.signalData[0];
                } else {
                    buffer[j] = this.signalData[k];
                }
            }
            oldSignalGrowth = newSignalGrowth;
            newSignalGrowth = (buffer[bufferSize - 1] - buffer[0]) > 0;
            if (i != 0) {
                if ((oldSignalGrowth && !newSignalGrowth) || (!oldSignalGrowth && newSignalGrowth)) {
                    newSignal[i] = true;
                }
            }
        }
        return new ExtremesBinarySignal(new BinarySignal(newSignal).getSignalShifted(-bufferSize/2-1, Values.ZERO));
    }

//    public Signal getExtremesSignalFiltratedByYDistance(Signal originalSignal) {
//        double[] newSignal = new double[this.signalLength];
//        double sigma = originalSignal.signalSigma;
//
//        double previousElement = 0;
//        int previousElementIndex = 0;
//        double currentElement = 0;
//        int currentElementNumber = 0;
//
//        for (int i = 0; i < this.signalLength; i++) {
//            if (this.signal[i] == 1) {
//                currentElement = originalSignal.signal[i];
//                if (currentElementNumber > 0) {
//                    if (abs(previousElement - currentElement) > sigma) {
//                        previousElement = currentElement;
//                        newSignal[i] = 1;
//                    } else {
//                        continue;
//                    }
//                } else {
//                    previousElement = currentElement;
//                    newSignal[i] = 1;
//                }
//                currentElementNumber += 1;
//            }
//
//        }
//        return new Signal(newSignal);
//    }

    public double findAggregatedXDistanceBetweenExtremes(int bufferSize, Values extrema, Values distanceAggregation) {
        return this.getSignalExtremesWithBuffer(bufferSize, extrema).findAggregatedDistancesBetweenUnits(distanceAggregation);
    }

    private static String signalToString(double[] signal, int maxArrayLenthToPrint, String templateOfNumber, String separator) {
        int signalLength = signal.length;

        StringBuilder signalString = new StringBuilder();
        signalString.append("[");

        if (signalLength <= maxArrayLenthToPrint) {
            for (int i = 0; i < signalLength; i++) {
                signalString.append(String.format(templateOfNumber, signal[i]));
                if (i < signalLength - 1) {
                    signalString.append(", ");
                }
            }
        } else {

            int numberOfStartElements = (maxArrayLenthToPrint - 1) / 2;
            int numberOfEndElements = maxArrayLenthToPrint - numberOfStartElements - 1;

            for (int i = 0; i < numberOfStartElements; i++) {
                signalString.append(String.format(templateOfNumber, signal[i]));
                if (i < numberOfStartElements - 1) {
                    signalString.append(", ");
                } else {
                    signalString.append(" ");
                    signalString.append(separator);
                    signalString.append(" ");
                }
            }

            for (int i = 0; i < numberOfEndElements; i++) {
                signalString.append(String.format(templateOfNumber, signal[i+(signalLength-numberOfEndElements)]));
                if (i < numberOfEndElements - 1) {
                    signalString.append(", ");
                }
            }
        }

        signalString.append("]");

        return signalString.toString();
    }

    @NonNull
    @Override
    public String toString() {
        final int NUMBER_OF_CHARACTERS_AFTER_POINT = 3;
        final int MAX_ARRAY_LENGTH_TO_PRINT = 7;
        final String SEPARATOR = "...";
        final String TEMPLATE_OF_NUMBER = "%." + NUMBER_OF_CHARACTERS_AFTER_POINT + "f";
        final String TEMPLATE = "SIGNAL: %s \nLENGTH: %s \nMIN: %s \nMAX: %s \nMEAN: %s \nSIGMA: %s \n";

        return String.format(TEMPLATE, signalToString(this.signalData, MAX_ARRAY_LENGTH_TO_PRINT, TEMPLATE_OF_NUMBER, SEPARATOR),
                this.signalLength, String.format(TEMPLATE_OF_NUMBER, this.findMin()),
                String.format(TEMPLATE_OF_NUMBER, this.findMax()),String.format(TEMPLATE_OF_NUMBER, this.findMean()),
                String.format(TEMPLATE_OF_NUMBER, this.findSigma()));
    }

    public double findValue(Values value) {
        switch (value) {
            case MAX:
                return this.findMax();
            case MIN:
                return this.findMin();
            case MEAN:
                return this.findMean();
            case MEDIAN:
                return this.findMedian();
            case SIGMA:
                return this.findSigma();
            case LENGTH:
                return this.signalLength;
            default:
                return 0;
        }
    }
}
