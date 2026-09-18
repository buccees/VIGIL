# VIGIL Implementation Roadmap

**Version:** 1.3  
**Status:** Approved sequence / active implementation bookmark  
**Purpose:** Persistent implementation direction and current-state bookmark for the next architectural milestones.

## Implementation Sequence

VIGIL implementation proceeds in this order:

**Fusion → Relevance/Priority → Attention/Presentation → Human Interaction/Voice → Integration/Testing**

This sequence is intentional. Work should not skip ahead to a later layer unless an architectural dependency or contract review explicitly requires it.

### 1. Spatial / Temporal Fusion

**Current state:** Complete for the initial deterministic milestone.

### 2. Relevance / Priority

**Current state:** Complete for the initial deterministic milestone.

Verified implementation merge commit: `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`  
Verified CI run: **#78** — successful.

### 3. Attention / Presentation

Convert prioritized world information into persistent, understandable information for the user.

Primary contract:

`docs/technical/ATTENTION-PRESENTATION-CONTRACT.md`

**Current state:** Complete for the initial deterministic milestone.

Verified implementation merge commit: `74dd64759f77e03e3774e78a0e9f6b0a33d762d3`  
Verified CI run: **#85** — successful.  
Implementation: immutable attention items, explicit lifecycle, replaceable policy, deterministic ordering, update/replacement, expiration, acknowledgment/dismissal, and boundary tests.

### 4. Human Interaction / Voice

Provide bidirectional communication between the human and VIGIL through text and optional voice.

Primary contract:

`docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

Voice and AI remain inside the human-decision boundary.

**Current state:** Next milestone.

### 5. Integration / Testing

Integrate the completed layers and validate the full architecture end-to-end.

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

## Current Position

**Current milestone:** Human Interaction / Voice  
**Previous completed milestone:** Attention / Presentation  
**Verified Attention merge commit:** `74dd64759f77e03e3774e78a0e9f6b0a33d762d3`  
**Verified Attention CI:** Spatial Core CI **#85** — successful  
**Next milestone:** Human Interaction / Voice

### Attention / Presentation Exit Checklist

- [x] Attention / Presentation contract established.
- [x] Boundary between priority results and presentation state established.
- [x] Persistent attention items and lifecycle semantics implemented.
- [x] Deterministic presentation ordering and replacement behavior implemented.
- [x] Source World Entity identity and source-state references preserved.
- [x] Presentation state does not mutate authoritative World Model truth.
- [x] Boundary tests added.
- [x] CI verified successfully.
- [x] Milestone verified before advancing.

**Attention / Presentation exit condition:** **Satisfied.**

## Next Engineering Rule

Work proceeds one logical change at a time:

1. identify the next contract gap;
2. implement only that coherent change;
3. add or update boundary tests;
4. run CI;
5. record failures and corrections in `PROJECT-HANDOFF.md`;
6. verify the result;
7. then continue.

## Handoff Reference

See `docs/architecture/PROJECT-HANDOFF.md` for detailed implementation and CI history.
