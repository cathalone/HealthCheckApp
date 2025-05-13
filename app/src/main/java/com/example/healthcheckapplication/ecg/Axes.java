package com.example.healthcheckapplication.ecg;

import com.example.healthcheckapplication.signals.Signals;

public enum Axes {
    X(0),
    Y(1),
    Z(2);
    private final int id;

    Axes(int id) {
        this.id = id;
    }

    public static Axes getAxesById(int id) {
        for (Axes axes : Axes.values()) {
            if (axes.id == id) {
                return axes;
            }
        }
        return Axes.X;
    }

    public int getId() {
        return this.id;
    }

}
