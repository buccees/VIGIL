package com.buccees.vigil.interaction;

import com.buccees.vigil.attention.AttentionItem;
import java.time.Instant;
import java.util.Objects;

public record AttentionInteractionHandoff(
        String requestId,
        String sessionId,
        String worldEntityId,
        double priority,
        double relevance,
        String prompt,
        Instant handoffTime
) {
    public AttentionInteractionHandoff {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        if (worldEntityId == null || worldEntityId.isBlank()) throw new IllegalArgumentException("worldEntityId must not be blank");
        if (!Double.isFinite(priority) || !Double.isFinite(relevance)) throw new IllegalArgumentException("priority and relevance must be finite");
        if (prompt == null || prompt.isBlank()) throw new IllegalArgumentException("prompt must not be blank");
        Objects.requireNonNull(handoffTime, "handoffTime");
    }

    public static AttentionInteractionHandoff from(String requestId, String sessionId, AttentionItem item, Instant now) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(now, "now");
        return new AttentionInteractionHandoff(
                requestId, sessionId, item.worldEntityId(), item.priority(), item.relevance(),
                "Attention item requires human review: " + item.worldEntityId(), now);
    }
}
