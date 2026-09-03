# VIGIL World Model Update Boundary Contract

**Document:** World Model Update Boundary Contract  
**Version:** 0.1  
**Status:** Proposed for implementation  
**Parent:** VIGIL Architecture Contract

## 1. Purpose

This contract makes the transition from derived perception/fusion state into authoritative Spatial World Model state explicit, controlled, and observable.

The boundary is:

```text
Track / Fused Estimate
          ↓
   WorldModelUpdater
          ↓
 Authoritative World Model
          ↓
 WorldModelEvent
```

`WorldModelUpdater` is the only component in this module permitted to commit track-derived or fusion-derived state into the authoritative World Model.

## 2. Boundary Invariants

The updater MUST:

- validate the incoming state before commit;
- preserve the separation `Detection ID ≠ Track ID ≠ World Entity ID`;
- preserve contributing track and detection provenance;
- reject older state from overwriting newer authoritative state;
- reject an unresolved fusion result that would implicitly merge already distinct World Entities;
- commit through the World Model's controlled update operation; and
- emit an observable state-change event only after a successful authoritative mutation.

The updater MUST NOT:

- perform fusion;
- invent missing spatial information;
- silently convert unavailable uncertainty into precision;
- silently merge already distinct entities; or
- allow a caller to mutate the World Model through a bypass path.

## 3. Accepted Inputs

The boundary explicitly accepts:

1. a validated `Track`; or
2. a validated `FusedEstimate`.

A `Track` update has origin `TRACK`.

A `FusedEstimate` update has origin `FUSED_ESTIMATE`.

A fused estimate's `associationId` is an internal deterministic association reference. It is not, by itself, authoritative physical identity.

## 4. Fused Estimate Association

For a fused estimate, the updater resolves contributing track IDs against existing track-to-entity associations.

The following rules are mandatory:

- no existing association: create one World Entity and associate all contributing tracks;
- exactly one existing World Entity: update that entity and associate all contributing tracks to it;
- more than one existing World Entity: reject the update without mutating authoritative state.

This prevents the update boundary from silently collapsing two established World Entities into one merely because a fusion result proposes a common association.

## 5. Temporal Ordering

The updater MUST compare the incoming authoritative update time with the current World Entity state before commit.

An older update MUST NOT overwrite newer state.

Returning the current state for an older update is not a state change and MUST NOT emit a state-change event.

## 6. Observable Boundary

Every successful mutation MUST emit a `WorldModelEvent` after the World Model has changed.

The event MUST identify:

- the affected World Entity;
- the update origin (`TRACK` or `FUSED_ESTIMATE`);
- the event type;
- the contributing track IDs;
- the detection IDs currently represented by the entity; and
- the before/after lifecycle state where applicable.

This event is an observability mechanism, not an authorization mechanism and not a second source of authoritative state.

A failed validation, rejected temporal update, or rejected multi-entity fusion association MUST NOT emit a successful state-change event.

## 7. Provenance

For a track update, the event's contributing track set contains the single source track.

For a fused update, the event's contributing track set contains all tracks represented by the fused estimate.

The World Entity retains the corresponding track and detection provenance. The event provides an externally observable indication of which path crossed the authoritative boundary.

## 8. Failure Semantics

Boundary failures MUST be explicit.

At minimum, implementations MUST distinguish:

- invalid input;
- older-than-current state;
- unresolved multi-entity association; and
- successful mutation.

Rejected updates MUST leave authoritative World Model state unchanged.

## 9. Verification Requirements

Tests MUST demonstrate:

1. a track update crosses the boundary with origin `TRACK`;
2. a fused update crosses the boundary with origin `FUSED_ESTIMATE`;
3. fused contributing-track provenance is observable;
4. older state cannot overwrite newer state;
5. distinct existing entities cannot be silently merged;
6. rejected updates do not emit successful mutation events; and
7. successful events are emitted only after authoritative state exists.

## 10. Architectural Relationship

This contract implements the architecture-level invariant:

> **Derived information may cross into authoritative World Model state only through a controlled update boundary, and that crossing must be observable after successful mutation.**

Fusion remains responsible for producing a derived estimate. `WorldModelUpdater` remains responsible for controlled projection into authoritative state. `WorldModelEvent` makes the successful transition observable without granting the event authority to mutate state.
