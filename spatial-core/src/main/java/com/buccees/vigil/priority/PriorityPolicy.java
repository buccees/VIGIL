package com.buccees.vigil.priority;

/** Explicit deterministic scoring policy for the initial priority milestone. */
public record PriorityPolicy(
        double proximityWeight,
        double movementWeight,
        double stateChangeWeight,
        double zoneWeight,
        double taskWeight,
        double unexpectedWeight,
        double persistenceWeight,
        double proximityRangeMeters,
        double movementThresholdMetersPerSecond,
        double staleMultiplier,
        double agingMultiplier,
        double invalidPriorityCeiling
) {
    public PriorityPolicy {
        requireNonNegative(proximityWeight, "proximityWeight");
        requireNonNegative(movementWeight, "movementWeight");
        requireNonNegative(stateChangeWeight, "stateChangeWeight");
        requireNonNegative(zoneWeight, "zoneWeight");
        requireNonNegative(taskWeight, "taskWeight");
        requireNonNegative(unexpectedWeight, "unexpectedWeight");
        requireNonNegative(persistenceWeight, "persistenceWeight");
        if (!(proximityRangeMeters > 0.0) || !Double.isFinite(proximityRangeMeters)) throw new IllegalArgumentException("proximityRangeMeters must be finite and > 0");
        if (!(movementThresholdMetersPerSecond >= 0.0) || !Double.isFinite(movementThresholdMetersPerSecond)) throw new IllegalArgumentException("movementThresholdMetersPerSecond must be finite and >= 0");
        requireUnit(staleMultiplier, "staleMultiplier");
        requireUnit(agingMultiplier, "agingMultiplier");
        requireUnit(invalidPriorityCeiling, "invalidPriorityCeiling");
        double weightTotal = proximityWeight + movementWeight + stateChangeWeight + zoneWeight
                + taskWeight + unexpectedWeight + persistenceWeight;
        if (weightTotal <= 0.0) throw new IllegalArgumentException("At least one priority weight must be > 0");
    }

    public static PriorityPolicy defaults() {
        return new PriorityPolicy(1.0, 0.75, 1.0, 0.75, 1.25, 1.0, 0.5,
                100.0, 0.5, 0.35, 0.7, 0.2);
    }

    public double totalWeight() {
        return proximityWeight + movementWeight + stateChangeWeight + zoneWeight
                + taskWeight + unexpectedWeight + persistenceWeight;
    }

    private static void requireNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) throw new IllegalArgumentException(name + " must be finite and >= 0");
    }

    private static void requireUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) throw new IllegalArgumentException(name + " must be between 0 and 1");
    }
}
