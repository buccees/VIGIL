package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.EntityType;
import com.buccees.vigil.world.Track;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** A track plus the source/frame/time metadata required by spatial-temporal fusion. */
public record FusionEvidence(
        String sourceId,
        Track track,
        String frameId,
        Instant eventTime,
        Instant ingestionTime,
        Double positionUncertaintyMeters
) {
    public FusionEvidence {
        requireText(sourceId, "sourceId");
        Objects.requireNonNull(track, "track");
        requireText(frameId, "frameId");
        Objects.requireNonNull(eventTime, "eventTime");
        Objects.requireNonNull(ingestionTime, "ingestionTime");
        if (positionUncertaintyMeters != null
                && (!Double.isFinite(positionUncertaintyMeters) || positionUncertaintyMeters < 0.0)) {
            throw new IllegalArgumentException("positionUncertaintyMeters must be finite and non-negative");
        }
    }

    public String evidenceId() {
        return sourceId + ":" + track.id();
    }

    public LocalPosition position() {
        return track.position();
    }

    public EntityType type() {
        return track.type();
    }

    public Confidence confidence() {
        return track.confidence();
    }

    public Duration ageAt(Instant now) {
        Objects.requireNonNull(now, "now");
        return Duration.between(eventTime, now);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
