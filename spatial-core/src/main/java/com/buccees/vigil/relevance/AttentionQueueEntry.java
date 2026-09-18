package com.buccees.vigil.relevance;

import java.util.Objects;

public record AttentionQueueEntry(
        String entityId,
        PresentationDecision decision
) {
    public AttentionQueueEntry {
        if (entityId == null || entityId.isBlank()) throw new IllegalArgumentException("entityId must not be blank");
        Objects.requireNonNull(decision, "decision");
        if (!entityId.equals(decision.entityId())) throw new IllegalArgumentException("entityId must match presentation decision");
    }
}
