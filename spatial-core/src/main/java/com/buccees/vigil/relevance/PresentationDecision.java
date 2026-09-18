package com.buccees.vigil.relevance;

import java.util.Objects;

public record PresentationDecision(
        String entityId,
        PresentationAction action,
        double priority,
        String explanation
) {
    public PresentationDecision {
        if (entityId == null || entityId.isBlank()) throw new IllegalArgumentException("entityId must not be blank");
        Objects.requireNonNull(action, "action");
        if (!Double.isFinite(priority) || priority < 0.0 || priority > 1.0)
            throw new IllegalArgumentException("priority must be between 0 and 1");
        if (explanation == null || explanation.isBlank()) throw new IllegalArgumentException("explanation must not be blank");
    }
}
