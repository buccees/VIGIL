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

    public HumanConfirmation confirm(HumanConfirmation required, HumanConfirmationRequest request, Instant now) {
        Objects.requireNonNull(required, "required");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        requireRequiredMatch(required, request);
        return new HumanConfirmation(required.confirmationId(), required.requestId(), required.sessionId(),
                required.operation(), required.scope(), ConfirmationStatus.CONFIRMED, now);
    }

    public HumanConfirmation decline(HumanConfirmation required, HumanConfirmationRequest request, Instant now) {
        Objects.requireNonNull(required, "required");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        requireRequiredMatch(required, request);
        return new HumanConfirmation(required.confirmationId(), required.requestId(), required.sessionId(),
                required.operation(), required.scope(), ConfirmationStatus.DECLINED, now);
    }

    private void requireRequiredMatch(HumanConfirmation required, HumanConfirmationRequest request) {
        if (required.status() != ConfirmationStatus.REQUIRED) {
            throw new IllegalStateException("Confirmation is not awaiting a human decision");
        }
        if (!required.confirmationId().equals(request.confirmationId())
                || !required.requestId().equals(request.requestId())
                || !required.sessionId().equals(request.sessionId())
                || !required.operation().equals(request.operation())
                || !required.scope().equals(request.scope())) {
            throw new IllegalArgumentException("Confirmation request does not match the required confirmation");
        }
        if (request.requestedAt().isBefore(required.decidedAt())) {
            throw new IllegalArgumentException("Confirmation request predates the confirmation request");
        }
    }

    private String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
