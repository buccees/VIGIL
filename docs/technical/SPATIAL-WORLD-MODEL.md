# VIGIL Spatial World Model — Technical Design

**Document:** Spatial World Model Technical Design  
**Version:** 0.1  
**Status:** Proposed for implementation review  
**Parent:** VIGIL System Architecture

## 1. Purpose and Scope

The Spatial World Model (SWM) is VIGIL's authoritative spatial state layer. It represents what VIGIL currently believes about the physical or simulated environment, the evidence supporting those beliefs, and the uncertainty associated with them.

The SWM does not directly control sensors, weapons, vehicles, or other consequential actuators. It provides shared spatial truth to navigation, security monitoring, targeting, search, inspection, environmental-awareness, and training/simulation applications.

The model must support both offline/local operation and connected deployments.

## 2. Design Principles

1. **Evidence before belief.** Preserve source observations so derived state can be explained and reconstructed.
2. **Facts and interpretations remain separate.** A sensor report, a perception result, a fused world belief, and an AI interpretation are different things.
3. **Unknown is a valid value.** Missing, stale, ambiguous, or uncalibrated information must never silently become zero, false, or a guess.
4. **Time is first-class.** Every observation and state transition has an explicit time model.
5. **Uncertainty is first-class.** Spatial estimates and identities carry uncertainty rather than false precision.
6. **Identity is layered.** Detection, track, and world-entity identity are distinct.
7. **The world model owns spatial truth.** Navigation and other applications consume it; they do not create competing spatial authorities.
8. **History is retained separately from current state.** Current state answers “what is believed now”; history answers “what happened and why.”
9. **Security follows the data.** Access, provenance, authorization, audit, and retention apply throughout the data lifecycle.
10. **Deterministic core, replaceable perception.** Spatial calculations should be testable without requiring an AI model or live hardware.

## 3. Terminology

| Term | Meaning |
|---|---|
| Observation | A source-reported measurement or event. |
| Evidence | An observation or artifact retained to support a conclusion. |
| Detection | A perception result identifying a possible object or feature in an observation. |
| Track | A temporal association of detections believed to represent the same physical entity. |
| World Entity | A model-level representation of a physical or logical entity. |
| Area | A spatial region with a defined boundary or semantic purpose. |
| Relationship | A derived or declared spatial/semantic relation between entities. |
| Target | A selected spatial objective represented independently of any weapon or actuator. |
| Belief | VIGIL's current modelled estimate of a property or entity. |
| Event | A meaningful state change or occurrence recorded in world history. |

## 4. Coordinate Systems and Units

VIGIL shall explicitly distinguish coordinate frames.

### 4.1 Required frames

- **Global:** latitude, longitude, altitude using a documented geodetic reference system.
- **World:** local Cartesian coordinates used for spatial computation within an operating area.
- **Sensor:** coordinates relative to a camera, IMU, GPS receiver, or other sensor.
- **Device:** coordinates relative to the host device/user platform.
- **Display:** coordinates used by a UI, AR overlay, VR scene, or other presentation system.

Transform chains shall be explicit:

`World ↔ Sensor ↔ Device ↔ Display`

Global-to-world transformation must also be explicit and versioned.

### 4.2 Units

- Distance: meters.
- Speed: meters/second.
- Acceleration: meters/second².
- Angles: degrees at API boundaries unless a component explicitly documents radians internally.
- Time duration: milliseconds or a typed duration representation.
- Latitude/longitude: decimal degrees.
- Altitude: meters with a documented vertical datum.

No API may rely on an undocumented unit convention.

## 5. Time Model

Every observation should carry:

- `event_time`: when the source says the event occurred.
- `ingest_time`: when VIGIL received it.
- `source_clock`: source clock identifier or synchronization state when available.
- `time_uncertainty`: estimated timestamp uncertainty.

The model must tolerate clock drift, delayed delivery, out-of-order observations, and unavailable source time.

State updates must not assume ingestion order equals physical-event order.

## 6. Evidence and Observation Model

An observation is the immutable source-level record from which downstream perception or fusion may derive information.

Conceptual fields:

```text
Observation
├── id
├── source_id
├── sensor_type
├── event_time
├── ingest_time
├── payload_reference
├── measurement_metadata
├── quality
└── provenance
```

Examples include:

- camera frame metadata
- GPS position
- IMU sample
- compass heading
- map feature
- operator-provided location
- simulator-generated measurement

Raw payload storage may be external to the SWM, but the observation must retain a stable reference to its source evidence when policy permits.

Observations are not themselves world entities.

## 7. Detection Model

A detection is a perception result derived from one or more observations.

A detection may contain:

- detection ID
- source observation IDs
- sensor/source ID
- detection time
- category/classification
- bounding geometry or image-space location
- estimated world position when available
- dimensions when available
- confidence
- uncertainty
- attributes
- model/version provenance

A detection represents **“something was detected”**, not **“this is definitively world entity X.”**

## 8. Track Model

A track associates detections over time.

A track shall contain:

- track ID
- contributing detection IDs
- estimated position
- estimated velocity when available
- estimated heading when available
- track age
- last observation time
- covariance or equivalent uncertainty representation
- track quality/state
- association confidence

Recommended lifecycle:

`tentative → confirmed → degraded → stale → terminated`

Track association must remain probabilistic when identity cannot be established confidently.

## 9. World Entity Model

A World Entity is the SWM representation of a physical or logical object.

Examples:

- person
- vehicle
- building
- door
- tree
- road segment
- landmark
- device
- unknown object

Conceptual structure:

```text
WorldEntity
├── entity_id
├── entity_type
├── geometry
├── kinematics
├── attributes
├── state
├── provenance
├── confidence
├── uncertainty
└── timestamps
```

The entity may be linked to one or more tracks and detections. Those links must not imply certainty of physical identity unless the evidence supports it.

### 9.1 Identity levels

The model explicitly distinguishes:

`Detection → Track → World Entity`

For example:

`detection-481 → track-72 → entity-184`

The links carry confidence and provenance.

## 10. Areas and Zones

Areas represent meaningful spatial regions.

Examples:

- property
- building
- room
- parking lot
- road
- path
- restricted zone
- monitoring zone
- geographic region

An area has a geometry, semantic type, provenance, validity period, and optional access/security classification.

The model must support containment and transition events such as:

- entity entered area
- entity exited area
- entity is inside area
- entity is near boundary

## 11. Relationships

Relationships are explicit records rather than hidden fields scattered through entities.

Examples:

- `A inside B`
- `A near B`
- `A observed_by camera-7`
- `A moving_toward B`
- `A associated_with track-91`
- `A connected_to B`

Each relationship should include:

- subject
- predicate
- object/target
- validity interval
- evidence/provenance
- confidence
- uncertainty where applicable

Derived relationships must be distinguishable from source-declared relationships.

## 12. Target Model

A Target is a spatial objective selected by a user, application, or authorized system component.

Target types may include:

- coordinate
- waypoint
- point of interest
- landmark
- detected object
- world entity
- area
- user-defined spatial objective

A target may expose:

- current location
- distance
- bearing
- direction
- elevation/relative height when known
- selection confidence
- target validity
- relevant contextual relationships

Targets may follow a moving world entity as its estimated position changes.

**Safety boundary:** the target model is spatial and informational. It must not contain weapon firing solutions, ballistic calculations, automated weapon selection, weapon locking, trigger control, or actuator-control interfaces.

## 13. Camera and Sensor Model

Every spatially relevant sensor must have a stable identity and calibration state.

A camera model should include:

- sensor ID
- physical location estimate
- orientation estimate
- field of view
- focal characteristics when available
- distortion/calibration parameters
- mounting orientation
- calibration timestamp
- calibration uncertainty
- health state
- data quality

If a camera moves or its calibration becomes invalid, dependent spatial estimates must be marked stale or invalid rather than silently reused.

The same pattern applies to GPS, IMU, compass, depth sensors, and future sensor types.

## 14. Environment Model

Environmental understanding is represented in layers.

### 14.1 Physical facts

Examples: wall geometry, terrain elevation, road location.

### 14.2 Observed conditions

Examples: wall is partially obscured, road is wet according to an observation, camera view is blocked.

### 14.3 Derived characteristics

Examples: estimated visibility obstruction, likely traversable surface, estimated line-of-sight condition.

### 14.4 AI interpretation

Examples: an AI explanation of what an environmental condition may imply.

The system must not promote an AI interpretation into an objective environmental fact without supporting evidence and explicit provenance.

## 15. Current World State

The current world state is a queryable projection of the latest valid beliefs.

For each modeled property, VIGIL should be able to answer:

- current value
- confidence
- uncertainty
- source/evidence
- last update time
- validity/staleness
- derivation method

A state value must never conceal whether it is directly observed, inferred, fused, simulated, or AI-interpreted.

## 16. World History and Events

World history retains observations, detections, track transitions, entity changes, area transitions, target changes, alerts, and other meaningful events according to retention policy.

Events should be append-oriented and reference the evidence and state they affected.

The history system enables questions such as:

- What did VIGIL believe at time T?
- What evidence caused the belief to change?
- When did an entity enter an area?
- Which camera observations contributed to a track?
- When did sensor health degrade?

## 17. Uncertainty and Confidence

Confidence and uncertainty are not interchangeable.

- **Confidence** expresses how strongly VIGIL supports a classification, association, or conclusion.
- **Uncertainty** expresses the possible error or range of an estimated quantity.

Spatial estimates should prefer explicit covariance, error bounds, or typed uncertainty representations over arbitrary decimal precision.

Unknown states should be represented explicitly, for example:

`unknown`, `not_observed`, `stale`, `invalid`, `insufficient_evidence`, `calibration_uncertain`.

## 18. Sensor Health and Data Quality

Sensor state shall be represented independently from the sensor's last numerical value.

Recommended states:

- healthy
- degraded
- stale
- unavailable
- invalid
- calibration_uncertain
- timestamp_uncertain

Quality metadata should be propagated into downstream estimates so a low-quality source cannot appear equivalent to a high-quality source.

## 19. Spatial Services

The SWM exposes reusable spatial operations rather than duplicating them in application modes.

Required services include:

- distance calculation
- bearing calculation
- relative direction
- coordinate transformation
- proximity queries
- point-in-area tests
- geometry intersection
- nearest-entity queries
- line-of-sight queries when sufficient geometry/evidence exists
- route/path queries through an adapter
- target tracking
- spatial filtering by time and confidence

Spatial services must return uncertainty and validity information where appropriate.

## 20. World Model Lifecycle

The recommended update pipeline is:

`Observation → Detection → Track → Entity Belief → Relationship/Area Updates → Current State Projection → Events`

Rules:

1. Preserve source evidence before deriving state.
2. Validate timestamps and coordinate frames.
3. Reject or quarantine invalid sensor data.
4. Associate detections with tracks using explicit confidence.
5. Update entity beliefs without erasing provenance.
6. Recalculate dependent relationships when relevant state changes.
7. Emit state-change events.
8. Mark stale data as stale rather than deleting its history.
9. Allow later evidence to revise a belief while preserving the previous belief in history.

## 21. Conceptual Interfaces

The first implementation should expose interfaces approximately equivalent to:

```text
ObservationStore
DetectionStore
TrackStore
WorldEntityStore
AreaStore
RelationshipStore
TargetStore
WorldHistoryStore
SensorRegistry
CoordinateTransformService
SpatialQueryService
WorldModelUpdater
EventPublisher
```

These are conceptual contracts, not a commitment to a particular programming language, database, or distributed architecture.

## 22. Persistence Strategy

The implementation should separate:

1. immutable source/evidence records
2. derived perception records
3. current world-state projections
4. historical events/state transitions
5. large binary payloads such as images or video

The initial implementation should favor a simple local persistence layer with clear interfaces over premature distributed infrastructure.

The model must support replaying recorded observations through the spatial pipeline for testing and debugging.

## 23. Security, Authorization, and Audit

Security is part of every SWM boundary.

The implementation must provide architectural hooks for:

- authenticated data sources
- authorization checks
- data classification
- retention policies
- audit records
- provenance verification
- access logging
- privacy-aware redaction
- tenant/project separation where required

A user or AI component must not gain access to a sensor, observation, historical record, or target merely because it can address the underlying object ID.

## 24. AI Boundary

VIGIL AI consumes SWM data and may produce analysis, explanations, correlations, summaries, recommendations, or simulation results.

AI output must identify:

- supporting evidence
- confidence
- uncertainty
- model/version when applicable
- whether the output is observation, inference, recommendation, or interpretation

AI must not silently rewrite authoritative world state or security permissions.

Preferred consequential-action chain:

`Observation → Evidence → Analysis → Confidence/Uncertainty → Proposed Action → Test/Simulation → Security Review → Human Approval → Execution → Verification → Audit`

## 25. Simulation and Test Contracts

The SWM must be testable without live hardware.

The simulator should be able to generate:

- static scenes
- moving entities
- multiple cameras
- GPS/IMU/compass streams
- sensor noise
- timestamp drift
- delayed/out-of-order observations
- camera movement
- calibration errors
- occlusion
- ambiguous detections
- entity entry/exit from areas
- target movement
- sensor failure

Core deterministic tests should verify:

- coordinate transformations
- distance and bearing
- temporal ordering
- stale-data handling
- uncertainty propagation
- track lifecycle
- entity association
- area transitions
- relationship validity
- target following
- replay determinism

## 26. Example Scenario

A camera observes a vehicle.

1. The camera produces observation `obs-1001`.
2. Perception creates detection `det-481` with vehicle classification confidence.
3. The tracker associates `det-481` with `track-72`.
4. Spatial fusion estimates the vehicle at a world coordinate with an uncertainty bound.
5. The SWM associates `track-72` with `entity-184` with an explicit association confidence.
6. The entity is determined to be inside `area-12` based on geometry.
7. A user selects `entity-184` as a spatial target.
8. The target service reports its current position, distance, bearing, and validity.
9. A later observation moves the estimated position; the target updates because it references the world entity rather than a stale screen coordinate.
10. If the camera becomes uncalibrated, the spatial estimate becomes degraded/invalid and the target reports reduced validity rather than silently presenting the old position as current.

At every step, the system can trace the current belief back through tracks, detections, observations, and sensor state.

## 27. Non-Goals and Safety Boundaries

The Spatial World Model does **not** implement:

- weapon firing control
- ballistic firing solutions
- automated weapon selection or engagement
- trigger or actuator control
- covert surveillance mechanisms
- bypassing access controls
- autonomous consequential actions without an authorized control layer

Those boundaries do not prevent VIGIL from providing generic spatial targeting, navigation, object selection, situational awareness, authorized security monitoring, or simulation.

## 28. Open Design Decisions

The following decisions should be finalized before production implementation:

1. Exact geodetic reference and altitude datum.
2. Exact local-world coordinate convention and axis orientation.
3. Typed representation for covariance/error bounds.
4. Persistence technology for the initial implementation.
5. Event serialization format.
6. Language/module boundaries for the Spatial Core.
7. Retention policies for raw observations and media.
8. Identity-association policy and thresholds.
9. Authorization model and data classifications.
10. Replay file format for simulator/test fixtures.
11. API versioning strategy.
12. Rules for conflict resolution between sensors and stale beliefs.

## 29. Implementation Order

Implementation should proceed in this order:

1. Coordinate and unit primitives.
2. Time and uncertainty primitives.
3. Observation and provenance types.
4. Detection and track types.
5. World entity and area types.
6. Relationships and target types.
7. Sensor registry and calibration state.
8. Spatial query services.
9. World-model update engine.
10. Event/history layer.
11. Local persistence and replay.
12. Deterministic simulator.
13. Automated test suite.
14. Application-mode adapters.

No UI should become the authoritative owner of spatial state.

## 30. Definition of Done for the Spatial Core

The Spatial Core is ready for its first application integration when it can:

- represent observations, detections, tracks, entities, areas, relationships, and targets;
- transform coordinates deterministically;
- represent time, confidence, uncertainty, and sensor health;
- maintain current state and historical events separately;
- explain the provenance of derived state;
- handle stale and invalid inputs safely;
- replay recorded observations deterministically;
- support generic spatial targeting without weapon-control logic;
- operate entirely locally in a test environment; and
- pass an automated simulator-backed test suite.

---

**Next implementation artifact:** `Spatial Core` library and its simulator-backed automated tests.
