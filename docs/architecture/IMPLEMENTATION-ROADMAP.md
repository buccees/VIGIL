# VIGIL Implementation Roadmap

**Version:** 1.2  
**Status:** Approved sequence / active implementation bookmark  
**Purpose:** Persistent implementation direction and current-state bookmark for the next architectural milestones.

## Implementation Sequence

VIGIL implementation proceeds in this order:

**Fusion → Relevance/Priority → Attention/Presentation → Human Interaction/Voice → Integration/Testing**

This sequence is intentional. Work should not skip ahead to a later layer unless an architectural dependency or contract review explicitly requires it.

### 1. Spatial / Temporal Fusion

Combine compatible perception evidence into uncertainty-aware, provenance-preserving fused estimates.

Primary contract:

`docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md`

**Current state:** Complete for the initial deterministic milestone; final implementation and boundary verification are complete.

### 2. Relevance / Priority

Determine which modeled information is most relevant to the human's current context and deserves attention.

Priority is separate from truth, confidence, uncertainty, and fusion.

Primary contract:

`docs/technical/RELEVANCE-PRIORITY-CONTRACT.md`

**Current state:** Complete for the initial deterministic milestone; implementation, boundary tests, correction history, and CI verification are complete.

Verified implementation merge commit: `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`  
Verified CI run: **#78** — successful.

### 3. Attention / Presentation

Convert prioritized world information into persistent, understandable information for the user through the device/headset presentation layer.

This layer owns presentation and attention management, not environmental truth.

**Current state:** Next milestone.

Primary contract:

`docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`

### 4. Human Interaction / Voice

Provide bidirectional communication between the human and VIGIL through text and optional voice.

Primary contract:

`docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

Voice and AI remain inside the human-decision boundary.

### 5. Integration / Testing

Integrate the completed layers and validate the full architecture end-to-end, including timing, provenance, freshness, performance, failure handling, and all architectural invariants.

## Governing Boundaries

All implementation remains subject to:

- `docs/architecture/AUTONOMY-HUMAN-DECISION-BOUNDARY.md`
- `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md`
- `docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md`
- `docs/technical/RELEVANCE-PRIORITY-CONTRACT.md`
- `docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`
- `docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

### Core Principle

> **VIGIL observes, understands, organizes, and presents information. The human decides and acts.**

No implementation milestone in this roadmap changes that boundary.

## Current Position

**Current milestone:** Attention / Presentation  
**Previous completed milestone:** Relevance / Priority  
**Verified Relevance / Priority merge commit:** `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`  
**Verified Relevance / Priority implementation fix:** `f7324bcf` — correct record-constructor weight validation  
**Verified Relevance / Priority CI:** Spatial Core CI **#78** — successful  
**Next milestone:** Attention / Presentation

### Relevance / Priority Exit Checklist

- [x] Deterministic relevance/priority contract established.
- [x] Explicit evaluation context.
- [x] Replaceable scoring policy.
- [x] Seven inspectable priority factors.
- [x] Immutable derived priority results with source-state summary.
- [x] Deterministic ordering by priority, then World Entity ID.
- [x] Explicit unavailable-factor handling.
- [x] Validity/freshness handling without World Model mutation.
- [x] Boundary tests for determinism, proximity, state change, task relevance, invalid/stale state, missing context, tie-breaking, and low-priority preservation.
- [x] CI correction history recorded in `PROJECT-HANDOFF.md`.
- [x] Final implementation verification passed.

**Relevance / Priority exit condition:** **Satisfied.** The deterministic relevance/priority behavior, separation from authoritative World Model state, explainability, boundary tests, correction history, and CI verification are complete.

### Attention / Presentation Entry Checklist

- [ ] Define the Attention / Presentation contract.
- [ ] Establish the boundary between priority results and presentation state.
- [ ] Define persistent attention items and lifecycle semantics.
- [ ] Define deterministic presentation ordering and replacement behavior.
- [ ] Preserve source World Entity identity and provenance references.
- [ ] Ensure presentation cannot mutate authoritative World Model truth.
- [ ] Add boundary tests.
- [ ] Run CI and record any correction history.
- [ ] Verify the milestone before advancing.

## Delivery Outlook

Dates are working targets, not promises. They are revised when implementation or verification changes the dependency schedule.

| Milestone | Working target | Condition |
|---|---|---|
| Spatial / Temporal Fusion | **September 18, 2026** | Complete — final verification passed |
| Relevance / Priority | **September 19–25, 2026** | Complete — CI #78 passed |
| Attention / Presentation | **September 26–October 2, 2026** | Next milestone |
| Human Interaction / Voice | **October 3–9, 2026** | Begins after Attention / Presentation exit |
| Integration / Testing | **October 10–18, 2026** | Release-gating integrated baseline |

These targets are planning projections as of the 2026-09-18 documentation checkpoint and may move with CI results, contract discoveries, or architectural dependencies.

## Next Engineering Rule

Work proceeds one logical change at a time:

1. identify the next contract gap;
2. implement only that coherent change;
3. add or update boundary tests;
4. run CI;
5. record failures and corrections in `PROJECT-HANDOFF.md`;
6. verify the result;
7. then continue.

Do not create multiple workflow runs for unrelated edits when one atomic change can contain them. Documentation synchronization may be grouped into one documentation checkpoint.

## Deferred Engineering Direction

These capabilities remain intentionally deferred until their resolution triggers are met:

| Priority | Future capability | Planned window | Resolution trigger |
|---|---|---|---|
| P1 | Advanced probabilistic association | October 2026 | After deterministic association is stable and benchmarked |
| P1 | Advanced uncertainty propagation / covariance | October–November 2026 | When uncertainty requirements exceed the initial scalar model |
| P1 | Multi-hypothesis tracking | November 2026 | When ambiguous identity/association cases require multiple live hypotheses |
| P2 | Automatic calibration | November–December 2026 | After sensor/calibration interfaces are established |
| P2 | ML-assisted fusion | December 2026+ | Only after deterministic fusion provides a trustworthy baseline |
| P2 | AI-assisted identity resolution | December 2026+ | Only after identity contracts and provenance are mature |
| P2 | Distributed fusion | 2027 direction | When multiple VIGIL nodes require shared spatial state |
| P2 | Persistent fusion databases | 2027 direction | When operational history/replay volume requires dedicated persistence |

### Deferred-work rule

A deferred capability should move into active implementation only when its **resolution trigger** is met and the current architecture/contracts can support it without weakening established boundaries. Each activation should receive its own technical contract, implementation milestone, tests, and CI verification.

The deferred list is a planning tool, not permission to skip current dependencies.

## Handoff Reference

For the detailed current state, recent implementation history, CI correction history, and exact next-step procedure, see:

`docs/architecture/PROJECT-HANDOFF.md`

The handoff is part of the project record and must be updated whenever implementation state materially changes.

## Change Control

Changes to this sequence should be documented and reviewed before implementation proceeds out of order. The roadmap is a bookmark, not a substitute for the individual technical contracts governing each subsystem.
