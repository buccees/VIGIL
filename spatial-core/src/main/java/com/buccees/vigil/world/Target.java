package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.util.Objects;

/** Generic informational spatial objective; independent of weapons or actuators. */
public record Target(String id, LocalPosition position, String entityId, boolean active) {
    public Target {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        Objects.requireNonNull(position, "position");
        if (entityId != null && entityId.isBlank()) throw new IllegalArgumentException("entityId must not be blank");
    }
}
