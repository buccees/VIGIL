# VIGIL Implementation Roadmap

**Version:** 1.4
**Status:** Approved sequence / active implementation bookmark
**Purpose:** Persistent implementation direction and current-state bookmark for the next architectural milestones.

## Implementation Sequence

**Fusion → Relevance/Priority → Attention/Presentation → Human Interaction/Voice → Integration/Testing**

### 1. Spatial / Temporal Fusion

**Current state:** Complete for the initial deterministic milestone.

### 2. Relevance / Priority

**Current state:** Complete for the initial deterministic milestone.

Verified merge: `500563b3153cdc0cfcc41d8c04f1d3e747b4c19f`  
Verified CI: **#78** — successful.

### 3. Attention / Presentation

**Current state:** Complete for the initial deterministic milestone.

Verified merge: `74dd64759f77e03e3774e78a0e9f6b0a33d762d3`  
Verified CI: **#85** — successful.

### 4. Human Interaction / Voice

Primary contract:

`docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

**Current state:** Initial structured interaction boundary complete; voice/media and richer interaction remain to be implemented before milestone exit.

Verified implementation merge: `0a219bf516e9e31c939ef2d1d7081bd5b86d1653`  
Verified CI: **#95** — successful.

Implemented first boundary:
- structured text and speech request model;
- explicit authentication state;
- explicit authorization context;
- grounded response model;
- authorization validation without executing requested operations;
- context cannot silently expand permissions;
- boundary tests.

Remaining Human Interaction / Voice work:
- interaction/session lifecycle;
- clarification and ambiguity handling;
- presentation-to-interaction handoff;
- voice input/output adapter boundaries;
- provenance for speech recognition and generated responses;
- explicit human confirmation for consequential actions;
- boundary tests and final CI verification.

**Current milestone:** Human Interaction / Voice.

### 5. Integration / Testing

**Current state:** Future milestone.

Integrate the completed layers and validate the full architecture end-to-end, including timing, provenance, freshness, performance, failure handling, and architectural invariants.

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
**Latest verified implementation:** `0a219bf516e9e31c939ef2d1d7081bd5b86d1653`  
**Latest verified CI:** Spatial Core CI **#95** — successful

## Human Interaction / Voice Exit Checklist

- [x] Structured human request boundary.
- [x] Text and speech modalities represented explicitly.
- [x] Authentication separated from authorization.
- [x] Authorization context explicit and immutable.
- [x] Grounded response boundary.
- [x] Requested operations validated rather than executed by the interaction layer.
- [x] Boundary tests for authentication, authorization, modalities, and permission separation.
- [ ] Session lifecycle.
- [ ] Clarification / ambiguity handling.
- [ ] Attention-to-interaction handoff.
- [ ] Voice input/output adapters.
- [ ] Speech and response provenance.
- [ ] Explicit confirmation boundary for consequential actions.
- [ ] Final milestone CI verification.

## Next Engineering Rule

Work proceeds one logical change at a time:

1. identify the next contract gap;
2. implement only that coherent change;
3. add/update boundary tests;
4. run CI;
5. record failures and corrections in `PROJECT-HANDOFF.md`;
6. verify the result;
7. then continue.

## Handoff Reference

See `docs/architecture/PROJECT-HANDOFF.md` for detailed implementation and CI history.
