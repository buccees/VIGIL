# VIGIL Project Handoff

**Branch:** `feature/spatial-core`  
**Last documented implementation commit:** `20f9c626` — Verify distinct fusion and track identity domains  
**Documentation checkpoint:** `7b200618` — Mark Fusion milestone complete  
**Handoff updated:** 2026-09-17

## Where We Left Off

VIGIL has completed the initial **Spatial / Temporal Fusion** milestone. The implementation sequence remains:

**Fusion → Relevance / Priority → Attention / Presentation → Human Interaction / Voice → Integration / Testing**

Fusion's contract, implementation, diagnostics, provenance, association boundaries, World Model boundary, and required verification are complete for the initial deterministic milestone. The next engineering milestone is **Relevance / Priority**.

## Completed Fusion Work

- Deterministic Fusion foundation implemented.
- Confidence, uncertainty, freshness, and provenance preserved.
- World Model mutation kept behind `WorldModelUpdater`.
- World Model update boundary made explicit and observable.
- Fusion exclusions made structured and observable.
- `FusedEstimate.qualified` semantics reconciled with the contract.
- Configurable freshness enforcement added; stale and future-dated evidence is explicitly excluded.
- Invalid source lifecycle states are explicitly excluded; degraded evidence remains usable but produces an unqualified result when policy requires.
- Deterministic translation-only spatial frame transforms added.
- Transform provenance is preserved in fused estimates.
- Velocity is passed through the same explicit spatial-transform boundary before fusion.
- Boundary tests were added for valid/invalid transforms and transformed velocity.
- Track ID, Fusion association ID, and World Entity ID are explicitly separated.
- Fused entities do not treat a Fusion association ID as an authoritative Track ID.
- `WorldEntity.sourceTrackId`, when populated, is constrained to an actual contributing Track ID.
- Multi-track fused provenance remains distinct from the Fusion association reference.

## Current Verified Checkpoint

The current implementation checkpoint is commit `20f9c626`:

- `20f9c626` verifies the distinct Fusion and Track identity domains.
- The preceding boundary commits enforce optional source-track semantics, separate track and fusion association identity in world events, and enforce identity-domain separation at the World Model boundary.
- The Fusion contract was synchronized to the verified transform and identity boundaries.
- The roadmap was synchronized before this handoff update.
- CI workflow **Build and test Spatial Core** completed successfully for `20f9c626`.

The verified Fusion exit condition is therefore satisfied for the initial deterministic milestone.

The Gradle/Actions environment may report Node 20 and `punycode` deprecation warnings. These are environment warnings, not VIGIL test failures.

## CI Correction History

This history is intentionally kept here so future contributors can distinguish repeated corrections from independent failures.

### Freshness work

- Workflow runs **#53 and #54** failed on the same `temporalSkewIsExplicitlyExcluded()` test.
- The implementation was correct; the test fixture had future-dated evidence that was being correctly rejected by the new freshness policy.
- Commit `d2f11e31` corrected the fixture so the temporal-skew test exercised temporal skew with otherwise eligible evidence.
- The supplied Gradle test output then passed.

### Spatial transform work

- The initial transform implementation exposed a stale `FusedEstimate` constructor in `WorldModelUpdaterFusionTest.java`.
- The workflow failed during `compileTestJava`, before tests ran.
- Commit `3c65ccc5` supplied the new transform-provenance argument to the test fixture.
- The subsequent velocity-transform implementation was committed as `2231d05c` and its supplied Gradle test run passed.

### Identity-domain work

- The remaining Fusion exit work focused on the Track → World Model identity/association boundary.
- `dc732b30` enforced optional source-track semantics for fused entities.
- `80039a2f` separated Track identity from Fusion association identity in world events.
- `96a85263` enforced identity-domain separation at the World Model boundary.
- `20f9c626` verified that the distinct identity domains remain separate in the completed Fusion behavior.
- The current CI verification for `20f9c626` completed successfully.

### Logging rule

Whenever a workflow failure requires a correction and another push, update this handoff with:

1. attempt/run number(s);
2. the failure that occurred;
3. whether later runs corrected the same failure or introduced a new one;
4. the corrective commit; and
5. the verification result.

This is a durable project record and should not depend on conversation history.

## Current Fusion Boundary

The initial spatial transform remains intentionally **translation-only**. Rotation, scale, arbitrary nonlinear/geographic transforms, advanced uncertainty propagation, and calibration frameworks are deferred until their architectural dependencies require them.

For a translation-only frame relationship, position is translated directly and velocity is passed through the explicit transform boundary unchanged. This keeps frame-dependent kinematics from bypassing the transform abstraction while avoiding premature general geometry infrastructure.

Identity boundaries are equally explicit:

```text
Track ID
   |
   +--> contributing Track provenance
   |
   v
Fusion association ID
   |
   v
Fused Estimate
   |
   v
WorldModelUpdater
   |
   v
World Entity ID
```

The Fusion association ID is an association reference, not authoritative physical identity. World Model identity remains resolved through the updater using actual contributing Track IDs and Track → World Entity association state.

## Next Engineering Work

Begin the **Relevance / Priority** milestone.

The next layer must remain separate from Fusion. Relevance/Priority may organize modeled information for the human's current context, but it must not redefine truth, confidence, uncertainty, provenance, Fusion association, Track identity, or authoritative World Entity identity.

For every implementation step:

1. identify the next contract gap;
2. make one coherent implementation change;
3. add/update boundary tests;
4. run CI;
5. record failures and corrections here;
6. verify the result;
7. only then proceed.

Do not expand Fusion's initial translation-only transform into a general geometry/calibration framework unless a later contract explicitly requires it.

## Authoritative References

- `docs/architecture/IMPLEMENTATION-ROADMAP.md` — sequence, current position, and projected delivery windows.
- `docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md` — completed initial Fusion behavioral contract.
- `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md` — system architecture and boundaries.
- `docs/architecture/AUTONOMY-HUMAN-DECISION-BOUNDARY.md` — human decision boundary.

## Handoff Rule

**The repository must always be understandable from its Markdown documentation without relying on the previous assistant conversation.**

When implementation state materially changes, update this handoff and the relevant roadmap/contract documentation as a documentation checkpoint so the next contributor can determine exactly where work stopped, what was verified, what failed and was corrected, and what must happen next.
