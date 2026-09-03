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
    void compatibleEvidenceProducesDeterministicFusedEstimate() {
        FusionEvidence a = evidence("camera-a", "track-a", 0, 0, 0, 0.8, 2.0, 0);
        FusionEvidence b = evidence("camera-b", "track-b", 2, 0, 0, 0.6, 4.0, 100);

        FusedEstimate result = engine.fuse(List.of(b, a), T0.plusMillis(200)).orElseThrow();

        assertEquals(0.8571428571, result.position().xM(), 1.0e-9);
        assertEquals(0.0, result.position().yM(), 1.0e-9);
        assertEquals(0.0, result.position().zM(), 1.0e-9);
        assertEquals(2.8571428571, result.positionUncertaintyMeters().orElseThrow(), 1.0e-9);
        assertEquals(List.of("camera-a", "camera-b"), result.sourceIds());
        assertEquals(List.of("track-a", "track-b"), result.trackIds());
        assertTrue(result.qualified());
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

        DeterministicFusionEngine.FusionResult result = engine.fuseDetailed(List.of(a, b), T0.plusMillis(100));

        assertEquals(1, result.exclusions().size());
        assertEquals("camera-b:track-b", result.exclusions().get(0).evidenceId());
        assertEquals(DeterministicFusionEngine.FusionExclusionReason.TEMPORAL_SKEW,
                result.exclusions().get(0).reason());
    }

    private static FusionEvidence evidence(String source, String trackId, double x, double y, double z,
                                           double confidence, Double uncertainty, long eventOffsetMs) {
        return new FusionEvidence(source, track(trackId, x, y, z, confidence, eventOffsetMs),
                "local-world", T0.plusMillis(eventOffsetMs), T0.plusMillis(eventOffsetMs + 10), uncertainty);
    }

    private static Track track(String id, double x, double y, double z, double confidence, long eventOffsetMs) {
        return new Track(id, EntityType.VEHICLE, new LocalPosition(x, y, z),
                new LocalPosition(1, 0, 0), new Confidence(confidence), T0.plusMillis(eventOffsetMs),
                List.of(id + "-detection"), TrackLifecycleState.CONFIRMED);
    }
}
