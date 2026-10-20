package com.buccees.vigil.input;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Derived interpretation; it never replaces the raw Coreless event. */
public record InterpretedInputEvent(
        String interpretationId,
        String kind,
        String sourceDeviceId,
        long timestampNs,
        List<Long> sourceSequences,
        Map<String, Object> metadata,
        Double confidence
) {
    public InterpretedInputEvent {
        if (interpretationId == null || interpretationId.isBlank()) {
            throw new IllegalArgumentException("interpretationId must not be blank");
        }
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("kind must not be blank");
        }
        if (sourceDeviceId == null || sourceDeviceId.isBlank()) {
            throw new IllegalArgumentException("sourceDeviceId must not be blank");
        }
        if (timestampNs < 0) {
            throw new IllegalArgumentException("timestampNs must be non-negative");
        }
        if (sourceSequences == null || sourceSequences.isEmpty()) {
            throw new IllegalArgumentException("sourceSequences must not be empty");
        }
        sourceSequences = List.copyOf(sourceSequences);
        long previous = -1;
        for (Long sequence : sourceSequences) {
            if (sequence == null || sequence < 0 || sequence <= previous) {
                throw new IllegalArgumentException("sourceSequences must increase monotonically");
            }
            previous = sequence;
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
        if (confidence != null && (!Double.isFinite(confidence) || confidence < 0.0 || confidence > 1.0)) {
            throw new IllegalArgumentException("confidence must be finite and between 0 and 1");
        }
    }
}
