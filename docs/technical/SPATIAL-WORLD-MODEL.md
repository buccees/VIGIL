# VIGIL Spatial World Model — Technical Design

**Document:** Spatial World Model Technical Design  
**Version:** 0.3  
**Status:** Proposed for implementation review  
**Parent:** VIGIL System Architecture

## 1. Purpose and Scope

The Spatial World Model (SWM) is VIGIL's authoritative spatial state layer. It represents what VIGIL currently believes about the physical or simulated environment, the evidence supporting those beliefs, and the uncertainty associated with them.

The SWM does not directly control sensors, weapons, vehicles, or other consequential actuators. It provides shared spatial state to navigation, security monitoring, generic spatial targeting, search, inspection, environmental-awareness, and training/simulation applications.

The model SHALL support both offline/local operation and connected deployments.

## 2. Design Principles

1. **Evidence before belief.** Source observations SHALL be preserved so derived state can be explained and reconstructed within applicable retention policy.
2. **Facts and interpretations remain separate.** A sensor report, a perception result, a fused world belief, and an AI interpretation are distinct information types.
3. **Unknown is a valid value.** Missing, stale, ambiguous, or uncalibrated information SHALL NOT silently become zero, false, or a guess.
4. **Time is first-class.** Every observation and relevant state transition SHALL have an explicit time model.
5. **Uncertainty is first-class.** Spatial estimates and identity associations SHALL carry uncertainty or an explicit unknown state rather than unsupported precision.
6. **Identity is layered.** Detection, track, and world-entity identity SHALL remain distinct.
7. **The world model is the authoritative source of VIGIL's supported spatial state.** Navigation and other applications SHALL consume that authoritative state and SHALL NOT create competing spatial authorities. Authoritative status SHALL mean authority within VIGIL's state model; it SHALL NOT imply objective certainty about the physical environment.
8. **History is retained separately from current state.** Current state answers what is currently believed; history answers what happened and why, subject to retention policy.
9. **Security follows the data.** Access, provenance, authorization, audit, and retention SHALL apply throughout the data lifecycle.
10. **Deterministic core, replaceable perception.** Deterministic spatial calculations SHALL be testable without requiring an AI model or live hardware.

## 3. Terminology

| Term | Meaning |
|---|---|
| Observation | A source-reported measurement or event. |
| Evidence | An observation or artifact retained to support a conclusion. |
| Detection | A perception result identifying a possible object or feature in an observation. |
| Track | A temporal association of detections believed to represent the same physical entity under the available evidence. |
| World Entity | A model-level representation of a physical or logical entity. |
| Area | A spatial region with a defined boundary or semantic purpose. |
| Relationship | A derived or declared spatial/semantic relation between entities. |
| Target | A selected spatial objective represented independently of any weapon or actuator. |
| Belief | VIGIL's current modelled estimate of a property or entity. |
| Event | A meaningful state change or occurrence recorded in world history. |

## 4. Coordinate Systems and Units

VIGIL SHALL explicitly distinguish coordinate frames.

### 4.1 Required frames

- **Global:** latitude, longitude, altitude using a documented geodetic reference system.
- **World:** local Cartesian coordinates used for spatial computation within an operating area.
- **Sensor:** coordinates relative to a camera, IMU, GPS receiver, or other sensor.
- **Device:** coordinates relative to the host device/user platform.
- **Display:** coordinates used by a UI, AR overlay, VR scene, or other presentation system.

Transform chains SHALL be explicit:

`World ↔ Sensor ↔ Device ↔ Display`

Global-to-world transformation SHALL also be explicit and versioned.

### 4.2 Units

- Distance: meters.
- Speed: meters/second.
- Acceleration: meters/second².
- Angles: degrees at API boundaries unless a component explicitly documents radians internally.
- Time duration: milliseconds or a typed duration representation.
- Latitude/longitude: decimal degrees.
- Altitude: meters with a documented vertical datum.

No API SHALL rely on an undocumented unit convention.

## 5. Time Model

Every observation SHALL carry, where the source or implementation can provide the value:

- `event_time`: when the source says the event occurred.
- `ingest_time`: when VIGIL received it.
- `source_clock`: source clock identifier or synchronization state when available.
- `time_uncertainty`: estimated timestamp uncertainty when available.

The model SHALL tolerate clock drift, delayed delivery, out-of-order observations, and unavailable source time.

State updates SHALL NOT assume ingestion order equals physical-event order.

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

Raw payload storage MAY be external to the SWM, but the observation SHALL retain a stable reference to its source evidence when policy permits.

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

A detection SHALL NOT by itself establish persistent identity or authoritative World Model state.

## 8. Track Model

A track associates detections over time.

A track SHALL contain:

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

The lifecycle is an implementation recommendation until the tracking contract defines the authoritative state machine.

Track association SHALL remain probabilistic or explicitly uncertain when identity cannot be established confidently.

Track maintenance SHALL own temporal continuity and SHALL NOT directly mutate authoritative World Model state.

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

The entity may be linked to one or more tracks and detections. Those links SHALL NOT imply certainty of physical identity unless the evidence supports it.

### 9.1 Identity levels

The model explicitly distinguishes:

`Detection → Track → World Entity`

For example:

`detection-481 → track-72 → entity-184`

The links SHALL carry confidence and provenance sufficient to determine the basis for the association. Stable association SHALL NOT by itself establish physical identity.

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

The model SHALL support containment and transition events such as:

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

Each relationship SHALL include, as applicable:

- subject
- predicate
- object/target
- validity interval
- evidence/provenance
- confidence
- uncertainty where applicable

Derived relationships SHALL be distinguishable from source-declared relationships.

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

**Safety boundary:** the target model is spatial and informational. It SHALL NOT contain weapon firing solutions, ballistic calculations, automated weapon selection, weapon locking, trigger control, or actuator-control interfaces.

## 13. Camera and Sensor Model

Every spatially relevant sensor SHALL have a stable identity and calibration state.

A camera model may include:

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

If a camera moves or its calibration becomes invalid, dependent spatial estimates SHALL be marked stale or invalid rather than silently reused.

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

The system SHALL NOT promote an AI interpretation into an objective environmental fact without supporting evidence and explicit provenance.

## 15. Current World State

The current world state is a queryable projection of the latest valid beliefs.

For each modeled property, VIGIL SHALL be able to represent or retrieve, where applicable:

- current value
- confidence
- uncertainty
- source/evidence
- last update time
- validity/staleness
- derivation method

A state value SHALL NOT conceal whether it is directly observed, inferred, fused, simulated, or AI-interpreted.

## 16. World History and Events

World history retains observations, detections, track transitions, entity changes, area transitions, target changes, alerts, and other meaningful events according to retention policy.

Events SHALL be append-oriented and SHALL reference the evidence and state they affected where those references are available and permitted.

The history system SHALL support questions such as:

- What did VIGIL believe at time T?
- What evidence caused the belief to change?
- When did an entity enter an area?
- Which camera observations contributed to a track?
- When did sensor health degrade?

## 17. Uncertainty and Confidence

Confidence and uncertainty are not interchangeable.

- **Confidence** expresses how strongly VIGIL supports a classification, association, or conclusion.
- **Uncertainty** expresses the possible error or range of an estimated quantity.

Spatial estimates SHALL prefer explicit covariance, error bounds, or typed uncertainty representations over arbitrary decimal precision.

Unknown states SHALL be represented explicitly, for example:

`unknown`, `not_observed`, `stale`, `invalid`, `insufficient_evidence`, `calibration_uncertain`.

## 18. Sensor Health and Data Quality

Sensor state SHALL be represented independently from the sensor's last numerical value.

Recommended states:

- healthy
- degraded
- stale
- unavailable
- invalid
- calibration_uncertain
- timestamp_uncertain

The state vocabulary is provisional until the applicable sensor contract defines the authoritative enumeration.

Quality metadata SHALL be propagated into downstream estimates so a low-quality source cannot appear equivalent to a high-quality source.

## 19. Spatial Services

The SWM SHALL expose reusable spatial operations rather than duplicating them in application modes.

Services include:

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

Spatial services SHALL return uncertainty and validity information where appropriate.

## 20. World Model Lifecycle

The authoritative information lifecycle SHALL be:

`Observation → Detection → Track → Spatial/Temporal Fusion → Entity Belief → Relationship/Area Updates → Current State Projection → Events`

Rules:

1. Source evidence SHALL be preserved before deriving dependent state, subject to retention policy.
2. Timestamps and coordinate frames SHALL be validated before they are used to derive authoritative spatial state.
3. Invalid sensor data SHALL be rejected or quarantined according to the applicable input contract.
4. Detections SHALL be associated with tracks using explicit confidence or uncertainty.
5. Spatial/temporal fusion SHALL operate on validated observations, detections, tracks, or other authorized evidence according to the applicable fusion contract.
6. Entity beliefs SHALL be updated from validated fusion output without erasing provenance.
7. Dependent relationships SHALL be recalculated when relevant authoritative state changes.
8. State-change events SHALL be emitted only after the corresponding authoritative state mutation succeeds.
9. Stale data SHALL be marked stale rather than deleting its history.
10. Later evidence MAY revise a belief while the previous belief remains available in history according to retention policy.
11. Track- or fusion-derived authoritative state SHALL cross the Track → World Model boundary only through `WorldModelUpdater`.

## 21. Conceptual Interfaces

The first implementation MAY expose interfaces equivalent in responsibility to the following conceptual components:

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

The names and programming-language representations of these components are implementation-defined. Their responsibilities SHALL remain consistent with the applicable sections of this technical design and higher-level contracts.

## 22. Persistence Strategy

The implementation SHALL separate, logically or through explicitly traceable storage boundaries:

1. immutable source/evidence records
2. derived perception records
3. current world-state projections
4. historical events/state transitions
5. large binary payloads such as images or video

The initial implementation SHOULD favor a simple local persistence layer with clear interfaces over premature distributed infrastructure.

The model SHALL support replaying recorded observations through the spatial pipeline for testing and debugging.

## 23. Security, Authorization, and Audit

Security is part of every SWM boundary.

The implementation SHALL provide architectural hooks for:

- authenticated data sources
- authorization checks
- data classification
- retention policies
- audit records
- provenance verification
- access logging
- privacy-aware redaction
- tenant/project separation where required

A user or AI component SHALL NOT gain access to a sensor, observation, historical record, or target merely because it can address the underlying object ID.

Authentication SHALL remain distinct from authorization.

## 24. AI Boundary

VIGIL AI consumes SWM data and may produce analysis, explanations, correlations, summaries, recommendations, or simulation results.

AI output SHALL identify, where applicable:

- supporting evidence
- confidence
- uncertainty
- model/version when applicable
- whether the output is observation, inference, recommendation, or interpretation

AI SHALL NOT silently rewrite authoritative world state or security permissions.

The consequential-action chain SHALL preserve the applicable authorization and human-decision boundaries:

`Observation → Evidence → Analysis → Confidence/Uncertainty → Proposed Action → Test/Simulation → Security Review → Human Approval → Execution → Verification → Audit`

The SWM SHALL NOT itself execute consequential physical actions.

## 25. Simulation and Test Contracts

The SWM SHALL be testable without live hardware.

The simulator SHOULD be able to generate:

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

Core deterministic tests SHALL verify, as applicable:

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
3. The tracker associates `det-481` with `track-72` with an explicit association state.
4. Spatial/temporal fusion estimates the vehicle at a world coordinate with an uncertainty bound.
5. The controlled `WorldModelUpdater` validates the derived state and the SWM associates `track-72` with `entity-184` with an association confidence.
6. The entity is determined to be inside `area-12` using the authoritative spatial state.
7. The user selects `entity-184` as a target.
8. The target reports its current position, distance, bearing, and validity state.
9. A later observation updates the fused estimate and the target follows the entity.
10. The camera becomes uncalibrated. The dependent spatial estimate becomes degraded or invalid, and the target's validity is reduced. The previous position is not presented as current merely because it remains in history.

This scenario demonstrates the traceability chain from sensor observation through perception, tracking, fusion, World Model update, spatial services, and user presentation.

## 27. Non-Goals and Safety Boundaries

The Spatial World Model does not implement:

- weapon firing control;
- ballistic firing solutions;
- automated weapon selection or engagement;
- trigger or actuator control;
- covert surveillance mechanisms;
- bypassing access controls; or
- autonomous consequential actions without an authorized control layer.

Generic spatial targeting, navigation, object selection, situational awareness, authorized security monitoring, and simulation remain within scope.

## 28. Open Design Decisions

The following decisions SHALL be finalized before production implementation depends on them:

1. exact geodetic reference system and altitude datum;
2. exact local-world coordinate convention and axis orientation;
3. covariance/error-bound representation and serialization;
4. persistence technology;
5. event serialization format;
6. programming-language and module boundaries;
7. retention policies for raw observations and media;
8. identity-association policies and thresholds;
9. authorization model and data classifications;
10. replay format;
11. API versioning strategy; and
12. conflict-resolution rules between sensors and stale beliefs.

Until these decisions are finalized, implementations SHALL NOT silently assume a value that materially changes semantic behavior.

## 29. Implementation Order

The initial implementation should proceed in the following order:

1. coordinate and unit primitives;
2. time and uncertainty primitives;
3. observation and provenance types;
4. detection and track types;
5. spatial/temporal fusion interfaces and deterministic fusion primitives;
6. world entity and area types;
7. relationship and target types;
8. sensor registry and calibration state;
9. spatial query services;
10. world-model update engine and controlled `WorldModelUpdater` boundary;
11. event/history layer;
12. local persistence and replay;
13. deterministic simulator;
14. automated test suite; and
15. application-mode adapters.

The implementation SHALL preserve the lifecycle and authority order defined in Section 20. In particular, Track Continuity SHALL precede Spatial/Temporal Fusion, and Spatial/Temporal Fusion SHALL precede authoritative World Model mutation through `WorldModelUpdater`. No implementation step SHALL bypass or collapse those authority boundaries merely because the underlying data structures are implemented in the same library or process.

## 30. Definition of Done for the Spatial Core

The Spatial Core is ready for first application integration only when it can:

- represent observations, detections, tracks, world entities, areas, relationships, and targets;
- perform deterministic coordinate transformations;
- represent time, confidence, uncertainty, and sensor health;
- maintain current state and historical events separately;
- preserve provenance for derived state;
- handle stale and invalid inputs safely;
- replay recorded inputs deterministically where required;
- support generic spatial targeting without weapon-control functionality;
- operate in a local test environment; and
- pass an automated simulator-backed test suite covering the defined contracts.

No UI component SHALL become the authoritative owner of spatial state.

The next implementation artifact should be the Spatial Core library with simulator-backed automated tests.
