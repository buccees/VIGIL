package com.buccees.vigil.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record InteractionProvenance(
        String requestId,
        String sessionId,
        List<String> sourceEntityIds,
        Instant sourceObservationTime,
        Instant responseTime,
        String interpretation
) {
    public InteractionProvenance {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        Objects.requireNonNull(sourceEntityIds, "sourceEntityIds");
        sourceEntityIds = List.copyOf(sourceEntityIds);
        Objects.requireNonNull(sourceObservationTime, "sourceObservationTime");
        Objects.requireNonNull(responseTime, "responseTime");
        if (responseTime.isBefore(sourceObservationTime)) throw new IllegalArgumentException("responseTime must not precede sourceObservationTime");
        if (interpretation == null || interpretation.isBlank()) throw new IllegalArgumentException("interpretation must not be blank");
    }

    public boolean isFreshAt(Instant now, Duration maxAge) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(maxAge, "maxAge");
        if (maxAge.isNegative()) throw new IllegalArgumentException("maxAge must not be negative");
        return !sourceObservationTime.isAfter(now) && !sourceObservationTime.plus(maxAge).isBefore(now);
    }
}
