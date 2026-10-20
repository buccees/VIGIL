package com.buccees.vigil.input;

/** Coordinate-frame names shared with Coreless Input ABI version 1. */
public enum CoordinateFrame {
    HOST("host"),
    CORELESS("coreless"),
    DISPLAY("display"),
    VIGIL("vigil");

    private final String wireValue;

    CoordinateFrame(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
