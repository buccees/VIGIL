package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public record HumanConfirmationRequest(
        String confirmationId,
        String requestId,
        String sessionId,
        String operation,
        String scope,
        Instant requestedAt
) {
    public HumanConfirmationRequest {
        if (confirmationId == null || confirmationId.isBlank()) throw new IllegalArgumentException("confirmationId must not be blank");
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        if (operation == null || operation.isBlank()) throw new IllegalArgumentException("operation must not be blank");
        if (scope == null || scope.isBlank()) throw new IllegalArgumentException("scope must not be blank");
        Objects.requireNonNull(requestedAt, "requestedAt");
    }
}
