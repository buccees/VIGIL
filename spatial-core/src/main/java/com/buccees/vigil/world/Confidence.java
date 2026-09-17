package com.buccees.vigil.world;

/** Confidence score in the closed interval [0, 1]. */
public record Confidence(double value) {
    public Confidence {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Confidence must be finite and between 0 and 1");
        }
    }

    public static Confidence certain() { return new Confidence(1.0); }
}
