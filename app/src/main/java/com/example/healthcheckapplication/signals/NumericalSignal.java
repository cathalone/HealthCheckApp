package com.example.healthcheckapplication.signals;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import androidx.annotation.NonNull;

public class NumericalSignal extends Signal<Double> implements INumericalFormSignal{

    protected static int NUMBER_OF_CHARACTERS_AFTER_POINT = 3;
    protected static String TEMPLATE = "SIGNAL: %s \nLENGTH: %s \nMIN: %s \nMAX: %s \nMEAN: %s \nSIGMA: %s \n";

    public NumericalSignal(Double[] signalData) {
        super(signalData);
    }

    public NumericalSignal(Double[] signalData, String signalName) {
        super(signalData, signalName);
    }

    public NumericalSignal(Signal<Double> signal) {
        super(signal);
    }

    public NumericalSignal(double[] signalData) {
        super(valueOf(signalData));
    }

    public NumericalSignal(double[] signalData, String signalName) {
        super(valueOf(signalData), signalName);
    }

    @Override
    public double[] getSignalAsDoubleArray() {
        return valueOf(this.signalData);
    }

    public static Double[] valueOf (double[] array) {
        Double[] wrappedSignalData = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            wrappedSignalData[i] = array[i];
        }
        return wrappedSignalData;
    }

    public static double[] valueOf (Double[] array) {
        double[] unwrappedSignalData = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            unwrappedSignalData[i] = array[i];
        }
        return unwrappedSignalData;
    }

    public Double findMin() {
        Double min = this.signalData[0];
        for (Double element : this.signalData) {
            if (element < min) {
                min = element;
            }
        }
        return min;
    }

    public Double findMax() {
        Double max = this.signalData[0];
        for (Double element : this.signalData) {
            if (element > max) {
                max = element;
            }
        }
        return max;
    }

    public Double findMean() {
        double signalSum = 0;
        for (Double element : this.signalData) {
            signalSum += element;
        }

        return signalSum / this.signalLength;
    }

    public Double findSigma() {
        Double signalMean = this.findMean();
        double signalSum = 0;
        for (Double element : this.signalData) {
            Double signalDiff = element - signalMean;
            signalSum += signalDiff * signalDiff;
        }

        return sqrt(signalSum / this.signalLength);
    }

    public Double findMedian() {
        Double[] sortedSignal = Arrays.copyOf(this.signalData, this.signalLength);
        Arrays.sort(sortedSignal);
        if (this.signalLength % 2 == 1) {
            return sortedSignal[this.signalLength / 2];
        } else {
            return (sortedSignal[(this.signalLength / 2) - 1] + sortedSignal[(this.signalLength / 2)]) / 2;
        }
    }

    public Double findCovariance(NumericalSignal other) {
        Double signalXMean = this.findMean();
        Double signalYMean = other.findMean();
        double signalSum = 0;
        for (int i = 0; i < this.signalLength; i++) {
            Double signalXDiff = this.signalData[i] - signalXMean;
            Double signalYDiff = other.signalData[i] - signalYMean;
            signalSum += signalXDiff * signalYDiff;
        }

        return signalSum / this.signalLength;
    }

    public Double findCorrelation(NumericalSignal other) {
        return this.findCovariance(other) / (this.findSigma() * other.findSigma());
    }

    public BinarySignal getSignalOfAnomalyPosition3Sigma(Values sigmaFrom) {
        Boolean[] newBinarySignal = new Boolean[this.signalLength];
        Double from = this.findValue(sigmaFrom);
        Double sigma = this.findSigma();

        for (int i = 0; i < this.signalLength; i++) {
            newBinarySignal[i] = abs(this.signalData[i] - from) > 3 * sigma;
        }
        return new BinarySignal(newBinarySignal);
    }

    public NumericalSignal getSignalReplacedAnomaly(Values replaceWith, Values sigmaFrom) {
        Double[] processedSignal = new Double[this.signalLength];
        Boolean[] isAnomalyIndex = this.getSignalOfAnomalyPosition3Sigma(sigmaFrom).getSignalData();
        Double with = this.findValue(replaceWith);

        for (int i = 0; i < this.signalLength; i++) {
            if (isAnomalyIndex[i]) {
                processedSignal[i] = with;
            } else {
                processedSignal[i] = this.signalData[i];
            }
        }
        return new NumericalSignal(processedSignal);
    }

    public NumericalSignal getSignalFiltered(Values replaceWith, int bufferSize, Values startSignal) {
        Double[] buffer = new Double[bufferSize];
        NumericalSignal bufferSignal = new NumericalSignal(buffer);

        Double[] newSignal = new Double[this.signalLength];
        Double start = this.findValue(startSignal);

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
        return new NumericalSignal(newSignal);
    }

    public NumericalSignal getSignalNormalized(Double from, Double to) {
        Double min = this.findMin();
        double delta = this.findMax() - min;
        double newDelta = to - from;
        Double[] newSignal = new Double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {
            newSignal[i] = (((this.signalData[i] - min) / delta) * newDelta) + from;
        }

        return new NumericalSignal(newSignal);
    }

    @Override
    public NumericalSignal getSignalShifted(int range, Values shiftWith) {
        return new NumericalSignal(super.getSignalShifted(range, shiftWith));
    }

    @Override
    public NumericalSignal getSignalShifted(int range, Double shiftWith) {
        return new NumericalSignal(super.getSignalShifted(range, shiftWith));
    }

    public NumericalSignal getSignalAutoCorrelation(Values shiftWith) {
        Double[] newSignal = new Double[this.signalLength];

        for (int i = 0; i < this.signalLength; i++) {

            newSignal[i] = this.findCorrelation(this.getSignalShifted(i, shiftWith));

        }

        return new NumericalSignal(newSignal);
    }

    public ExtremesBinarySignal getSignalExtremesWithBuffer(int bufferSize, Values extrema) {
        Double[] buffer = new Double[bufferSize];
        Boolean[] newSignal = new Boolean[this.signalLength];
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
                    newSignal[i] = oldSignalGrowth && !newSignalGrowth;
                } else {
                    newSignal[i] = !oldSignalGrowth && newSignalGrowth;
                }
            } else {
                newSignal[i] = false;
            }
        }
        return new ExtremesBinarySignal(new BinarySignal(newSignal).getSignalShifted(-bufferSize/2-1, Values.ZERO));
    }

    public ExtremesBinarySignal getSignalExtremesWithBuffer(int bufferSize) {
        Double[] buffer = new Double[bufferSize];
        Boolean[] newSignal = new Boolean[this.signalLength];
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
                newSignal[i] = (oldSignalGrowth && !newSignalGrowth) || (!oldSignalGrowth && newSignalGrowth);
            } else {
                newSignal[i] = false;
            }
        }
        return new ExtremesBinarySignal(new BinarySignal(newSignal).getSignalShifted(-bufferSize/2-1, Values.ZERO));
    }

    public NumericalSignal getSignalOfExtremesValues(ExtremesBinarySignal extremesBinarySignal) {
        int[] extremesIndexes = extremesBinarySignal.findExtremesIndexes();
        double[] extremesValues = new double[extremesIndexes.length];
        for (int i = 0; i < extremesIndexes.length; i++) {
            extremesValues[i] = this.signalData[extremesIndexes[i]];
        }
        return new NumericalSignal(extremesValues);
    }

    public NumericalSignal getSignalOfYDistancesBetweenExtremes(ExtremesBinarySignal extremesBinarySignal) {
        double[] extremesValues = valueOf(this.getSignalOfExtremesValues(extremesBinarySignal).getSignalData());
        double[] YDistancesSignal = new double[extremesValues.length-1];

        for (int i = 0; i < YDistancesSignal.length; i++) {
            YDistancesSignal[i] = extremesValues[i+1]-extremesValues[i];
        }
        return new NumericalSignal(YDistancesSignal);
    }

    public BinarySignal getBinarySignalOfExtremesValuesFiltered(ExtremesBinarySignal extremesBinarySignal, double minYDistance) {
        double[] YDistances = valueOf(this.getSignalOfYDistancesBetweenExtremes(extremesBinarySignal).getSignalData());
        boolean[] keepValues = new boolean[YDistances.length + 1];

        for (int i = 0; i < YDistances.length; i++) {
            if (YDistances[i] > minYDistance) {
                keepValues[i] = true;
                keepValues[i+1] = true;
            }
        }

        return new BinarySignal(keepValues);
    }

    public ExtremesBinarySignal getExtremesBinarySignalFiltered(ExtremesBinarySignal extremesBinarySignal, double minYDistance) {
        boolean[] keepValues = BinarySignal.valueOf(this.getBinarySignalOfExtremesValuesFiltered(extremesBinarySignal, minYDistance).getSignalData());
        int[] extremesIndexes = extremesBinarySignal.findExtremesIndexes();
        boolean[] extremesBinarySignalFiltered = new boolean[extremesBinarySignal.getSignalLength()];

        for (int i = 0; i < keepValues.length; i++) {
            if (keepValues[i]) {
                extremesBinarySignalFiltered[extremesIndexes[i]] = true;
            }
        }

        return new ExtremesBinarySignal(extremesBinarySignalFiltered);

    }

    public static NumericalSignal getEuclideanMetricSignal(NumericalSignal[] axisNumericalSignals) {
        int signalLength = axisNumericalSignals[0].signalLength;
        int axesLength = axisNumericalSignals.length;
        double[][] signalData = new double[axesLength][signalLength];
        double[] newSignalData = new double[signalLength];
        double tempSum;

        for (int j = 0; j < axesLength; j++) {
            signalData[j] = NumericalSignal.valueOf(axisNumericalSignals[j].getSignalData());
        }

        for (int i = 0; i < signalLength; i++) {
            tempSum = 0;
            for (int j = 0; j < axesLength; j++) {
                tempSum += pow(signalData[j][i], 2);
            }
            newSignalData[i] = sqrt(tempSum);
        }
        return new NumericalSignal(newSignalData);
    }

    public Double findAggregatedXDistanceBetweenExtremes(ExtremesBinarySignal extremesBinarySignal, Values distanceAggregation) {
        return extremesBinarySignal.findAggregatedDistancesBetweenUnits(distanceAggregation);
    }

    private static String getTemplateOfElement(int numberOfCharactersAfterPoint) {
        return  "%." + numberOfCharactersAfterPoint + "f";
    }

    @NonNull
    @Override
    public String toString() {
        String TEMPLATE_OF_NUMBER = getTemplateOfElement(NUMBER_OF_CHARACTERS_AFTER_POINT);
        return String.format(TEMPLATE, this.signalToString(MAX_ARRAY_LENGTH_TO_PRINT, TEMPLATE_OF_NUMBER, SEPARATOR),
                this.signalLength, String.format(TEMPLATE_OF_NUMBER, this.findMin()),
                String.format(TEMPLATE_OF_NUMBER, this.findMax()),String.format(TEMPLATE_OF_NUMBER, this.findMean()),
                String.format(TEMPLATE_OF_NUMBER, this.findSigma()));
    }

    public Double findValue(Values value) {
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
                return (double) this.signalLength;
            default:
                return (double) 0;
        }
    }
}
