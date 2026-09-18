package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public record HumanInteractionResponse(
        String requestId,
        ResponseStatus status,
        String message,
        Instant responseTime,
        String groundingSummary
) {
    public HumanInteractionResponse {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        Objects.requireNonNull(status, "status");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("message must not be blank");
        Objects.requireNonNull(responseTime, "responseTime");
        if (groundingSummary == null || groundingSummary.isBlank())
            throw new IllegalArgumentException("groundingSummary must not be blank");
    }

    public enum ResponseStatus { ANSWERED, CLARIFICATION_REQUIRED, UNAVAILABLE, UNAUTHORIZED, AUTHENTICATION_REQUIRED }
}
