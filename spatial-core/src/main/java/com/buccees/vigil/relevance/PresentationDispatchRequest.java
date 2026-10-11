package com.buccees.vigil.relevance;

import java.util.Objects;

public record PresentationDispatchRequest(
        String entityId,
        String explanation
) {
    public PresentationDispatchRequest {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId must not be blank");
        }
        Objects.requireNonNull(explanation, "explanation");
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
    }
}
