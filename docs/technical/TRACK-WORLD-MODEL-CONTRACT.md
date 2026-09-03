# VIGIL Track → World Model Technical Contract

**Document:** Track → World Model Technical Contract  
**Version:** 0.2  
**Status:** Proposed for implementation  
**Parent:** Spatial World Model Technical Design

## 1. Purpose

This contract defines the boundary between temporal perception state (`Track`), derived fusion state (`FusedEstimate`), and authoritative spatial state (`World Model`). It establishes the behavior that implementation must preserve as VIGIL evolves toward multi-source spatial/temporal fusion.

The contract defines semantics first; classes, storage, frameworks, and persistence mechanisms are implementation details.

## 2. Scope

This milestone covers:

`Detection → TrackManager → Track → Spatial / Temporal Fusion → FusedEstimate → WorldModelUpdater → World Entity → World Model`

It preserves the canonical lifecycle:

`Observation → Detection → Track → Spatial / Temporal Fusion → Spatial World Model → Spatial / Environmental Services → Relevance & Priority → Presentation / Attention`

Fusion and track-derived state may both enter authoritative state, but neither may bypass `WorldModelUpdater`.

## 3. Responsibilities

### 3.1 TrackManager

`TrackManager` owns temporal association and track maintenance.

It is responsible for:

- associating detections with tracks according to its configured association policy;
- maintaining track state and lifecycle;
- estimating supported spatial/kinematic state;
- maintaining contributing detection references;
- maintaining timestamps and confidence/quality information; and
- exposing a stable track identifier.

`TrackManager` does not own authoritative world entities and does not directly mutate `WorldModel`.

### 3.2 Fusion Layer

The fusion layer combines compatible evidence into a `FusedEstimate`.

A fused estimate is derived state, not authoritative World Model state.

Fusion must preserve confidence, uncertainty, freshness, validity, provenance, and material disagreement. Fusion must not directly mutate `WorldModel`.

### 3.3 WorldModelUpdater

`WorldModelUpdater` is the controlled boundary between derived perception/fusion state and authoritative world state.

It shall:

1. validate the incoming state and required spatial/time semantics;
2. resolve the applicable Track → Entity or fused association;
3. create or update the entity without losing authoritative entity identity;
4. propagate supported spatial/kinematic state;
5. preserve confidence, uncertainty, freshness, validity, and provenance;
6. reject older state from overwriting newer authoritative state;
7. commit World Model state only after validation succeeds; and
8. publish a state-change event only after the authoritative state has changed.

A fused estimate that references multiple tracks must not silently merge already distinct World Entities.

### 3.4 World Model

`WorldModel` owns the authoritative current projection of spatial state.

It stores current World Entities, exposes validity/freshness, and remains independent of the perception or fusion implementation that produced the state.

The World Model does not become a tracker or fusion engine.

## 4. Identity Contract

VIGIL shall maintain separate identity domains:

`Detection ID ≠ Track ID ≠ World Entity ID`

A detection identifies a perception result. A track identifies temporal continuity. A World Entity identifies the model-level representation consumed by downstream services.

### 4.1 Track association

For the initial implementation:

- a previously unseen Track receives one deterministic World Entity ID;
- subsequent updates from that Track preserve that entity;
- association is maintained explicitly;
- movement does not create a duplicate entity; and
- sophisticated multi-source identity resolution remains deferred.

### 4.2 Fused association

`FusedEstimate.associationId` is a deterministic association key for the estimate. It is **not** an authoritative physical identity and must not be interpreted as a World Entity ID.

The current implementation derives this key deterministically from contributing track IDs. This is an implementation convenience that preserves deterministic behavior while leaving identity resolution replaceable.

When a fused estimate references:

- no previously associated tracks, the updater may allocate a new World Entity;
- tracks already associated with the same World Entity, the updater may update that entity; or
- tracks associated with different existing World Entities, the updater must reject the update as an unresolved identity conflict and must not mutate authoritative state.

## 5. Spatial State Contract

World Entity spatial state shall use the coordinate conventions and units defined by the Spatial World Model specification.

Required supported state includes:

- entity ID;
- entity type;
- current position when available;
- velocity when available;
- confidence;
- last event/update time;
- contributing track reference(s);
- contributing detection references or equivalent provenance; and
- validity/freshness state.

All spatial values must have an explicit coordinate-frame interpretation. Unavailable values remain unavailable; they must not become zero or another fabricated default.

## 6. Confidence and Uncertainty

Confidence and uncertainty are independent properties.

- **Confidence** describes support for a classification, association, or conclusion.
- **Uncertainty** describes expected error or imprecision in an estimated quantity.

Missing uncertainty remains unknown rather than becoming fabricated precision.

A fused estimate's `qualified` flag means the estimate is suitable for use as a qualified result under the active fusion policy. It does **not** mean that a conflict occurred.

Therefore:

- compatible or single-evidence results may be `qualified`;
- material disagreement that causes evidence reduction/deferment produces an unqualified result; and
- quality metadata must explain the reason.

Priority is outside this contract.

## 7. Temporal Contract

Track and fused updates contain event/observation time representing when the state applies, plus ingestion/processing timing where available.

The updater shall distinguish event time from processing order.

### 7.1 Ordering

- newer or equal state may advance current authoritative state;
- older state must not overwrite newer current state;
- equal timestamps must be handled deterministically; and
- rejected out-of-order information must not corrupt current state.

Fusion must apply its configured temporal compatibility policy before producing a fused estimate.

### 7.2 Freshness

Freshness is derived from the supported observation/event time and configured policy.

Stale state remains represented when appropriate and must not be presented as newly observed merely because processing occurred recently.

## 8. Lifecycle

The intended Track lifecycle is:

`TENTATIVE → CONFIRMED → DEGRADED → STALE → TERMINATED`

The World Model shall preserve identity when a track becomes stale or terminated.

A terminated Track does not by itself authorize deletion of its World Entity. Future fusion/identity layers may retain an entity through other supporting tracks.

## 9. Provenance Contract

Derived World Model state must retain a traceable provenance path:

`World Entity → Track(s) / Fused Estimate → Detection(s) → Observation(s) / Source(s)`

A fused update must preserve all contributing track and detection references that support the resulting state.

Provenance must not be reduced to only the latest contributing detection.

## 10. Update Semantics

### 10.1 Track update

```text
Track update
    ↓
Validate
    ↓
Resolve Track → Entity
    ↓
Apply spatial/kinematic state
    ↓
Apply confidence/uncertainty/freshness/validity
    ↓
Preserve provenance
    ↓
Commit World Model
    ↓
Emit state-change event
```

### 10.2 Fused estimate update

```text
Fused Estimate
    ↓
Validate
    ↓
Resolve contributing Track(s) → Entity
    ↓
Reject unresolved identity conflict
    ↓
Apply fused state
    ↓
Apply confidence/uncertainty/freshness/validity
    ↓
Preserve all contributing provenance
    ↓
Commit World Model
    ↓
Emit state-change event
```

Both paths terminate at the same controlled World Model update boundary.

## 11. Events

Initial event vocabulary:

- `WorldEntityCreated`
- `WorldEntityUpdated`
- `WorldEntityBecameDegraded`
- `WorldEntityBecameStale`
- `WorldEntityTerminated`

Events should identify event ID, event time, entity ID, update origin, affected/contributing tracks, state transition where applicable, and relevant detection provenance.

The event is downstream of the authoritative state change.

## 12. Error and Invalid-Input Rules

The updater must reject or quarantine invalid state, including:

- missing stable identity;
- unsupported entity type;
- non-finite spatial or kinematic values;
- invalid timestamps;
- incompatible frame information;
- invalid confidence;
- invalid uncertainty; or
- explicitly invalid upstream evidence.

Invalid input must not silently mutate authoritative state.

## 13. Determinism

Given the same initial World Model state, association state, valid input sequence, and configuration, VIGIL shall produce the same authoritative state and equivalent event sequence.

`associationId` is a deterministic key, not a substitute for authoritative identity.

## 14. Required Tests

The implementation is not complete until tests demonstrate at least:

1. a new Track creates exactly one World Entity;
2. a subsequent Track update preserves the same Entity;
3. movement does not create a duplicate;
4. velocity and confidence propagate when supported;
5. unavailable uncertainty remains unknown;
6. provenance remains traceable;
7. older updates cannot overwrite newer state;
8. stale and terminated tracks retain represented identity;
9. events occur only after state mutation;
10. invalid input cannot corrupt authoritative state;
11. repeated identical updates are deterministic;
12. separate Tracks initially produce separate Entity IDs;
13. a valid FusedEstimate enters the World Model only through `WorldModelUpdater`;
14. multiple contributing tracks are preserved as provenance;
15. an unresolved fused association referencing distinct existing entities is rejected without mutation;
16. a qualified result represents suitability under policy rather than conflict occurrence; and
17. material disagreement is visible through quality metadata and does not get hidden by blind averaging.

## 15. Deferred Capabilities

The following remain intentionally deferred:

- sophisticated multi-source identity resolution;
- probabilistic multi-hypothesis association;
- full covariance/state-estimation mathematics;
- spatial transforms beyond the currently supported frame model;
- automatic calibration;
- persistent database implementation;
- distributed event infrastructure;
- advanced relationship/area recomputation;
- AI-driven identity decisions; and
- presentation/attention prioritization.

Deferred capabilities must integrate through this contract and must not bypass the World Model boundary.

## 16. Architectural Invariant

> **Track state is perception/temporal state. FusedEstimate is derived fusion state. World Entity state is authoritative spatial world state. WorldModelUpdater is the controlled boundary between them.**

The Track Manager must never become the World Model, and the World Model must never become the Track Manager or fusion engine.
