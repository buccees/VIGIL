package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.TrackLifecycleState;

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
    private final List<SpatialTransform> transforms;

    public DeterministicFusionEngine(FusionPolicy policy) {
        this(policy, List.of());
    }

    public DeterministicFusionEngine(FusionPolicy policy, List<SpatialTransform> transforms) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.transforms = List.copyOf(Objects.requireNonNull(transforms, "transforms"));
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

        List<FusionExclusion> exclusions = new ArrayList<>();
        List<FusionEvidence> temporallyEligible = new ArrayList<>();
        for (FusionEvidence candidate : valid) {
            if (candidate.track().lifecycleState() == TrackLifecycleState.STALE
                    || candidate.track().lifecycleState() == TrackLifecycleState.TERMINATED) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INVALID_SOURCE_STATE));
                continue;
            }

            Duration age = candidate.ageAt(fusionTime);
            if (age.isNegative() || age.compareTo(policy.maxEvidenceAge()) > 0) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.STALE_OR_FUTURE_DATED));
                continue;
            }
            temporallyEligible.add(candidate);
        }
        if (temporallyEligible.isEmpty()) return new FusionResult(Optional.empty(), List.copyOf(exclusions));

        FusionEvidence first = temporallyEligible.get(0);
        List<FusionEvidence> compatible = new ArrayList<>();
        List<ResolvedEvidence> resolved = new ArrayList<>();
        for (FusionEvidence candidate : temporallyEligible) {
            if (candidate.type() != first.type()) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INCOMPATIBLE_TYPE));
                continue;
            }
            SpatialTransform transform = transformFor(candidate.frameId(), first.frameId());
            if (!candidate.frameId().equals(first.frameId()) && transform == null) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INCOMPATIBLE_FRAME));
                continue;
            }
            if (transform != null && !transform.valid()) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.INVALID_TRANSFORM));
                continue;
            }
            LocalPosition position = transform == null ? candidate.position() : transform.apply(candidate.position());
            if (temporalSkew(first.eventTime(), candidate.eventTime()).compareTo(policy.maxEventTimeSkew()) > 0) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.TEMPORAL_SKEW));
                continue;
            }
            if (first.position().distanceTo(position) > policy.conflictDistanceMeters()) {
                exclusions.add(new FusionExclusion(candidate.evidenceId(), FusionExclusionReason.OUTSIDE_CONFLICT_DISTANCE));
                continue;
            }
            compatible.add(candidate);
            resolved.add(new ResolvedEvidence(candidate, position, transform));
        }
        if (compatible.isEmpty()) return new FusionResult(Optional.empty(), List.copyOf(exclusions));

        boolean conflict = false;
        for (int i = 0; i < resolved.size(); i++) {
            for (int j = i + 1; j < resolved.size(); j++) {
                if (resolved.get(i).position().distanceTo(resolved.get(j).position())
                        > policy.maxAssociationDistanceMeters()) {
                    conflict = true;
                }
            }
        }

        if (conflict) {
            ResolvedEvidence strongest = resolved.stream()
                    .max(Comparator.comparingDouble((ResolvedEvidence e) -> e.evidence().confidence().value())
                            .thenComparing(e -> e.evidence().evidenceId(), Comparator.reverseOrder()))
                    .orElseThrow();
            for (ResolvedEvidence item : resolved) {
                if (!item.evidence().evidenceId().equals(strongest.evidence().evidenceId())) {
                    exclusions.add(new FusionExclusion(item.evidence().evidenceId(), FusionExclusionReason.MATERIAL_DISAGREEMENT));
                }
            }
            compatible = List.of(strongest.evidence());
            resolved = List.of(strongest);
        }

        double totalWeight = resolved.stream()
                .mapToDouble(e -> Math.max(e.evidence().confidence().value(), 1.0e-9)).sum();
        double x = 0, y = 0, z = 0;
        double vx = 0, vy = 0, vz = 0;
        double weightedConfidence = 0;
        double weightedUncertainty = 0;
        boolean allHaveUncertainty = true;
        boolean includesDegradedEvidence = false;
        Set<String> sources = new LinkedHashSet<>();
        Set<String> tracks = new LinkedHashSet<>();
        Set<String> detections = new LinkedHashSet<>();
        Set<String> transformProvenance = new LinkedHashSet<>();
        Instant latestEvent = resolved.stream().map(e -> e.evidence().eventTime()).max(Instant::compareTo).orElseThrow();

        for (ResolvedEvidence item : resolved) {
            FusionEvidence evidenceItem = item.evidence();
            double weight = Math.max(evidenceItem.confidence().value(), 1.0e-9);
            double fraction = weight / totalWeight;
            x += item.position().xM() * fraction;
            y += item.position().yM() * fraction;
            z += item.position().zM() * fraction;
            vx += evidenceItem.track().velocityMetersPerSecond().xM() * fraction;
            vy += evidenceItem.track().velocityMetersPerSecond().yM() * fraction;
            vz += evidenceItem.track().velocityMetersPerSecond().zM() * fraction;
            weightedConfidence += evidenceItem.confidence().value() * fraction;
            sources.add(evidenceItem.sourceId());
            tracks.add(evidenceItem.track().id());
            detections.addAll(evidenceItem.track().detectionIds());
            if (item.transform() != null) transformProvenance.add(item.transform().provenance());
            if (evidenceItem.track().lifecycleState() == TrackLifecycleState.DEGRADED) includesDegradedEvidence = true;
            if (evidenceItem.positionUncertaintyMeters() == null) allHaveUncertainty = false;
            else weightedUncertainty += evidenceItem.positionUncertaintyMeters() * fraction;
        }

        String associationId = tracks.stream().sorted().findFirst().orElseThrow();
        String qualityNote = conflict
                ? "Material disagreement detected; estimate uses the strongest deterministic evidence and is not cross-source qualified."
                : includesDegradedEvidence
                    ? "Degraded evidence contributed under the configured deterministic fusion policy; result is not qualified as fully healthy evidence."
                    : compatible.size() == 1
                        ? "Single compatible evidence item; no cross-source fusion performed."
                        : "Compatible evidence fused using confidence-weighted deterministic averaging.";

        FusedEstimate estimate = new FusedEstimate(
                associationId, first.type(), new LocalPosition(x, y, z), new LocalPosition(vx, vy, vz),
                new Confidence(weightedConfidence),
                allHaveUncertainty ? OptionalDouble.of(weightedUncertainty) : OptionalDouble.empty(),
                fusionTime, latestEvent, List.copyOf(sources), List.copyOf(tracks), List.copyOf(detections),
                List.copyOf(transformProvenance), !conflict && !includesDegradedEvidence, qualityNote);
        return new FusionResult(Optional.of(estimate), List.copyOf(exclusions));
    }

    private SpatialTransform transformFor(String sourceFrame, String destinationFrame) {
        return transforms.stream()
                .filter(transform -> transform.connects(sourceFrame, destinationFrame))
                .sorted(Comparator.comparing(SpatialTransform::provenance))
                .findFirst()
                .orElse(null);
    }

    private record ResolvedEvidence(FusionEvidence evidence, LocalPosition position, SpatialTransform transform) {}

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
        INVALID_TRANSFORM,
        INCOMPATIBLE_TYPE,
        TEMPORAL_SKEW,
        STALE_OR_FUTURE_DATED,
        INVALID_SOURCE_STATE,
        OUTSIDE_CONFLICT_DISTANCE,
        MATERIAL_DISAGREEMENT
    }

    private static Duration temporalSkew(Instant a, Instant b) {
        return Duration.between(a, b).abs();
    }
}
