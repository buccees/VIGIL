package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Small, deterministic first-pass fusion engine. It deliberately avoids probabilistic identity
 * resolution and heavyweight state estimation; those can be introduced behind this boundary later.
 */
public final class DeterministicFusionEngine {
    private final FusionPolicy policy;

    public DeterministicFusionEngine(FusionPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Attempts to fuse compatible evidence. Evidence is never written to the World Model here.
     */
    public Optional<FusedEstimate> fuse(List<FusionEvidence> evidence, Instant fusionTime) {
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(fusionTime, "fusionTime");
        if (evidence.isEmpty()) return Optional.empty();

        List<FusionEvidence> valid = evidence.stream()
                .filter(Objects::nonNull)
                .filter(this::isValid)
                .sorted(Comparator.comparing(FusionEvidence::evidenceId))
                .toList();
        if (valid.isEmpty()) return Optional.empty();

        FusionEvidence first = valid.get(0);
        List<FusionEvidence> compatible = new ArrayList<>();
        for (FusionEvidence candidate : valid) {
            if (!candidate.frameId().equals(first.frameId())) continue;
            if (candidate.type() != first.type()) continue;
            if (temporalSkew(first.eventTime(), candidate.eventTime()).compareTo(policy.maxEventTimeSkew()) > 0) continue;
            if (first.position().distanceTo(candidate.position()) > policy.conflictDistanceMeters()) continue;
            compatible.add(candidate);
        }
        if (compatible.isEmpty()) return Optional.empty();

        boolean conflict = false;
        for (int i = 0; i < compatible.size(); i++) {
            for (int j = i + 1; j < compatible.size(); j++) {
                double distance = compatible.get(i).position().distanceTo(compatible.get(j).position());
                if (distance > policy.maxAssociationDistanceMeters()) {
                    conflict = true;
                }
            }
        }

        // A material conflict is not hidden by averaging. Keep the strongest deterministic subset.
        if (conflict) {
            FusionEvidence strongest = compatible.stream()
                    .max(Comparator.comparing((FusionEvidence e) -> e.confidence().value())
                            .thenComparing(FusionEvidence::evidenceId).reversed())
                    .orElseThrow();
            compatible = List.of(strongest);
        }

        double totalWeight = compatible.stream().mapToDouble(e -> Math.max(e.confidence().value(), 1.0e-9)).sum();
        double x = 0, y = 0, z = 0;
        double vx = 0, vy = 0, vz = 0;
        double weightedConfidence = 0;
        Set<String> sources = new LinkedHashSet<>();
        Set<String> tracks = new LinkedHashSet<>();
        Set<String> detections = new LinkedHashSet<>();
        boolean allHaveUncertainty = true;
        double weightedUncertainty = 0;
        Instant latestEvent = compatible.stream().map(FusionEvidence::eventTime).max(Instant::compareTo).orElseThrow();

        for (FusionEvidence item : compatible) {
            double weight = Math.max(item.confidence().value(), 1.0e-9);
            double fraction = weight / totalWeight;
            x += item.position().xM() * fraction;
            y += item.position().yM() * fraction;
            z += item.position().zM() * fraction;
            vx += item.track().velocityMetersPerSecond().xM() * fraction;
            vy += item.track().velocityMetersPerSecond().yM() * fraction;
            vz += item.track().velocityMetersPerSecond().zM() * fraction;
            weightedConfidence += item.confidence().value() * fraction;
            sources.add(item.sourceId());
            tracks.add(item.track().id());
            detections.addAll(item.track().detectionIds());
            if (item.positionUncertaintyMeters() == null) allHaveUncertainty = false;
            else weightedUncertainty += item.positionUncertaintyMeters() * fraction;
        }

        String associationId = tracks.stream().sorted().findFirst().orElseThrow();
        String qualityNote = conflict
                ? "Material disagreement detected; fused estimate uses the strongest deterministic evidence."
                : compatible.size() == 1
                    ? "Single compatible evidence item; no cross-source fusion performed."
                    : "Compatible evidence fused using confidence-weighted deterministic averaging.";

        return Optional.of(new FusedEstimate(
                associationId,
                first.type(),
                new LocalPosition(x, y, z),
                new LocalPosition(vx, vy, vz),
                new com.buccees.vigil.world.Confidence(weightedConfidence),
                allHaveUncertainty ? OptionalDouble.of(weightedUncertainty) : OptionalDouble.empty(),
                fusionTime,
                latestEvent,
                List.copyOf(sources),
                List.copyOf(tracks),
                List.copyOf(detections),
                conflict,
                qualityNote));
    }

    private boolean isValid(FusionEvidence evidence) {
        if (evidence.track().position() == null || evidence.track().velocityMetersPerSecond() == null) return false;
        if (evidence.eventTime() == null || evidence.ingestionTime() == null) return false;
        return !evidence.frameId().isBlank();
    }

    private static Duration temporalSkew(Instant a, Instant b) {
        return Duration.between(a, b).abs();
    }
}
