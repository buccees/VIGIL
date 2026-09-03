package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;

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

/** Small, deterministic first-pass fusion engine. */
public final class DeterministicFusionEngine {
    private final FusionPolicy policy;

    public DeterministicFusionEngine(FusionPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /** Attempts to fuse compatible evidence without mutating authoritative world state. */
    public Optional<FusedEstimate> fuse(List<FusionEvidence> evidence, Instant fusionTime) {
        return fuseDetailed(evidence, fusionTime).estimate();
    }

    /**
     * Performs deterministic fusion and exposes evidence exclusions alongside the estimate.
     * Exclusions are diagnostic output only and never mutate authoritative world state.
     */
    public FusionResult fuseDetailed(List<FusionEvidence> evidence, Instant fusionTime) {
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(fusionTime, "fusionTime");
        if (evidence.isEmpty()) return new FusionResult(Optional.empty(), List.of());

        List<FusionEvidence> valid = evidence.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(FusionEvidence::evidenceId))
                .toList();
        if (valid.isEmpty()) return new FusionResult(Optional.empty(), List.of());

        FusionEvidence first = valid.get(0);
        List<FusionExclusion> exclusions = new ArrayList<>();
        List<FusionEvidence> compatible = new ArrayList<>();
        for (FusionEvidence candidate : valid) {
            if (!candidate.frameId().equals(first.frameId())) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INCOMPATIBLE_FRAME));
                continue;
            }
            if (candidate.type() != first.type()) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INCOMPATIBLE_TYPE));
                continue;
            }
            if (temporalSkew(first.eventTime(), candidate.eventTime()).compareTo(policy.maxEventTimeSkew()) > 0) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.TEMPORAL_SKEW));
                continue;
            }
            if (first.position().distanceTo(candidate.position()) > policy.conflictDistanceMeters()) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.OUTSIDE_CONFLICT_DISTANCE));
                continue;
            }
            compatible.add(candidate);
        }
        if (compatible.isEmpty()) return new FusionResult(Optional.empty(), List.copyOf(exclusions));

        boolean conflict = false;
        for (int i = 0; i < compatible.size(); i++) {
            for (int j = i + 1; j < compatible.size(); j++) {
                if (compatible.get(i).position().distanceTo(compatible.get(j).position())
                        > policy.maxAssociationDistanceMeters()) {
                    conflict = true;
                }
            }
        }

        if (conflict) {
            FusionEvidence strongest = compatible.stream()
                    .max(Comparator.comparingDouble((FusionEvidence e) -> e.confidence().value())
                            .thenComparing(FusionEvidence::evidenceId, Comparator.reverseOrder()))
                    .orElseThrow();
            for (FusionEvidence item : compatible) {
                if (!item.evidenceId().equals(strongest.evidenceId())) {
                    exclusions.add(new FusionExclusion(item.evidenceId(), FusionExclusionReason.MATERIAL_DISAGREEMENT));
                }
            }
            compatible = List.of(strongest);
        }

        double totalWeight = compatible.stream()
                .mapToDouble(e -> Math.max(e.confidence().value(), 1.0e-9)).sum();
        double x = 0, y = 0, z = 0;
        double vx = 0, vy = 0, vz = 0;
        double weightedConfidence = 0;
        double weightedUncertainty = 0;
        boolean allHaveUncertainty = true;
        Set<String> sources = new LinkedHashSet<>();
        Set<String> tracks = new LinkedHashSet<>();
        Set<String> detections = new LinkedHashSet<>();
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
                ? "Material disagreement detected; estimate uses the strongest deterministic evidence and is not cross-source qualified."
                : compatible.size() == 1
                    ? "Single compatible evidence item; no cross-source fusion performed."
                    : "Compatible evidence fused using confidence-weighted deterministic averaging.";

        FusedEstimate estimate = new FusedEstimate(
                associationId, first.type(), new LocalPosition(x, y, z), new LocalPosition(vx, vy, vz),
                new Confidence(weightedConfidence),
                allHaveUncertainty ? OptionalDouble.of(weightedUncertainty) : OptionalDouble.empty(),
                fusionTime, latestEvent, List.copyOf(sources), List.copyOf(tracks), List.copyOf(detections),
                !conflict, qualityNote);
        return new FusionResult(Optional.of(estimate), List.copyOf(exclusions));
    }

    /** Structured diagnostic result for a fusion attempt. */
    public record FusionResult(Optional<FusedEstimate> estimate, List<FusionExclusion> exclusions) {
        public FusionResult {
            Objects.requireNonNull(estimate, "estimate");
            exclusions = List.copyOf(Objects.requireNonNull(exclusions, "exclusions"));
        }
    }

    /** Evidence excluded from the selected fusion set and the deterministic reason. */
    public record FusionExclusion(String evidenceId, FusionExclusionReason reason) {
        public FusionExclusion {
            Objects.requireNonNull(evidenceId, "evidenceId");
            Objects.requireNonNull(reason, "reason");
        }
    }

    public enum FusionExclusionReason {
        INCOMPATIBLE_FRAME,
        INCOMPATIBLE_TYPE,
        TEMPORAL_SKEW,
        OUTSIDE_CONFLICT_DISTANCE,
        MATERIAL_DISAGREEMENT
    }

    private static Duration temporalSkew(Instant a, Instant b) {
        return Duration.between(a, b).abs();
    }
}
