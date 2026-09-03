# VIGIL Implementation Roadmap

**Version:** 1.0  
**Status:** Approved sequence  
**Purpose:** Persistent implementation bookmark for the next architectural milestones.

## Implementation Sequence

VIGIL implementation proceeds in this order:

**Fusion → Relevance/Priority → Attention/Presentation → Human Interaction/Voice → Integration/Testing**

This sequence is intentional. Work should not skip ahead to a later layer unless an architectural dependency or contract review explicitly requires it.

### 1. Spatial / Temporal Fusion

Combine compatible perception evidence into uncertainty-aware, provenance-preserving fused estimates.

Primary contract:

`docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md`

### 2. Relevance / Priority

Determine which modeled information is most relevant to the human's current context and deserves attention.

Priority is separate from truth, confidence, uncertainty, and fusion.

### 3. Attention / Presentation

Convert prioritized world information into persistent, understandable information for the user through the device/headset presentation layer.

This layer owns presentation and attention management, not environmental truth.

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
- `docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md`

### Core Principle

> **VIGIL observes, understands, organizes, and presents information. The human decides and acts.**

No implementation milestone in this roadmap changes that boundary.

## Current Position

**Current milestone:** Spatial / Temporal Fusion

**Next milestone:** Relevance / Priority

The fusion contract is approved for implementation. The next engineering work should implement and test that contract before moving to Relevance / Priority.

## Change Control

Changes to this sequence should be documented and reviewed before implementation proceeds out of order. The roadmap is a bookmark, not a substitute for the individual technical contracts governing each subsystem.
