package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;

import java.util.Objects;

/** Small deterministic translation between two spatial frames. */
public record SpatialTransform(
        String sourceFrameId,
        String destinationFrameId,
        LocalPosition translationMeters,
        boolean valid,
        String provenance
) {
    public SpatialTransform {
        requireText(sourceFrameId, "sourceFrameId");
        requireText(destinationFrameId, "destinationFrameId");
        Objects.requireNonNull(translationMeters, "translationMeters");
        requireText(provenance, "provenance");
    }

    /** Applies this frame relationship without changing the source evidence itself. */
    public LocalPosition apply(LocalPosition sourcePosition) {
        Objects.requireNonNull(sourcePosition, "sourcePosition");
        if (!valid) throw new IllegalStateException("Cannot apply an invalid spatial transform");
        return new LocalPosition(
                sourcePosition.xM() + translationMeters.xM(),
                sourcePosition.yM() + translationMeters.yM(),
                sourcePosition.zM() + translationMeters.zM());
    }

    /** Applies the translation-only frame relationship to a velocity vector. */
    public LocalPosition applyVelocity(LocalPosition sourceVelocityMetersPerSecond) {
        Objects.requireNonNull(sourceVelocityMetersPerSecond, "sourceVelocityMetersPerSecond");
        if (!valid) throw new IllegalStateException("Cannot apply an invalid spatial transform");
        return sourceVelocityMetersPerSecond;
    }

    public boolean connects(String source, String destination) {
        return sourceFrameId.equals(source) && destinationFrameId.equals(destination);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
