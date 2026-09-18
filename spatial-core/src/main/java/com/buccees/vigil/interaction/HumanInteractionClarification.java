package com.buccees.vigil.interaction;

import java.util.Objects;

public record HumanInteractionClarification(
        String requestId,
        ClarificationReason reason,
        String question
) {
    public HumanInteractionClarification {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        Objects.requireNonNull(reason, "reason");
        if (question == null || question.isBlank()) throw new IllegalArgumentException("question must not be blank");
    }
}
