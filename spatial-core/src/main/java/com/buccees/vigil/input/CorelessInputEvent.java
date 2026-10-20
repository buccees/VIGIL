package com.buccees.vigil.input;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable envelope for one raw Coreless input event (ABI v1).
 *
 * VIGIL consumes this event as evidence. It must not mutate or replace the
 * raw input history owned by Coreless.
 */
public record CorelessInputEvent(
        int abiVersion,
        InputEventKind eventType,
        String deviceId,
        long timestampNs,
        long sequence,
        CoordinateFrame coordinateFrame,
        Double x,
        Double y,
        Integer contactId,
        Double pressure,
        Integer button,
        Map<String, Object> metadata
) {
    public static final int ABI_VERSION = 1;

    public CorelessInputEvent {
        if (abiVersion != ABI_VERSION) {
            throw new IllegalArgumentException("unsupported Coreless input ABI version");
        }
        Objects.requireNonNull(eventType, "eventType");
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
        if (timestampNs < 0) {
            throw new IllegalArgumentException("timestampNs must be non-negative");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must be non-negative");
        }
        Objects.requireNonNull(coordinateFrame, "coordinateFrame");
        if ((x == null) != (y == null)) {
            throw new IllegalArgumentException("x and y must be supplied together");
        }
        if (x != null && (!Double.isFinite(x) || !Double.isFinite(y))) {
            throw new IllegalArgumentException("coordinates must be finite");
        }
        if (pressure != null && !Double.isFinite(pressure)) {
            throw new IllegalArgumentException("pressure must be finite");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (key == null || key.isBlank()) {
                    throw new IllegalArgumentException("metadata keys must not be blank");
                }
                copy.put(key, value);
            });
        }
        metadata = Collections.unmodifiableMap(copy);
    }
}
