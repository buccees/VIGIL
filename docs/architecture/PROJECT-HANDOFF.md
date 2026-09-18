# VIGIL Project Handoff

**Branch:** `main`  
**Last documented implementation commit:** `74dd64759f77e03e3774e78a0e9f6b0a33d762d3` — merge Attention / Presentation milestone  
**Documentation checkpoint:** `e3bc0e5b` — advance roadmap to Human Interaction / Voice  
**Handoff updated:** 2026-09-18

## Where We Left Off

VIGIL has completed the initial **Spatial / Temporal Fusion**, **Relevance / Priority**, and **Attention / Presentation** milestones.

The implementation sequence remains:

**Fusion → Relevance / Priority → Attention / Presentation → Human Interaction / Voice → Integration / Testing**

## Completed Attention / Presentation Work

- Deterministic Attention / Presentation contract established in `docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`.
- Immutable attention items preserve World Entity identity and derived source-state information.
- Explicit lifecycle states implemented: NEW, ACTIVE, ACKNOWLEDGED, EXPIRED, DISMISSED.
- Replaceable presentation policy implemented with minimum priority, active-item limit, and persistence duration.
- Deterministic ordering uses priority descending, relevance descending, then World Entity ID ascending.
- Repeated evaluation of the same World Entity updates one attention item rather than creating duplicates.
- Expiration, acknowledgment, and dismissal modify presentation state only.
- Low-priority items remain retained in attention state even when suppressed from the active presentation.
- Boundary tests cover ordering, stable identity, repeated evaluation, lifecycle preservation, expiration, and low-priority retention.
- PR #3 was merged into `main` as merge commit `74dd64759f77e03e3774e78a0e9f6b0a33d762d3`.
- Verification CI run **#85** completed successfully.

## CI Correction History

This history is intentionally durable.

### Freshness work
- Workflow runs **#53 and #54** failed on the same `temporalSkewIsExplicitlyExcluded()` test.
- Commit `d2f11e31` corrected the fixture.
- The supplied Gradle test output then passed.

### Spatial transform work
- Initial transform work failed during `compileTestJava` because a test fixture used a stale `FusedEstimate` constructor.
- Commit `3c65ccc5` corrected the fixture.
- Velocity-transform implementation commit `2231d05c` then passed its supplied Gradle test run.

### Identity-domain work
- `dc732b30` enforced optional source-track semantics.
- `80039a2f` separated Track identity from Fusion association identity.
- `96a85263` enforced identity-domain separation at the World Model boundary.
- `20f9c626` verified the completed Fusion behavior.

### Relevance / Priority work
- Workflow runs **#67 through #76** all failed.
- Root cause: `PriorityPolicy` compact-constructor validation called an accessor before implicit record field assignment.
- Commit `f7324bcf` corrected validation to use constructor parameters.
- Workflow run **#78** verified the correction successfully.
- PR #2 merged as `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`.

### Attention / Presentation work
- Implementation PR #3 completed without a CI correction cycle.
- Workflow run **#85** completed successfully on the implementation head `1d2f7b032f399b5cc7b9a2796fc5854377de2902`.
- PR #3 merged as `74dd64759f77e03e3774e78a0e9f6b0a33d762d3`.

## Current Architectural Boundary

Attention / Presentation consumes derived Priority Results and owns presentation state only. It must not become a second World Model or change environmental truth, confidence, validity, freshness, provenance, Track identity, Fusion association, or World Entity identity.

## Next Engineering Work

Begin the **Human Interaction / Voice** milestone.

Primary contract:

`docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

The next pass should implement the contract boundary before adding richer interaction or voice behavior.

For each implementation step:

1. identify the next contract gap;
2. make one coherent implementation change;
3. add/update boundary tests;
4. run CI;
5. record failures and corrections here;
6. verify the result;
7. only then proceed.

## Attention / Presentation Exit Checklist

- [x] Contract established.
- [x] Boundary between priority results and presentation state implemented.
- [x] Persistent items and lifecycle semantics implemented.
- [x] Deterministic ordering/replacement implemented.
- [x] Source identity and source-state references preserved.
- [x] Presentation does not mutate World Model truth.
- [x] Boundary tests added.
- [x] CI verified successfully.
- [x] Milestone verified before advancing.

## Authoritative References

- `docs/architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`
- `docs/technical/RELEVANCE-PRIORITY-CONTRACT.md`
- `docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`
- `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md`
- `docs/architecture/AUTONOMY-HUMAN-DECISION-BOUNDARY.md`

## Handoff Rule

**The repository must always be understandable from its Markdown documentation without relying on the previous assistant conversation.**

When implementation state materially changes, update this handoff and the relevant roadmap/contract documentation as a documentation checkpoint.
