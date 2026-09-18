package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public final class HumanConfirmationService {
    public HumanConfirmation required(HumanInteractionRequest request, String operation, String scope, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        return new HumanConfirmation(
                request.requestId() + "-confirmation",
                request.requestId(),
                request.sessionId(),
                requireNonBlank(operation, "operation"),
                requireNonBlank(scope, "scope"),
                ConfirmationStatus.REQUIRED,
                now);
    }

    public HumanConfirmation confirm(HumanConfirmationRequest request, Instant now) {
        return decide(request, ConfirmationStatus.CONFIRMED, now);
    }

    public HumanConfirmation decline(HumanConfirmationRequest request, Instant now) {
        return decide(request, ConfirmationStatus.DECLINED, now);
    }

    private HumanConfirmation decide(HumanConfirmationRequest request, ConfirmationStatus status, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        return new HumanConfirmation(request.confirmationId(), request.requestId(), request.sessionId(),
                request.operation(), request.scope(), status, now);
    }

    private String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
