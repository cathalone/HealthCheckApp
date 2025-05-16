package com.example.healthcheckapplication.signals;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class ComplexNumber {
    private final double real;
    private final double imaginary;

    public ComplexNumber(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public double getImaginary() {
        return imaginary;
    }

    public double getReal() {
        return real;
    }

    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(this.real + other.real, this.imaginary + other.imaginary);
    }

    public ComplexNumber negative() {
        return new ComplexNumber(-this.real, -this.imaginary);
    }

    public ComplexNumber multiply(ComplexNumber other) {
        return new ComplexNumber((this.real * other.real) - (this.imaginary * other.imaginary), (this.real * other.imaginary) + (this.imaginary * other.real));
    }

    public ComplexNumber multiply(double other) {
        return new ComplexNumber(this.real * other, this.imaginary * other);
    }

    public ComplexNumber inverse() {
        double divider = this.real * this.real + this.imaginary * this.imaginary;
        return new ComplexNumber(this.real / divider, -this.imaginary / divider);
    }

    public double abs() {
        return sqrt(this.real * this.real + this.imaginary * this.imaginary);
    }

    @Override
    public String toString() {
        if (this.imaginary == 0 && this.real == 0) {
            return "0.0";
        }
        String sign = "+";
        if (this.imaginary < 0) {
            sign = "-";
        }
        String real = "";
        if (this.real != 0) {
            real = String.valueOf(this.real);
        }
        String imaginary = "";
        if (this.imaginary != 0) {
            if (Math.abs(this.imaginary) != 1) {
                imaginary = sign + Math.abs(this.imaginary) + "i";
            } else {
                imaginary = sign + "i";
            }
        }

        return real + imaginary;
    }
}
