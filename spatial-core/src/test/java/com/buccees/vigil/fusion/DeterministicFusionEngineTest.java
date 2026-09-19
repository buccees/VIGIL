package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.EntityType;
import com.buccees.vigil.world.Track;
import com.buccees.vigil.world.TrackLifecycleState;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicFusionEngineTest {
    private static final Instant T0 = Instant.parse("2026-09-02T00:00:00Z");
    private final FusionPolicy policy = new FusionPolicy(Duration.ofMillis(500), 5.0, 20.0);
    private final DeterministicFusionEngine engine = new DeterministicFusionEngine(policy);

    @Test
    void eventTimeIsDistinctFromIngestionTimeWhenComputingFreshness() {
        FusionEvidence observedEarlierButIngestedLater = new FusionEvidence(
                "camera-a",
                track("track-a", 0, 0, 0, 0.8, 0),
                "local-world",
                T0,
                T0.plusSeconds(5),
                null);

        DeterministicFusionEngine.FusionResult result =
                engine.fuseDetailed(List.of(observedEarlierButIngestedLater), T0.plusSeconds(1));

        FusedEstimate estimate = result.estimate().orElseThrow();
        assertEquals(T0.plusSeconds(1), estimate.fusionTime());
        assertEquals(T0, estimate.latestEventTime());
        assertTrue(estimate.qualified());
    }

    @Test
    void compatibleEvidenceProducesDeterministicFusedEstimate() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, 2.0, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 2, 0, 0, 0.6, 4.0, 100);

        FusedEstimate result = engine.fuse(List.of(b, a), T0.plusMillis(200)).orElseThrow();

        assertEquals(0.8571428571, result.position().xM(), 1.0e-9);
        assertEquals(0.0, result.position().yM(), 1.0e-9);
        assertEquals(0.0, result.position().zM(), 1.0e-9);
        assertEquals(2.8571428571, result.positionUncertaintyMeters().orElseThrow(), 1.0e-9);
        assertEquals(List.of("camera-a", "camera-b"), result.sourceIds());
        assertEquals("fusion:track-a+track-b", result.deterministicAssociationKey());
        assertEquals(List.of("track-a", "track-b"), result.trackIds());
        assertTrue(result.transformProvenance().isEmpty());
        assertTrue(result.qualified());
    }

    @Test
    void validSpatialTransformAlignsEvidenceIntoFusionFrame() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = new FusionEvidence("camera-b", trackWithVelocity("track-b", -1, 0, 0, 0.8, 50,
                new LocalPosition(2, 0, 0)),
                "sensor-b", T0.plusMillis(50), T0.plusMillis(60), null);
        SpatialTransform transform = new SpatialTransform("sensor-b", "local-world",
                new LocalPosition(1, 0, 0), true, "calibration:sensor-b-to-local-world:v1");

        DeterministicFusionEngine transformedEngine = new DeterministicFusionEngine(policy, List.of(transform));
        FusedEstimate result = transformedEngine.fuse(List.of(a, b), T0.plusMillis(100)).orElseThrow();

        assertEquals(0.0, result.position().xM(), 1.0e-9);
        assertEquals(1.5, result.velocityMetersPerSecond().xM(), 1.0e-9);
        assertEquals(List.of("calibration:sensor-b-to-local-world:v1"), result.transformProvenance());
        assertEquals(List.of("camera-a", "camera-b"), result.sourceIds());
    }

    @Test
    void transformedCanonicalAnchorIsUsedForConflictChecks() {
        FusionEvidence transformedAnchor = new FusionEvidence(
                "camera-a",
                track("track-a", 0, 0, 0, 0.8, 0),
                "sensor-a",
                T0,
                T0.plusMillis(10),
                null);
        FusionEvidence localEvidence = evidence("camera-b", "track-b", 100, 0, 0, 0.8, null, 50);
        SpatialTransform transform = new SpatialTransform("sensor-a", "local-world",
                new LocalPosition(100, 0, 0), true, "calibration:sensor-a-to-local-world:v1");

        DeterministicFusionEngine transformedEngine = new DeterministicFusionEngine(policy, List.of(transform));
        FusedEstimate result = transformedEngine.fuse(List.of(transformedAnchor, localEvidence), T0.plusMillis(100))
                .orElseThrow();

        assertEquals(100.0, result.position().xM(), 1.0e-9);
        assertEquals(List.of("camera-a", "camera-b"), result.sourceIds());
        assertEquals(List.of("calibration:sensor-a-to-local-world:v1"), result.transformProvenance());
        assertTrue(result.qualified());
    }

    @Test
    void invalidSpatialTransformIsExplicitlyExcluded() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = new FusionEvidence("camera-b", track("track-b", -1, 0, 0, 0.8, 50),
                "sensor-b", T0.plusMillis(50), T0.plusMillis(60), null);
        SpatialTransform invalid = new SpatialTransform("sensor-b", "local-world",
                new LocalPosition(1, 0, 0), false, "calibration:sensor-b-to-local-world:invalid");

        DeterministicFusionEngine transformedEngine = new DeterministicFusionEngine(policy, List.of(invalid));
        DeterministicFusionEngine.FusionResult result = transformedEngine.fuseDetailed(List.of(a, b), T0.plusMillis(100));

        assertEquals(List.of("camera-a"), result.estimate().orElseThrow().sourceIds());
        assertEquals(1, result.exclusions().size());
        assertEquals("camera-b:track-b", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.INVALID_TRANSFORM,
                result.exclusions().get(0).reason());
    }

    @Test
    void incompatibleFramesDoNotFuse() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = new FusionEvidence("camera-b", track("track-b", 1, 0, 0, 0.8, 1), "other-frame", T0, T0, null);

        FusedEstimate result = engine.fuse(List.of(a, b), T0).orElseThrow();
        assertEquals(List.of("camera-a"), result.sourceIds());
        assertTrue(result.qualified());
    }

    @Test
    void incompatibleFrameIsExplicitlyExcluded() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = new FusionEvidence("camera-b", track("track-b", 1, 0, 0, 0.8, 1), "other-frame", T0, T0, null);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(a, b), T0);

        assertEquals(1, result.exclusions().size());
        assertEquals("camera-b:track-b", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.INCOMPATIBLE_FRAME,
                result.exclusions().get(0).reason());
    }

    @Test
    void missingUncertaintyRemainsUnknown() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 1, 0, 0, 0.8, 3.0, 100);

        FusedEstimate result = engine.fuse(List.of(a, b), T0.plusMillis(100)).orElseThrow();
        assertTrue(result.positionUncertaintyMeters().isEmpty());
        assertTrue(result.qualified());
    }

    @Test
    void materialConflictIsUnqualifiedAndDoesNotBlindlyAverage() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.9, 1.0, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 10, 0, 0, 0.4, 1.0, 50);

        FusedEstimate result = engine.fuse(List.of(a, b), T0.plusMillis(100)).orElseThrow();

        assertFalse(result.qualified());
        assertEquals(0.0, result.position().xM(), 1.0e-9);
        assertTrue(result.qualityNote().contains("Material disagreement"));
    }

    @Test
    void materialDisagreementIsExplicitlyExcluded() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.9, 1.0, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 10, 0, 0, 0.4, 1.0, 50);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(a, b), T0.plusMillis(100));

        assertEquals(1, result.exclusions().size());
        assertEquals("camera-b:track-b", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.MATERIAL_DISAGREEMENT,
                result.exclusions().get(0).reason());
    }

    @Test
    void detailedFusionResultIsDeterministicIncludingExclusionOrdering() {
        FusionEvidence valid = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence incompatibleFrame = new FusionEvidence(
                "camera-c",
                track("track-c", 1, 0, 0, 0.7, 50),
                "other-frame",
                T0.plusMillis(50),
                T0.plusMillis(60),
                null);
        FusionEvidence stale = evidence("camera-b", "track-b", 2, 0, 0, 0.9, null, 3000);

        DeterministicFusionEngine.FusionResult firstOrder =
                engine.fuseDetailed(List.of(stale, incompatibleFrame, valid), T0.plusSeconds(3));
        DeterministicFusionEngine.FusionResult reversedOrder =
                engine.fuseDetailed(List.of(valid, incompatibleFrame, stale), T0.plusSeconds(3));

        assertEquals(firstOrder, reversedOrder);
    }

    @Test
    void materialConflictTieBreakIsIndependentOfInputOrder() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, 1.0, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 10, 0, 0, 0.8, 1.0, 50);

        FusedEstimate firstOrder = engine.fuse(List.of(a, b), T0.plusMillis(100)).orElseThrow();
        FusedEstimate reversedOrder = engine.fuse(List.of(b, a), T0.plusMillis(100)).orElseThrow();

        assertEquals(firstOrder.position(), reversedOrder.position());
        assertEquals(firstOrder.trackIds(), reversedOrder.trackIds());
        assertEquals(firstOrder.associationId(), reversedOrder.associationId());
        assertEquals(firstOrder.qualified(), reversedOrder.qualified());
        assertEquals(firstOrder.qualityNote(), reversedOrder.qualityNote());
    }

    @Test
    void outOfOrderEvidenceWithinConfiguredSkewIsAcceptedDeterministically() {
        FusionEvidence earlier = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence later = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 400);

        DeterministicFusionEngine.FusionResult result =
                engine.fuseDetailed(List.of(later, earlier), T0.plusMillis(500));

        assertEquals(List.of("camera-a", "camera-b"), result.estimate().orElseThrow().sourceIds());
        assertTrue(result.estimate().orElseThrow().qualified());
        assertTrue(result.exclusions().isEmpty());
    }

    @Test
    void evidenceOutsideTemporalPolicyIsExcluded() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 1000);

        FusedEstimate result = engine.fuse(List.of(a, b), T0.plusMillis(100)).orElseThrow();

        assertEquals(List.of("camera-a"), result.sourceIds());
        assertEquals(List.of("track-a"), result.trackIds());
    }

    @Test
    void temporalSkewIsExplicitlyExcluded() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 1000);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(a, b),
                T0.plusSeconds(1).plusMillis(500));

        assertEquals(1, result.exclusions().size());
        assertEquals("camera-b:track-b", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.TEMPORAL_SKEW,
                result.exclusions().get(0).reason());
    }

    @Test
    void staleEvidenceIsExplicitlyExcludedWithoutChangingFreshAnchorSelection() {
        FusionEvidence stale = evidence("camera-a", "track-a", 100, 0, 0, 0.95, null, 0);
        FusionEvidence fresh = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 1000);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(stale, fresh), T0.plusSeconds(3));

        assertEquals(List.of("camera-b"), result.estimate().orElseThrow().sourceIds());
        assertEquals(1, result.exclusions().size());
        assertEquals("camera-a:track-a", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.STALE_OR_FUTURE_DATED,
                result.exclusions().get(0).reason());
    }

    @Test
    void futureDatedEvidenceIsExplicitlyExcluded() {
        FusionEvidence future = evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 1000);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(future), T0);

        assertTrue(result.estimate().isEmpty());
        assertEquals(1, result.exclusions().size());
        assertEquals("camera-a:track-a", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.STALE_OR_FUTURE_DATED,
                result.exclusions().get(0).reason());
    }

    @Test
    void degradedEvidenceIsIncludedButDoesNotQualifyWhenPolicyAllowsIt() {
        FusionPolicy allowingPolicy = new FusionPolicy(Duration.ofMillis(500), 5.0, 20.0,
                Duration.ofSeconds(2), true);
        FusionEvidence degraded = new FusionEvidence(
                "camera-a",
                trackWithState("track-a", 0, 0, 0, 0.9, 0, TrackLifecycleState.DEGRADED),
                "local-world",
                T0,
                T0,
                null);

        FusedEstimate result = new DeterministicFusionEngine(allowingPolicy)
                .fuse(List.of(degraded), T0.plusMillis(100)).orElseThrow();

        assertEquals(List.of("track-a"), result.trackIds());
        assertFalse(result.qualified());
        assertTrue(result.qualityNote().contains("Degraded evidence"));
    }

    @Test
    void degradedEvidenceCanBeRejectedByPolicy() {
        FusionPolicy rejectingPolicy = new FusionPolicy(Duration.ofMillis(500), 5.0, 20.0,
                Duration.ofSeconds(2), false);
        FusionEvidence degraded = new FusionEvidence(
                "camera-a",
                trackWithState("track-a", 0, 0, 0, 0.9, 0, TrackLifecycleState.DEGRADED),
                "local-world",
                T0,
                T0,
                null);
        FusionEvidence healthy = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 0);

        DeterministicFusionEngine.FusionResult result = new DeterministicFusionEngine(rejectingPolicy)
                .fuseDetailed(List.of(degraded, healthy), T0.plusMillis(100));

        assertEquals(List.of("camera-b"), result.estimate().orElseThrow().sourceIds());
        assertTrue(result.estimate().orElseThrow().qualified());
        assertEquals(1, result.exclusions().size());
        assertEquals("camera-a:track-a", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.INVALID_SOURCE_STATE,
                result.exclusions().get(0).reason());
    }

    @Test
    void invalidTrackLifecycleIsExplicitlyExcluded() {
        FusionEvidence terminated = new FusionEvidence(
                "camera-a",
                trackWithState("track-a", 0, 0, 0, 0.9, 0, TrackLifecycleState.TERMINATED),
                "local-world",
                T0,
                T0,
                null);
        FusionEvidence healthy = evidence("camera-b", "track-b", 1, 0, 0, 0.8, null, 0);

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(
                List.of(terminated, healthy), T0.plusMillis(100));

        assertEquals(List.of("camera-b"), result.estimate().orElseThrow().sourceIds());
        assertEquals(1, result.exclusions().size());
        assertEquals("camera-a:track-a", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.INVALID_SOURCE_STATE,
                result.exclusions().get(0).reason());
    }


    @Test
    void fusionDoesNotMutateAuthoritativeWorldModel() {
        com.buccees.vigil.world.WorldModel worldModel = new com.buccees.vigil.world.WorldModel();
        com.buccees.vigil.world.WorldModelUpdater updater = new com.buccees.vigil.world.WorldModelUpdater(worldModel);
        com.buccees.vigil.world.Track existingTrack = track("existing-track", 5, 0, 0, 0.9, 0);
        updater.update(existingTrack);
        var before = worldModel.snapshot();

        FusionEvidence evidence = evidence("camera-a", "track-a", 1, 2, 3, 0.8, null, 0);
        engine.fuse(List.of(evidence), T0.plusMillis(100)).orElseThrow();

        assertEquals(before, worldModel.snapshot());
    }

    @Test
    void fusionAssociationIdRemainsDistinctFromContributingTrackIds() {
        FusedEstimate result = engine.fuse(List.of(
                evidence("camera-a", "track-a", 0, 0, 0, 0.8, null, 0),
                evidence("camera-b", "track-b", 1, 0, 0, 0.7, null, 10)),
                T0.plusMillis(100)).orElseThrow();

        assertEquals("fusion:track-a+track-b", result.associationId());
        assertEquals(List.of("track-a", "track-b"), result.trackIds());
        assertFalse(result.associationId().equals(result.trackIds().get(0)));
        assertFalse(result.associationId().equals(result.trackIds().get(1)));
    }

    @Test
    void invalidConfidenceIsRejectedBeforeFusion() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Track("invalid-confidence", EntityType.VEHICLE, new LocalPosition(0, 0, 0),
                        new LocalPosition(0, 0, 0), new Confidence(Double.NaN), T0,
                        List.of("invalid-confidence-detection"), TrackLifecycleState.CONFIRMED));
    }

    @Test
    void invalidSpatialValuesAreRejectedBeforeFusion() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LocalPosition(Double.POSITIVE_INFINITY, 0, 0));
    }

    @Test
    void missingRequiredEvidenceIdentityIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FusionEvidence("", track("track-a", 0, 0, 0, 0.8, 0),
                        "local-world", T0, T0, null));
    }

    @Test
    void missingRequiredFrameIdentityIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FusionEvidence("camera-a", track("track-a", 0, 0, 0, 0.8, 0),
                        " ", T0, T0, null));
    }

    @Test
    void invalidTransformCannotBeApplied() {
        SpatialTransform invalid = new SpatialTransform("sensor-a", "local-world",
                new LocalPosition(1, 0, 0), false, "invalid-transform");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> invalid.apply(new LocalPosition(0, 0, 0)));
    }

    private static FusionEvidence evidence(String source, String trackId, double x, double y, double z,
                                           double confidence, Double uncertainty, long eventOffsetMs) {
        return new FusionEvidence(source, track(trackId, x, y, z, confidence, eventOffsetMs),
                "local-world", T0.plusMillis(eventOffsetMs), T0.plusMillis(eventOffsetMs + 10), uncertainty);
    }

    private static Track track(String id, double x, double y, double z, double confidence, long eventOffsetMs) {
        return trackWithState(id, x, y, z, confidence, eventOffsetMs, TrackLifecycleState.CONFIRMED);
    }

    private static Track trackWithVelocity(String id, double x, double y, double z, double confidence,
                                           long eventOffsetMs, LocalPosition velocity) {
        return new Track(id, EntityType.VEHICLE, new LocalPosition(x, y, z), velocity,
                new Confidence(confidence), T0.plusMillis(eventOffsetMs),
                List.of(id + "-detection"), TrackLifecycleState.CONFIRMED);
    }

    private static Track trackWithState(String id, double x, double y, double z, double confidence,
                                        long eventOffsetMs, TrackLifecycleState lifecycleState) {
        return new Track(id, EntityType.VEHICLE, new LocalPosition(x, y, z),
                new LocalPosition(1, 0, 0), new Confidence(confidence), T0.plusMillis(eventOffsetMs),
                List.of(id + "-detection"), lifecycleState);
    }
}
