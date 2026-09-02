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

The central capability is to expand human environmental perception through:

- Breadth — processing more information than a person can intake unaided.
- Speed — acquiring and processing information at machine speed.
- Persistence — retaining useful information after it leaves the user's immediate view.
- Correlation — combining observations across sensors, time, and spatial context.
- Presentation — selecting and displaying the information most useful to the user.

The human remains the consumer, interpreter, decision-maker, and actor.

### Core operating principle

> **Machine-speed acquisition and processing → persistent environmental information → human-speed interpretation and decision.**

---

## 2. Approved High-Level Architecture

```text
Sensors / Authorized Data Sources
            ↓
      Observations
            ↓
       Detections
            ↓
         Tracks
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

AI is an optional information-analysis and query layer over structured world state. It is not the authority that determines what physically exists and does not autonomously execute consequential physical actions.

---

## 3. Architecture Decisions Already Established

### Information-first design

- VIGIL presents information rather than physically acting on the environment.
- The system may support navigation, search, inspection, environmental awareness, authorized security monitoring, and generic spatial targeting/pointing concepts.
- The user is responsible for interpreting information and deciding what to do with it.

### Separation of knowledge layers

VIGIL distinguishes:

1. Raw sensor observations
2. Perception/detections
3. Track continuity
4. World-model beliefs/current state
5. Derived spatial/environmental characteristics
6. AI interpretation or analysis
7. User-facing presentation

Sensors do not directly define the system's understanding of the world.

### Persistent environmental information

Transient detections can become tracks and remain represented in the world state/history after leaving the immediate sensor view.

### Priority and attention

Priority is separate from truth. A low-priority entity still exists in the World Model; priority only determines what receives user attention first.

Confidence and priority are independent:

- High confidence does not automatically mean high priority.
- Moderate-confidence information can deserve attention if its potential relevance is high.

Priority should be dynamic, explainable, and influenced by factors such as proximity, movement, rate of change, zone entry, task relevance, unexpected appearance/disappearance, path/area intersection, persistence, and recurrence.

### Human bandwidth vs. machine bandwidth

VIGIL should be capable of processing more information than a human can simultaneously consume. The Presentation / Attention Layer therefore acts as an information filter and salience manager rather than merely rendering everything available.

### Fast and slow processing paths

The fast path must maintain continuous low-latency environmental awareness. Expensive analysis must not block immediate useful presentation.

### Latency as a first-class concern

The important performance metric is environment-to-useful-display latency, not merely frame rate.

Important stages to instrument include:

- Sensor acquisition
- Observation ingestion
- Perception
- Detection-to-track association
- World-model update
- Prioritization
- Rendering/presentation
- End-to-end latency

Observation age/data freshness is also first-class information.

### Spatial foundations

The architecture includes explicit handling for:

- Geographic/global coordinates
- Local Cartesian coordinates
- Sensor coordinates
- Device/user coordinates
- Display coordinates
- Coordinate transformations
- Units
- Camera calibration
- Sensor health and validity
- Time and timestamp uncertainty
- Areas/zones rather than only points
- Spatial relationships

### Identity separation

The architecture distinguishes:

- Detection identity
- Track identity
- World-entity identity

A track is continuity evidence, not automatically a permanent real-world identity.

### Current state vs. history

The World Model's current state and historical/replay information are separate concepts and should not be conflated.

### Provenance and explainability

VIGIL should be able to answer:

> **What does VIGIL currently believe about the environment, why does it believe that, and how certain is it?**

Evidence/provenance therefore follows information through the pipeline.

### Security and authorization

Security, authorization, and audit information must follow data through the pipeline. VIGIL must only use authorized data sources.

### Offline-first

The local core should function without requiring cloud services. Cloud functionality can be optional.

### Terminology rule

Technical terminology should remain precise while avoiding language that implies autonomous physical intervention, offensive action, or weapon control.

Preferred vocabulary includes:

- Observations
- Detections
- Tracks
- Track Continuity / Track Maintenance
- Spatial World Model
- Sensor Fusion
- Spatial Estimation
- Motion Estimation
- Uncertainty Quantification
- Confidence
- Data Freshness / Observation Age
- Relevance / Priority
- Attention Management
- Information Prioritization
- Information Presentation
- Spatial Visualization / Augmented Visualization
- Temporal History
- Data Provenance
- Sensor Health / Sensor Validity
- Sensor Calibration
- Regions / Zones / Areas of Interest
- Change Detection
- Anomaly Detection
- Motion Projection / Trajectory Estimation
- Spatial Search
- Object of Interest / Entity of Interest

Avoid unnecessary terminology associated with autonomous physical intervention or weapon control.

---

## 4. Documentation Already Established

### `README.md`

Expanded to document:

- VIGIL purpose and system boundary
- Information-first design
- Core pipeline
- Fast/slow processing paths
- Latency
- Presentation and attention
- Priority vs. truth
- Confidence vs. priority
- Persistence
- World Model
- Identity separation
- History/replay
- Security/authorization
- Offline-first operation
- Current engineering direction
- Non-goals
- Documentation references

### `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md`

Architecture specification, currently at v0.2, establishing the system architecture and engineering direction.

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

A deterministic association implementation currently:

- Associates detections using proximity.
- Requires matching entity type.
- Calculates local velocity from successive detections.
- Preserves detection history.
- Does not infer permanent identity beyond supplied detection evidence.

### WorldModel

An initial in-memory current-state projection exists using a concurrent map, with operations for upsert, find, snapshot, and clear.

### Tests

Tests currently cover:

- Track continuity
- Velocity calculation
- Distant-object separation
- Type separation
- Invalid association thresholds

### CI

A GitHub Actions workflow exists for Java 17 / Gradle 8.10 spatial-core testing.

---

## 6. Current Engineering Position

**Completed conceptual milestone:** Observation → Detection → Track.

**Current implementation milestone:** deterministic detection-to-track association is implemented and tested.

**Next engineering milestone:** **Track → Spatial World Model.**

The next step should not simply be adding another class. It should establish the contract between tracking and persistent world state while preserving the distinction between perception evidence, track continuity, and current world-state beliefs.

---

## 7. What Another Engineer Can Understand Today

An engineer joining the project should already be able to answer:

- What VIGIL is.
- What VIGIL is not.
- Who/what consumes the information.
- What the major pipeline stages are.
- Why persistence matters.
- Why latency matters.
- Why priority and attention are first-class systems.
- Why confidence and priority are separate.
- Why provenance matters.
- Why coordinate systems and time are explicit.
- Why current state and history are separate.
- Why the architecture is offline-first.
- What terminology should be used.
- What has already been implemented.
- What the immediate next engineering milestone is.

They cannot yet answer every implementation-level question, because several technical contracts are intentionally still being designed.

---

## 8. Remaining Engineering Gaps

### A. World Model contract — **NEXT**

Define exactly how tracks become world-state representations.

Need to establish:

- WorldEntity fields
- Track-to-entity relationship
- Current position
- Motion state
- Confidence
- Uncertainty
- Provenance
- Observation freshness
- Associated tracks
- Lifecycle state
- Current-state vs. historical-state boundaries
- Stale/disappeared entity behavior

### B. Temporal model

Define:

- Event time
- Ingestion time
- Processing time
- World-state time
- Presentation time
- Out-of-order observations
- Delayed sensors
- Clock differences
- Timestamp uncertainty
- Replay behavior

### C. Spatial uncertainty and estimation

Define how uncertainty is represented and propagated through:

- Position estimates
- Motion estimates
- Coordinate transformations
- Sensor measurements
- Fusion

### D. Sensor abstraction

Define a common sensor/data-source contract covering:

- Sensor identity
- Capabilities
- Coordinate frame
- Calibration
- Health
- Validity
- Timestamp characteristics
- Observation stream

### E. Multi-sensor fusion

Define how information from multiple sensors is correlated, including:

- Temporal alignment
- Coordinate transformation
- Association
- Duplicate observations
- Conflicting observations
- Sensor reliability
- Fusion confidence

### F. Event/message architecture

Determine how information moves between processing stages without prematurely introducing distributed infrastructure.

Potential approaches include:

- In-process events
- Queues
- Streams
- Synchronous calls
- Hybrid architecture

### G. Performance architecture

Establish measurable targets for:

- Observation ingestion latency
- Detection latency
- Detection-to-track latency
- World-model update latency
- Prioritization latency
- Presentation latency
- End-to-end environment-to-useful-display latency
- Throughput
- Concurrent tracks/entities
- Resource usage

### H. Relevance and Priority Engine

Define the formal priority/relevance model, including:

- Relevance factors
- Priority scoring
- Dynamic reprioritization
- Priority stability/hysteresis
- User context
- Spatial proximity
- Motion/change
- Zone/area relationships
- Persistence/recurrence
- Explainability

### I. Attention Management

Define how machine-scale information is reduced to human-usable presentation.

Need to establish:

- Attention budget
- Salience
- Alert levels
- Persistent vs. transient information
- Information suppression
- Attention transitions
- Clutter management

### J. Presentation architecture

Define a device-independent presentation model supporting future:

- Desktop displays
- Mobile screens
- Camera overlays
- AR displays
- Wearable/visual interfaces

The World Model should not become coupled to a particular display technology.

### K. Device spatial model

Eventually represent:

- Device position
- Device orientation
- Camera mounting geometry
- IMU
- Compass
- GPS/GNSS
- Sensor-to-device transforms
- Display geometry

### L. Persistence and replay

Define:

- What is memory-only
- What is persisted
- Retention policy
- Storage format
- Indexing
- Compression
- Replay format
- Historical querying
- Crash recovery

### M. Simulation environment

Build simulated environments and sensors so the complete pipeline can be exercised without physical hardware.

### N. Testing strategy

Expand beyond unit tests into:

- Integration tests
- Simulation tests
- Performance tests
- Fault-injection tests
- Replay/determinism tests
- Sensor degradation tests
- Stale-data tests

### O. User/context model

Define the information the presentation system can use about:

- User location
- Orientation
- Current view
- Current task
- Selected entity/object
- Navigation context
- Attention state
- Presentation preferences

This context should influence presentation and relevance without corrupting authoritative environmental state.

### P. Application-layer architecture

After the core contracts stabilize, define the surrounding application modules for:

- Navigation
- Search
- Inspection
- Environmental awareness
- Authorized security monitoring
- Generic spatial information/targeting workflows
- Future visual/wearable interfaces

---

## 9. Recommended Engineering Sequence

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

## 10. Immediate Next Task

### Track → World Model

Before adding more high-level functionality, implement the contract that promotes track information into persistent spatial world state.

The implementation should answer:

> **What does the World Model currently believe exists, where is it, how recently was it observed, how confident is that belief, what track/evidence supports it, and what happens when the evidence becomes stale?**

The implementation should include tests for:

- Track-to-entity projection
- Entity updates from continued tracks
- Multiple simultaneous entities
- Track continuity
- Stale tracks
- Disappearing entities
- Confidence propagation
- Provenance preservation
- Current state vs. history boundaries

---

## 11. Definition of Progress

A milestone is considered complete when:

- The architectural concept is documented.
- The data contract is explicit.
- The implementation reflects the contract.
- Unit/integration tests cover expected behavior and important failure modes.
- CI validates the change.
- The implementation does not collapse distinctions between observations, detections, tracks, world state, history, or presentation.
- Documentation remains understandable to an engineer who was not present for the design discussion.

---

## 12. Project Resume Point

**Resume here:** `Track → Spatial World Model`

**Branch:** `feature/spatial-core`

**Do not modify:** `buccees/ObsidianShareTarget`  
**Do not use for VIGIL work:** `buccees/ObsidianShareTarget-AgentTest`

The VIGIL architecture is established enough for implementation to proceed systematically. The remaining work is primarily the conversion of architectural concepts into explicit technical contracts, tested implementations, performance requirements, and eventually physical-device/presentation integrations.
