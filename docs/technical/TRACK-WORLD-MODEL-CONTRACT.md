# VIGIL Track → World Model Technical Contract

**Document:** Track → World Model Technical Contract  
**Version:** 0.1  
**Status:** Proposed for implementation  
**Parent:** Spatial World Model Technical Design

## 1. Purpose

This contract defines the boundary between temporal perception state (`Track`) and authoritative spatial state (`World Model`). It establishes the behavior that implementation must preserve as VIGIL evolves from a single-sensor deterministic tracker toward spatial/temporal fusion and multi-source world modeling.

The contract is intentionally implementation-independent where possible. It defines semantics first; classes, storage, frameworks, and persistence mechanisms are implementation details.

## 2. Scope

This milestone covers:

`Detection → TrackManager → Track → WorldModelUpdater → World Entity → World Model`

It establishes the interface needed for the later canonical lifecycle:

`Observation → Detection → Track → Spatial/Temporal Fusion → Spatial World Model → Spatial/Environmental Services → Relevance & Priority → Presentation / Attention`

Spatial/temporal fusion is not implemented by this contract. The boundary must, however, allow fused track state to enter the same World Model update path later.

## 3. Responsibilities

### 3.1 TrackManager

`TrackManager` owns temporal association and track maintenance.

It is responsible for:

- associating detections with tracks according to its configured association policy;
- maintaining track state and track lifecycle;
- estimating track position and, when supported, velocity and heading;
- maintaining contributing detection references;
- maintaining track timestamps and confidence/quality information;
- exposing a stable track identifier.

`TrackManager` does **not** own authoritative world entities and does not directly mutate `WorldModel`.

### 3.2 WorldModelUpdater

`WorldModelUpdater` is the boundary between track state and authoritative world state.

For each accepted track update it shall:

1. validate the track state and required spatial/time semantics;
2. determine the world entity associated with the track;
3. create or update the entity without losing entity identity;
4. propagate supported spatial/kinematic state;
5. preserve confidence, uncertainty, freshness, and validity semantics;
6. preserve provenance links back through the track and detections;
7. reject or ignore an update that is older than the authoritative current entity state according to the temporal rules in this contract;
8. update the World Model only after validation succeeds;
9. publish a state-change event only after the World Model state has changed.

The updater must not invent unsupported spatial information.

### 3.3 World Model

`WorldModel` owns the authoritative current projection of spatial state.

It is responsible for:

- storing current World Entities;
- returning current state and snapshots;
- maintaining entity identity independently from track identity;
- making current validity/freshness visible to consumers;
- remaining independent of the perception implementation that produced the state.

The World Model does not become a second tracker.

## 4. Identity Contract

VIGIL shall maintain separate identity domains:

`Detection ID ≠ Track ID ≠ World Entity ID`

Example:

`det-481 → track-72 → entity-184`

A detection identifies a perception result. A track identifies a temporal association of detections. A world entity identifies the model-level representation consumed by spatial services and applications.

### 4.1 Initial association policy

For the first implementation, association from Track to World Entity shall be deterministic and stable:

- a previously unseen track receives one new entity ID;
- subsequent updates from that track update the same entity;
- the association is maintained explicitly rather than deriving the entity ID from a display coordinate or detection ID;
- a track update must not create a duplicate entity solely because the entity moved;
- sophisticated multi-track/multi-sensor identity resolution is deferred to the fusion/identity layer.

The implementation should maintain an explicit track-to-entity association rather than coupling the two identifiers.

### 4.2 Identity uncertainty

A Track → Entity association is an association claim, not automatic proof of physical identity. The association must remain explainable and must be replaceable by a more capable identity/fusion policy later.

## 5. Spatial State Contract

The World Entity must represent spatial state using the coordinate conventions defined by the Spatial World Model specification.

Minimum required state for this milestone:

- entity ID;
- entity type;
- current position, when available;
- velocity, when available from the track;
- confidence;
- last update/event time;
- source track ID;
- contributing detection references or an equivalent provenance path;
- current validity/freshness state.

All spatial values must have an explicit coordinate-frame interpretation. Distances and velocities use the SWM documented units.

The updater must not silently convert an unavailable position, velocity, or other value into zero.

## 6. Confidence and Uncertainty

Confidence and uncertainty are independent properties.

- **Confidence** describes how strongly the system supports a classification, association, or conclusion.
- **Uncertainty** describes the expected error/range of an estimated quantity.

The updater must preserve confidence from the track where supported.

If the current Track implementation does not yet provide a required uncertainty representation, the World Model must represent uncertainty as unavailable/unknown rather than fabricating precision.

Priority is not part of this contract. Priority is applied later by the Relevance & Priority Engine and must not determine whether authoritative world state exists.

## 7. Temporal Contract

Track updates contain an event/observation time representing when the tracked state is applicable, plus ingestion timing where available.

The updater shall distinguish physical-event time from processing/ingestion order.

### 7.1 Ordering

For the initial implementation:

- an update newer than or equal to the entity's authoritative last-update time may advance current state;
- an update older than the authoritative current state must not overwrite newer current state;
- rejected out-of-order updates may be retained by future history infrastructure, but they must not corrupt the current projection;
- equal timestamps require deterministic handling and must not create duplicate entities.

This rule provides a safe initial behavior while leaving room for later temporal fusion and replay logic.

### 7.2 Freshness

Freshness is derived from the relationship between current time and the most recent supported observation/update time, subject to the configured freshness policy.

A loss of recent observations must not cause the entity to disappear from the World Model merely because it is no longer being observed.

## 8. Track Lifecycle to World State

The intended Track lifecycle is:

`TENTATIVE → CONFIRMED → DEGRADED → STALE → TERMINATED`

The first implementation may support this lifecycle incrementally, but its semantics are fixed by the following rules:

- **TENTATIVE:** insufficient history for normal confirmed confidence; entity may exist but must expose its provisional status.
- **CONFIRMED:** sufficient supporting evidence for normal track operation.
- **DEGRADED:** track remains usable but supporting evidence or quality has declined.
- **STALE:** no sufficiently recent supporting update; the last known state remains represented but is not presented as current/fresh.
- **TERMINATED:** the track is no longer maintained.

Track termination does **not** mean deleting the World Entity or erasing history. The entity's current validity must indicate that its active supporting track has terminated.

A future identity/fusion layer may keep a World Entity associated with other supporting tracks after one track terminates.

## 9. Provenance Contract

Every World Entity produced from a Track must preserve a traceable provenance path:

`World Entity → Track → Detection → Observation`

For this milestone, the World Entity must retain at least:

- source track ID;
- contributing detection IDs or equivalent references;
- enough information to resolve the upstream evidence through the appropriate stores/interfaces when those stores exist.

The updater must not replace provenance with only the latest detection.

## 10. Update Semantics

An accepted Track update follows this logical sequence:

```text
Track update
    ↓
Validate
    ↓
Resolve Track → Entity association
    ↓
Create or update World Entity
    ↓
Apply spatial/kinematic state
    ↓
Apply confidence/uncertainty/freshness/validity
    ↓
Preserve provenance
    ↓
Commit World Model state
    ↓
Emit state-change event
```

The event is downstream of the authoritative state change.

The updater must be deterministic for identical valid inputs and existing state.

## 11. Creation Rules

When a valid previously unseen Track is accepted:

1. allocate a distinct World Entity ID;
2. associate the Track with that Entity;
3. populate the minimum required state;
4. preserve the Track's provenance;
5. insert the entity into the World Model;
6. emit a `WorldEntityCreated` event after successful insertion.

The entity must not be created from an invalid Track.

## 12. Update Rules

When a valid update arrives for an associated Track:

1. locate the associated World Entity;
2. compare update time with the entity's authoritative last-update time;
3. reject an older update from overwriting newer state;
4. apply newer/equal state deterministically;
5. preserve entity ID;
6. preserve and extend provenance;
7. update validity/freshness as appropriate;
8. emit `WorldEntityUpdated` only after a state change.

Movement changes position; they do not create a new entity.

## 13. Events

Initial event vocabulary:

- `WorldEntityCreated`
- `WorldEntityUpdated`
- `WorldEntityBecameDegraded`
- `WorldEntityBecameStale`
- `WorldEntityTerminated`

Events should identify:

- event ID;
- event time;
- entity ID;
- affected track ID when applicable;
- state before and after when applicable;
- relevant provenance/evidence references.

The first implementation may use a simple in-memory publisher/test sink. A distributed event bus is explicitly out of scope for this milestone.

## 14. Error and Invalid-Input Rules

The updater must reject or quarantine updates when required information is invalid, including:

- missing stable Track ID;
- unsupported/invalid entity type;
- non-finite spatial coordinates;
- invalid timestamps;
- incompatible coordinate-frame information;
- invalid confidence values;
- other values explicitly prohibited by the SWM contract.

Invalid input must not silently mutate authoritative world state.

## 15. Determinism

Given the same:

- initial World Model state;
- Track-to-Entity association state;
- valid Track update sequence;
- configuration;

VIGIL shall produce the same World Model state and equivalent event sequence.

No AI model or nondeterministic identity mechanism is required for this milestone.

## 16. Concurrency

The initial implementation may use an in-memory synchronization strategy appropriate to the selected runtime. Regardless of implementation, a single logical entity update must be atomic from the perspective of World Model consumers:

**state update and its associated state-change event must have a defined ordering.**

Concurrency optimization is secondary to correctness and determinism in this milestone.

## 17. Required Tests

The implementation is not complete until tests demonstrate at least:

1. a new Track creates exactly one World Entity;
2. a subsequent update to the same Track updates the same Entity;
3. movement changes entity position without creating a duplicate;
4. velocity propagates when available;
5. confidence propagates correctly;
6. unavailable values remain unknown rather than becoming zero/default guesses;
7. provenance remains traceable from Entity to Track and contributing Detections;
8. older/out-of-order Track updates cannot overwrite newer current state;
9. Track lifecycle changes are reflected in entity validity/state without deleting historical identity;
10. stale entities remain represented;
11. terminated tracks do not silently erase their entities;
12. creation/update events occur only after the corresponding World Model mutation;
13. invalid Track input cannot corrupt authoritative state;
14. repeated identical valid updates are deterministic;
15. separate Tracks initially produce separate Entity IDs.

## 18. Deferred Capabilities

The following are intentionally deferred rather than left ambiguous:

- sophisticated multi-sensor identity resolution;
- probabilistic multi-hypothesis tracking;
- full covariance/uncertainty mathematics if not yet supported by Track;
- spatial/temporal fusion algorithms;
- persistent database implementation;
- distributed event infrastructure;
- advanced relationship and area recomputation;
- AI-driven identity decisions;
- presentation and attention prioritization.

These capabilities must integrate through the contracts defined here rather than bypassing the World Model boundary.

## 19. Implementation Boundary

The first code implementation should introduce a dedicated `WorldModelUpdater` and strengthen the minimum `WorldEntity` representation needed to satisfy this contract.

The implementation should preserve the existing VIGIL spatial primitives and coordinate/unit conventions rather than introducing competing spatial representations.

Technology versions and libraries are implementation choices. When coding begins, the repository's current toolchain should be evaluated for supported, maintainable versions rather than freezing the project to an older external application's dependency versions.

## 20. Architectural Invariant

The following invariant is mandatory:

> **Track state is perception/temporal state. World Entity state is authoritative spatial world state. WorldModelUpdater is the controlled boundary between them.**

The Track Manager must never become the World Model, and the World Model must never become the Track Manager.
