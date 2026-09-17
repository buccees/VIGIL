package com.buccees.vigil.world;

import java.time.Instant;
import java.util.Objects;

/** Immutable source-level measurement or event. */
public record Observation(
        String id,
        String sourceId,
        Instant eventTime,
        Instant ingestTime,
        String sensorType,
        String payloadReference
) {
    public Observation {
        requireText(id, "id");
        requireText(sourceId, "sourceId");
        Objects.requireNonNull(eventTime, "eventTime");
        Objects.requireNonNull(ingestTime, "ingestTime");
        requireText(sensorType, "sensorType");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
