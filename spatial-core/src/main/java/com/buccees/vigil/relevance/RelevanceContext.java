package com.buccees.vigil.relevance;

import com.buccees.vigil.spatial.LocalPosition;
import java.util.Objects;

/** Explicit user context used to determine information relevance. */
public record RelevanceContext(
        LocalPosition userPosition,
        String task
) {
    public RelevanceContext {
        Objects.requireNonNull(userPosition, "userPosition");
        if (task == null || task.isBlank()) {
            throw new IllegalArgumentException("task must not be blank");
        }
    }
}
