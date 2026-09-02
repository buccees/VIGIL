package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic nearest-neighbor tracker for the first Spatial Core milestone.
 *
 * <p>Tracks are associated only by spatial proximity and entity type. This class does not
 * infer identity beyond the evidence supplied by detections.</p>
 */
public final class TrackManager {
    private final double associationDistanceMeters;
    private final Map<String, Track> tracks = new LinkedHashMap<>();
    private long nextTrackNumber = 1;

    public TrackManager(double associationDistanceMeters) {
        if (!Double.isFinite(associationDistanceMeters) || associationDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("associationDistanceMeters must be finite and positive");
        }
        this.associationDistanceMeters = associationDistanceMeters;
    }

    public synchronized Track update(Detection detection) {
        Track match = tracks.values().stream()
                .filter(track -> track.type() == detection.type())
                .filter(track -> track.position().distanceTo(detection.position()) <= associationDistanceMeters)
                .min(Comparator.comparingDouble(track -> track.position().distanceTo(detection.position())))
                .orElse(null);

        Track updated = match == null ? createTrack(detection) : updateTrack(match, detection);
        tracks.put(updated.id(), updated);
        return updated;
    }

    public synchronized List<Track> snapshot() {
        return List.copyOf(tracks.values());
    }

    public synchronized void clear() {
        tracks.clear();
        nextTrackNumber = 1;
    }

    private Track createTrack(Detection detection) {
        String id = "track-" + nextTrackNumber++;
        return new Track(
                id,
                detection.type(),
                detection.position(),
                new LocalPosition(0.0, 0.0, 0.0),
                detection.confidence(),
                detection.detectedAt(),
                List.of(detection.id()));
    }

    private Track updateTrack(Track previous, Detection detection) {
        double seconds = Duration.between(previous.lastUpdated(), detection.detectedAt()).toNanos() / 1_000_000_000.0;
        LocalPosition velocity = seconds > 0.0
                ? velocity(previous.position(), detection.position(), seconds)
                : previous.velocityMetersPerSecond();

        List<String> detectionIds = new ArrayList<>(previous.detectionIds());
        detectionIds.add(detection.id());

        return new Track(
                previous.id(),
                detection.type(),
                detection.position(),
                velocity,
                detection.confidence(),
                detection.detectedAt(),
                detectionIds);
    }

    private static LocalPosition velocity(LocalPosition from, LocalPosition to, double seconds) {
        return new LocalPosition(
                (to.xM() - from.xM()) / seconds,
                (to.yM() - from.yM()) / seconds,
                (to.zM() - from.zM()) / seconds);
    }
}
