# VIGIL Project Progress & Engineering Bookmark

**Project:** VIGIL — Visual Intelligence & Geographic Information Layer  
**Branch:** `feature/spatial-core`  
**Status:** Architecture established; spatial-core implementation underway

## Purpose of This File

This document is a persistent bookmark for the state of the VIGIL project. It records what has been established, what has been implemented, what remains undefined, and the recommended engineering sequence so another engineer—or a future development session—can resume work without reconstructing the project history.

---

## 1. Core System Definition

VIGIL is an **information and presentation system**, not an action system.

The device has no physical appendages or actuators. Its purpose is to acquire, process, correlate, retain, prioritize, and present environmental information to a human user through a screen or future visual interface.

The central capability is to expand human environmental perception through breadth, speed, persistence, correlation, and presentation.

The human remains the consumer, interpreter, decision-maker, and actor.

### Core operating principle

> **Machine-speed acquisition and processing → persistent environmental information → human-speed interpretation and decision.**

---

## 2. Canonical Architecture

The repository's architecture specification now defines the single canonical pipeline:

```text
Sensors / Authorized Data Sources
            ↓
      Observations
            ↓
       Detections
            ↓
Tracking / Track Continuity
            ↓
     Spatial / Temporal Fusion
            ↓
     Spatial World Model
            ↓
Spatial / Environmental Services
            ↓
 Relevance & Priority Engine
            ↓
 Presentation / Attention Layer
            ↓
           USER
            ↓
 Human Decision / Action
```

World-state changes produce history/events after the state change:

```text
World-state change
        ↓
 Events / History
```

AI is an optional information-analysis and query layer over structured world state, evidence, and history. It is not the authority that determines physical truth or executes physical action.

The architecture specification also explicitly defines an **Information & Event Output / Integration layer** for authorized external information consumers. This layer communicates structured information, events, notifications, attention requests, and similar outputs; it does not make VIGIL a physical-action or actuator system.

---

## 3. Architecture Decisions Already Established

### Information-first design

- VIGIL presents information rather than physically acting on the environment.
- The system may support navigation, search, inspection, environmental awareness, authorized security monitoring, and generic spatial targeting.
- The user is responsible for interpreting information and deciding what to do with it.

### Full lifecycle is the architecture

The architecture is not limited to the portions already implemented. The intended lifecycle is:

1. Acquire authorized source information.
2. Record observations.
3. Produce detections/perception results.
4. Maintain temporal tracks.
5. Perform spatial/temporal fusion.
6. Update the Spatial World Model.
7. Update dependent spatial/environmental services.
8. Record world-state changes as events/history.
9. Perform optional AI or other information analysis.
10. Determine relevance and priority.
11. Manage attention and presentation.
12. Present information.
13. Human interprets, decides, and acts.

Implementation is incremental through this lifecycle. An incomplete implementation must not redefine the architecture.

### Separation of knowledge layers

VIGIL distinguishes:

1. Raw sensor observations
2. Perception/detections
3. Track continuity
4. Spatial/temporal fusion
5. World-model beliefs/current state
6. Derived spatial/environmental characteristics
7. AI interpretation or analysis
8. User-facing presentation

### Tracking before the World Model

Tracking is part of the pre-World-Model perception/fusion path. It establishes temporal continuity; it does not own authoritative world state.

### Events after world-state changes

Events and history describe meaningful changes in established or updated world state. They are not another perception stage and do not replace the state that produced them.

### Information & Event Output / Integration

VIGIL may expose structured information and events to authorized external consumers. This is explicitly an information/integration boundary, not an action or actuator layer.

### Persistent environmental information

Transient detections can become tracks and remain represented in world state/history after leaving the immediate sensor view.

### Priority and attention

Priority is separate from truth. A low-priority entity still exists in the World Model; priority only determines what receives user attention first.

Confidence and priority are independent. Priority should be dynamic, explainable, and influenced by explicit contextual factors.

### Human bandwidth vs. machine bandwidth

VIGIL should be capable of processing more information than a human can simultaneously consume. The Presentation / Attention Layer therefore acts as an information filter and salience manager rather than merely rendering everything available.

### Fast and slow processing paths

The fast path must maintain continuous low-latency environmental awareness. Expensive analysis must not block immediate useful presentation.

### Latency as a first-class concern

The important performance metric is environment-to-useful-display latency, not merely frame rate. Sensor acquisition, observation ingestion, perception, detection-to-track association, fusion, World Model update, prioritization, presentation, and end-to-end latency should be measurable.

### Spatial foundations

The architecture includes explicit handling for geographic/global coordinates, local Cartesian coordinates, sensor coordinates, device/user coordinates, display coordinates, transformations, units, calibration, sensor health, time, timestamp uncertainty, areas/zones, and spatial relationships.

### Identity separation

The architecture distinguishes detection identity, track identity, and world-entity identity. A track is continuity evidence, not automatically a permanent real-world identity.

### Current state vs. history

The World Model's current state and historical/replay information are separate concepts and should not be conflated.

### Provenance and explainability

VIGIL should be able to answer what it currently believes, why it believes it, and how certain that belief is. Evidence/provenance follows information through the pipeline.

### Security and authorization

Security, authorization, privacy, and audit information must follow data through the pipeline. VIGIL must only use authorized data sources.

### Offline-first

The local core should function without requiring cloud services. Cloud functionality can be optional.

### Terminology rule

Technical terminology should remain precise while avoiding language that implies autonomous physical intervention, offensive action, or weapon control.

---

## 4. Documentation Already Established

### `README.md`

Documents VIGIL's purpose, information-first design, core pipeline, fast/slow paths, latency, presentation, priority, confidence, persistence, World Model, identity separation, history/replay, security, authorization, offline-first operation, and non-goals.

### `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md`

Version 0.3 is the canonical architecture baseline. It explicitly places tracking before the World Model, places events/history after world-state changes, defines the Information & Event Output / Integration layer, and states that the complete lifecycle is the intended architecture even while implementation proceeds incrementally.

### `docs/architecture/DOCUMENTATION-RULES.md`

Defines the technical terminology and documentation style required for VIGIL.

### `docs/technical/SPATIAL-WORLD-MODEL.md`

Defines the intended spatial/world-model architecture including coordinates, time, observations, detections, tracks, entities, areas/zones, relationships, sensors, calibration, health, confidence, uncertainty, history, persistence, security, AI boundaries, simulation, and implementation order.

---

## 5. Implemented Engineering Work

The current Java 17 `spatial-core` module contains the initial spatial foundations.

### Implemented

- `GeoPoint`
- `LocalPosition`
- `SpatialMath`
- `Confidence`
- `EntityType`
- `Observation`
- `Detection`
- `Track`
- `TrackManager`
- `WorldEntity`
- `WorldModel`

### Observation

The current Observation model captures source identity, event time, ingestion time, sensor type, and an optional payload reference.

### Detection

Immutable detection representation has been added as the next perception layer above observations.

### Track

Persistent track representation has been added to maintain continuity across successive detections.

### TrackManager

A deterministic association implementation currently associates detections using proximity and matching entity type, calculates local velocity from successive detections, preserves detection history, and does not infer permanent identity beyond supplied detection evidence.

### WorldModel

An initial in-memory current-state projection exists using a concurrent map, with operations for upsert, find, snapshot, and clear.

### Tests

Tests currently cover track continuity, velocity calculation, distant-object separation, type separation, and invalid association thresholds.

### CI

A GitHub Actions workflow exists for Java 17 / Gradle 8.10 spatial-core testing.

---

## 6. Current Engineering Position

**Completed conceptual milestone:** Observation → Detection → Track.

**Current implementation milestone:** deterministic detection-to-track association is implemented and tested.

**Next engineering milestone:** **Track → Spatial World Model.**

The next step should establish the contract between tracking and persistent world state while preserving the distinction between perception evidence, track continuity, and current world-state beliefs.

---

## 7. Remaining Engineering Gaps

### A. World Model contract — **NEXT**

Define exactly how tracks become world-state representations, including entity fields, track-to-entity relationships, current position and motion, confidence, uncertainty, provenance, freshness, associated tracks, lifecycle state, and current-state/history boundaries.

### B. Temporal model

Define event time, ingestion time, processing time, world-state time, presentation time, out-of-order observations, delayed sensors, clock differences, timestamp uncertainty, and replay behavior.

### C. Spatial uncertainty and estimation

Define how uncertainty is represented and propagated through position, motion, coordinate transformations, sensor measurements, and fusion.

### D. Sensor abstraction

Define sensor identity, capabilities, coordinate frame, calibration, health, validity, timestamp characteristics, and observation stream contracts.

### E. Multi-sensor fusion

Define temporal alignment, coordinate transformation, association, duplicate observations, conflicting observations, sensor reliability, and fusion confidence.

### F. Event/message architecture

Determine how information moves between processing stages without prematurely introducing distributed infrastructure. Candidate approaches include in-process events, queues, streams, synchronous calls, or a hybrid.

### G. Performance architecture

Establish measurable targets for ingestion, detection, detection-to-track, fusion, World Model update, prioritization, presentation, end-to-end latency, throughput, concurrent tracks/entities, and resource usage.

### H. Relevance and Priority Engine

Define relevance factors, priority scoring, dynamic reprioritization, stability/hysteresis, user context, spatial proximity, motion/change, zone relationships, persistence/recurrence, and explainability.

### I. Attention Management

Define attention budget, salience, alert levels, persistent/transient information, suppression, attention transitions, and clutter management.

### J. Presentation architecture

Define a device-independent presentation model supporting desktop, mobile, camera overlays, AR displays, and wearable/visual interfaces.

### K. Device spatial model

Eventually represent device position, orientation, camera mounting geometry, IMU, compass, GPS/GNSS, sensor-to-device transforms, and display geometry.

### L. Persistence and replay

Define memory-only state, persisted state, retention, storage, indexing, compression, replay, historical querying, and crash recovery.

### M. Simulation environment

Build simulated environments and sensors so the complete pipeline can be exercised without physical hardware.

### N. Testing strategy

Expand into integration, simulation, performance, fault-injection, replay/determinism, sensor-degradation, and stale-data tests.

### O. User/context model

Define the information the presentation system can use about user location, orientation, current view, task, selected entity/object, navigation context, attention state, and presentation preferences.

### P. Application-layer architecture

After core contracts stabilize, define surrounding modules for navigation, search, inspection, environmental awareness, authorized security monitoring, generic spatial information/targeting workflows, and future visual/wearable interfaces.

---

## 8. Recommended Engineering Sequence

### Phase 1 — Spatial Truth

1. Observation ✅
2. Detection ✅
3. Track ✅
4. **World Model ← CURRENT**
5. Track/world-state relationships
6. Spatial uncertainty
7. Current state vs. history

### Phase 2 — Temporal & Fusion

8. Time model
9. Sensor abstraction
10. Coordinate transformations
11. Multi-sensor fusion
12. Sensor health/validity
13. Provenance

### Phase 3 — Real-Time Architecture

14. Event/message model
15. Processing pipeline/concurrency
16. Latency instrumentation
17. Throughput and performance testing

### Phase 4 — Information Intelligence

18. Relevance
19. Priority
20. Attention management
21. User/context model
22. Information lifecycle

### Phase 5 — Presentation

23. Presentation model
24. Spatial visualization
25. Display coordinate transformation
26. Alerts/indicators
27. Information clutter management
28. User interaction

### Phase 6 — Device Integration

29. GPS/GNSS
30. IMU
31. Compass
32. Cameras
33. Camera calibration
34. Device/sensor geometry

### Phase 7 — Simulation & Validation

35. Simulated sensors
36. Simulated environments
37. Replay
38. Fault injection
39. Performance benchmarking

### Phase 8 — Applications

40. Navigation
41. Search
42. Inspection
43. Environmental awareness
44. Authorized security monitoring
45. Future visual/wearable interfaces

---

## 9. Immediate Next Task

### Track → World Model

Before adding more high-level functionality, implement the contract that promotes track information into persistent spatial world state.

The implementation should answer:

> **What does the World Model currently believe exists, where is it, how recently was it observed, how confident is that belief, what track/evidence supports it, and what happens when the evidence becomes stale?**

The implementation should include tests for track-to-entity projection, entity updates from continued tracks, multiple simultaneous entities, track continuity, stale tracks, disappearing entities, confidence propagation, provenance preservation, and current state vs. history boundaries.

---

## 10. Definition of Progress

A milestone is complete when:

- The architectural concept is documented.
- The data contract is explicit.
- The implementation reflects the contract.
- Unit/integration tests cover expected behavior and important failure modes.
- CI validates the change.
- The implementation does not collapse distinctions between observations, detections, tracks, world state, history, or presentation.
- Documentation remains understandable to an engineer who was not present for the design discussion.

---

## 11. Project Resume Point

**Resume here:** `Track → Spatial World Model`  
**Branch:** `feature/spatial-core`

**Do not modify:** `buccees/ObsidianShareTarget`  
**Do not use for VIGIL work:** `buccees/ObsidianShareTarget-AgentTest`

The architecture is now the complete intended lifecycle, while implementation remains deliberately incremental. The next engineering work should convert the Track → World Model boundary into an explicit technical contract and tested implementation without expanding the architecture unnecessarily.
