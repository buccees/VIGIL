# VIGIL Project Handoff

**Branch:** `main`  
**Last documented implementation commit:** `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f` — merge Relevance / Priority milestone  
**Documentation checkpoint:** `5712ba2d` — advance roadmap to Attention / Presentation  
**Handoff updated:** 2026-09-18

## Where We Left Off

VIGIL has completed the initial **Spatial / Temporal Fusion** and **Relevance / Priority** milestones. The implementation sequence remains:

**Fusion → Relevance / Priority → Attention / Presentation → Human Interaction / Voice → Integration / Testing**

Fusion and Relevance / Priority contracts, implementations, diagnostics, provenance boundaries, World Model boundaries, required tests, correction histories, and CI verification are complete for their initial deterministic milestones.

The next engineering milestone is **Attention / Presentation**.

## Completed Relevance / Priority Work

- Deterministic Relevance / Priority contract established in `docs/technical/RELEVANCE-PRIORITY-CONTRACT.md`.
- Explicit evaluation context established.
- Replaceable deterministic scoring policy established.
- Seven inspectable priority factors implemented.
- Immutable derived priority results preserve source-state summary.
- Deterministic ordering uses priority, then World Entity ID.
- Missing evaluation context is represented explicitly rather than guessed.
- Validity and freshness affect derived priority without mutating authoritative World Model state.
- Low-priority entities remain present in the World Model.
- Boundary tests cover determinism, proximity, state change, task relevance, invalid/stale state, missing context, tie-breaking, and low-priority preservation.
- The compact-constructor validation bug in `PriorityPolicy` was corrected in commit `f7324bcf`.
- Verification CI run **#78** completed successfully.
- PR #2 was merged into `main` as merge commit `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`.

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

- `dc732b30` enforced optional source-track semantics for fused entities.
- `80039a2f` separated Track identity from Fusion association identity in world events.
- `96a85263` enforced identity-domain separation at the World Model boundary.
- `20f9c626` verified that the distinct identity domains remain separate in the completed Fusion behavior.
- The current CI verification for `20f9c626` completed successfully.

### Relevance / Priority work

- Workflow runs **#67 through #76** failed during the Relevance / Priority implementation sequence.
- The failures traced to a Java record compact-constructor bug in `PriorityPolicy`: the constructor called `totalWeight()` before the record's implicit field assignment, so the accessor observed default zero values and rejected valid policies.
- Commit `f7324bcf` corrected the constructor to calculate the validation total directly from constructor parameters.
- Workflow run **#78** verified the correction successfully.
- PR #2 was then merged into `main` as `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`.

### Logging rule

Whenever a workflow failure requires a correction and another push, update this handoff with:

1. attempt/run number(s);
2. the failure that occurred;
3. whether later runs corrected the same failure or introduced a new one;
4. the corrective commit; and
5. the verification result.

This is a durable project record and should not depend on conversation history.

## Current Architectural Boundary

Relevance / Priority consumes authoritative World Entities and produces derived information for downstream attention management. It does not redefine:

- truth;
- confidence;
- uncertainty;
- provenance;
- Track identity;
- Fusion association;
- authoritative World Entity identity; or
- authoritative World Model state.

The next Attention / Presentation layer must preserve the same separation. Presentation may decide what is persistent, visible, audible, queued, replaced, or acknowledged at the human interface, but it must not become a second World Model.

## Next Engineering Work

Begin the **Attention / Presentation** milestone.

Primary entry contract:

`docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`

The first implementation pass should establish the contract before adding presentation behavior.

For every implementation step:

1. identify the next contract gap;
2. make one coherent implementation change;
3. add/update boundary tests;
4. run CI;
5. record failures and corrections here;
6. verify the result;
7. only then proceed.

## Attention / Presentation Entry Checklist

- [ ] Define the Attention / Presentation contract.
- [ ] Establish the boundary between priority results and presentation state.
- [ ] Define persistent attention items and lifecycle semantics.
- [ ] Define deterministic presentation ordering and replacement behavior.
- [ ] Preserve source World Entity identity and provenance references.
- [ ] Ensure presentation cannot mutate authoritative World Model truth.
- [ ] Add boundary tests.
- [ ] Run CI and record any correction history.
- [ ] Verify the milestone before advancing.

## Authoritative References

- `docs/architecture/IMPLEMENTATION-ROADMAP.md` — sequence and current position.
- `docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md` — completed initial Fusion behavioral contract.
- `docs/technical/RELEVANCE-PRIORITY-CONTRACT.md` — completed initial Relevance / Priority behavioral contract.
- `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md` — system architecture and boundaries.
- `docs/architecture/AUTONOMY-HUMAN-DECISION-BOUNDARY.md` — human decision boundary.

## Handoff Rule

**The repository must always be understandable from its Markdown documentation without relying on the previous assistant conversation.**

When implementation state materially changes, update this handoff and the relevant roadmap/contract documentation as a documentation checkpoint so the next contributor can determine exactly where work stopped, what was verified, what failed and was corrected, and what must happen next.
