# VIGIL Project Handoff

**Branch:** `feature/spatial-core`  
**Last documented implementation commit:** `2231d05c` — Transform velocity into fusion frame  
**Documentation checkpoint:** `84dee605`  
**Handoff updated:** 2026-09-16

## Where We Left Off

VIGIL is currently in the **Spatial / Temporal Fusion** milestone. The implementation sequence remains:

**Fusion → Relevance / Priority → Attention / Presentation → Human Interaction / Voice → Integration / Testing**

Do not advance to Relevance / Priority until Fusion's contract, implementation, diagnostics, provenance, and World Model boundary are complete and CI-verified.

## Completed Since the Previous Handoff

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

## Current Verified Checkpoint

Commit `2231d05c` passed the Gradle test step:

- `:spatial-core:compileJava` passed.
- `:spatial-core:compileTestJava` passed.
- `:spatial-core:test` passed.
- `BUILD SUCCESSFUL in 34s` was reported by the workflow output supplied during the handoff.

The Gradle/Actions environment also reported Node 20 and `punycode` deprecation warnings. These were environment warnings, not VIGIL test failures.

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

## Next Engineering Work

The next step is to inspect the remaining Fusion contract requirements against the implementation and address the **next concrete gap**, one logical change at a time.

Do not start Relevance / Priority yet.

For every implementation step:

1. identify the contract gap;
2. make one coherent implementation change;
3. add/update boundary tests;
4. run CI;
5. record failures and corrections here;
6. verify the result;
7. only then proceed.

## Authoritative References

- `docs/architecture/IMPLEMENTATION-ROADMAP.md` — sequence, current position, and projected delivery windows.
- `docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md` — current Fusion behavioral contract.
- `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md` — system architecture and boundaries.
- `docs/architecture/AUTONOMY-HUMAN-DECISION-BOUNDARY.md` — human decision boundary.

## Handoff Rule

**The repository must always be understandable from its Markdown documentation without relying on the previous assistant conversation.**

When implementation state materially changes, update this handoff and the relevant roadmap/contract documentation as a documentation checkpoint so the next contributor can determine exactly where work stopped, what was verified, what failed and was corrected, and what must happen next.
