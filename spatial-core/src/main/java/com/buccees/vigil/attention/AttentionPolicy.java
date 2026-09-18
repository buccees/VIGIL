package com.buccees.vigil.attention;

public record AttentionPolicy(
        double minimumPriority,
        int maximumActiveItems,
        long persistenceMillis
) {
    public AttentionPolicy {
        if (!Double.isFinite(minimumPriority) || minimumPriority < 0.0 || minimumPriority > 1.0)
            throw new IllegalArgumentException("minimumPriority must be between 0 and 1");
        if (maximumActiveItems < 1) throw new IllegalArgumentException("maximumActiveItems must be >= 1");
        if (persistenceMillis < 0) throw new IllegalArgumentException("persistenceMillis must be >= 0");
    }

    public static AttentionPolicy defaults() {
        return new AttentionPolicy(0.25, 5, 10_000);
    }
}
