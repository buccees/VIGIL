# VIGIL Architecture Specification

**Project:** VIGIL  
**Full name:** Visual Intelligence & Geographic Information Layer  
**Document version:** 0.3  
**Status:** Architecture approved for technical design  
**Audience:** Project developers, reviewers, maintainers, and future contributors

---

## 1. Purpose

VIGIL is an AI-powered environmental awareness and security platform.

Its purpose is to combine information from multiple authorized sources and turn that information into a coherent, evidence-grounded representation of an environment.

Possible information sources include:

- Security and IP cameras
- Phone and wearable cameras
- GPS and other location systems
- Compass and orientation sensors
- IMU sensors
- Maps and geographic databases
- Other authorized sensors and data feeds

VIGIL represents objects, locations, movement, events, environmental characteristics, and changes over time while preserving the distinction between source observations, derived perception, temporal continuity, fused evidence, authoritative world state, and AI interpretation.

The architecture supports applications such as security monitoring, navigation, generic spatial targeting, search, inspection, environmental awareness, and simulation.

The architecture remains independent of any particular camera, phone, AR headset, vehicle, or other hardware.

---

## 2. Architectural Goals

VIGIL SHALL be:

### Hardware independent

The core system SHALL NOT require a particular camera, GPS receiver, phone, computer, wearable display, or other physical device.

### Sensor independent

Sensor-specific implementations SHALL remain replaceable behind defined interfaces without requiring unrelated core layers to depend on a particular sensor implementation.

### Spatially aware

VIGIL SHALL maintain a shared representation of supported environmental entities, spatial relationships, areas, and locations in the Spatial World Model.

### Time aware

VIGIL SHALL represent relevant event time, ingestion time, freshness, and ordering semantics so changes in the environment can be evaluated through time.

### Uncertainty aware

VIGIL SHALL preserve confidence, uncertainty, validity, data quality, and unresolved states where applicable rather than representing unsupported precision or certainty.

### AI compatible

AI SHALL reason over structured information and SHALL NOT own deterministic geometry, authoritative spatial state, or sensor plumbing that belongs to other architectural layers.

### Extensible

New sensors, perception models, displays, application modes, and AI models SHALL be addable through defined interfaces without changing unrelated architectural responsibilities.

### Secure

Access to cameras, stored data, alerts, system capabilities, and other protected resources SHALL be controlled and auditable according to applicable security contracts.

### Testable

Core functionality SHALL be testable without physical hardware through simulation, recorded data, deterministic services, and automated verification where applicable.

---

## 3. Core Architectural Principle

The central rule in VIGIL is:

> **Observation is not the same thing as understanding.**

A camera may observe an object. A perception system may classify it. Tracking may establish temporal continuity among detections. Spatial/temporal fusion may combine compatible evidence and estimate spatial state. The Spatial World Model may represent the resulting supported belief. AI may then analyze that structured state and evidence in context.

These responsibilities SHALL remain separate.

The canonical processing flow is:

**Authorized Source → Observation → Detection/Perception → Track Continuity → Spatial/Temporal Fusion → Spatial World Model → Spatial/Environmental Services → Relevance/Priority → Presentation/Attention → Human User → Human Decision/Action**

Supporting services such as event processing, security, logging, evidence/history, sensor health, and simulation operate within their defined boundaries and SHALL NOT create competing authority over the Spatial World Model.

---

## 4. High-Level Architecture

VIGIL is organized into the following major layers and supporting services.

### Layer 1: Sensors and Data Sources

Provides raw information to VIGIL from authorized sources.

Examples:

- Cameras
- GPS
- Compass
- IMU
- Maps
- Geographic databases
- Other authorized sensors

This layer does not determine the authoritative meaning of the information.

### Layer 2: Observation

Converts authorized source input into timestamped observations.

An observation may state that a camera captured a frame at a particular time, that a GPS receiver reported a position, or that an IMU reported an orientation.

Observations preserve their source identity, event timestamp, ingestion timestamp where applicable, quality, clock information, uncertainty, and provenance.

### Layer 3: Perception

Derives detections or other perception results from observations.

Examples:

- Object detected
- Vehicle detected
- Sign detected
- Building feature detected
- Person detected where the deployment is authorized to process people
- Scene changed
- Camera view became obstructed

Perception produces evidence and detections. It does not own authoritative spatial world state and does not establish persistent identity merely by producing a detection.

### Layer 4: Track Continuity

Maintains temporal associations among detections.

A track represents a time-linked association that may refer to the same physical entity. Track identity SHALL remain distinct from detection identity and World Entity identity.

Track association SHALL preserve uncertainty and SHALL NOT be treated as confirmed physical identity when the evidence is insufficient.

Track management SHALL NOT directly mutate authoritative World Model state.

### Layer 5: Spatial and Temporal Fusion

Combines compatible observations, detections, track information, sensor pose, calibration, timing, and other authorized evidence under the applicable fusion policy.

Fusion SHALL preserve provenance, timing, confidence, uncertainty, freshness, validity, and material disagreement.

Fusion SHALL NOT force a single answer when evidence remains materially conflicting or insufficient.

Fusion SHALL NOT directly mutate authoritative World Model state. Fusion output crosses into authoritative state only through the controlled WorldModelUpdater boundary.

### Layer 6: Spatial World Model

The Spatial World Model is VIGIL's authoritative representation of its current supported belief about the environment.

It contains structured information about:

- World entities
- Locations and geometry
- Areas and zones
- Generic spatial targets
- Cameras and sensors
- Routes and route relationships
- Points of interest
- Environmental features
- Relationships
- Current world state
- Evidence and observations
- World history
- Confidence and uncertainty
- Validity and freshness

The World Model represents current belief supported by available evidence. It does not assert objective certainty for every value.

### Layer 7: Spatial and Environmental Services

Deterministic services operate on authoritative World Model state or explicitly supplied validated inputs.

Examples:

- Distance calculation
- Bearing calculation
- Relative direction
- Elevation relationships
- Coordinate conversion
- Object proximity
- Movement and velocity relationships
- Route relationships
- Visibility relationships where sufficient data exists
- Collision and proximity analysis

Where applicable, these services SHALL return validity and uncertainty information with their results.

They SHALL NOT become a competing source of authoritative environmental state.

### Layer 8: Relevance, Priority, and Presentation/Attention

Relevance describes usefulness to the active user or application context. Priority determines which valid information receives attention first.

Priority SHALL NOT determine whether an entity exists in authoritative World Model state.

Presentation SHALL manage human attention without becoming the source of environmental truth. Reduced presentation salience SHALL NOT delete authoritative state.

### Layer 9: VIGIL AI

The AI reasons over structured information produced by the lower layers.

AI MAY:

- combine information from multiple sources;
- explain observations and derived relationships;
- compare current and previous states;
- assess confidence and identify uncertainty;
- summarize events and world state;
- answer questions about available information;
- explain evidence supporting a conclusion; and
- provide bounded recommendations within an authorized application context.

AI SHALL NOT:

- become the authoritative source of physical or environmental truth;
- replace deterministic spatial calculations with unsupported interpretation where deterministic information is available;
- silently rewrite authoritative World Model state;
- bypass authorization; or
- acquire physical-action authority through language, presentation, or conversational context.

### Layer 10: Human Interaction

Text and optional voice interfaces provide a bidirectional information channel between the human user and authorized VIGIL software functions.

Human interaction SHALL NOT create physical-action authority.

Authentication, authorization, session identity, speech recognition, and conversational context SHALL remain distinct concerns.

Conversational context SHALL NOT silently expand authorization.

### Layer 11: Application Modes

Application modes determine operational context and the relevance, priority, and presentation of available information.

Initial modes include:

- Security monitoring
- Navigation
- Generic spatial targeting
- Search
- Inspection
- Environmental awareness
- Training and simulation

Generic spatial targeting allows a user or authorized application to select, locate, follow, or present information about a spatial objective such as a destination, waypoint, point of interest, landmark, detected object, world entity, area, or user-defined location.

Generic spatial targeting SHALL remain independent of weapon control or consequential physical-action functionality. Weapon-control interfaces, firing solutions, ballistic calculations, and automated weapon engagement are outside VIGIL's architecture.

Application modes SHALL NOT create competing authority over the Spatial World Model.

---

## 5. Canonical Data Flow

A typical camera input SHALL follow the architectural lifecycle below. Implementations may batch or internally decompose steps, but SHALL preserve the defined responsibility boundaries and ordering semantics.

1. An authorized camera source provides a frame or measurement.
2. The sensor adapter records the source identity and applicable timestamps and quality metadata.
3. The observation layer creates an observation record.
4. The perception layer analyzes the observation and creates one or more detections or other perception results.
5. The tracking layer evaluates temporal continuity and creates or updates a track association with explicit confidence and uncertainty.
6. The spatial/temporal fusion layer combines compatible evidence, including sensor pose, calibration, timing, and track information where applicable.
7. The controlled WorldModelUpdater validates eligible derived state and updates the Spatial World Model.
8. Spatial/environmental services calculate deterministic relationships from authoritative state or explicitly validated inputs.
9. The event system records meaningful changes after authoritative state mutation.
10. Relevance and priority determine which valid information receives attention in the active application context.
11. Presentation and human-interaction layers present information or accept authorized software-level requests.
12. AI may receive structured state, evidence, history, and authorized conversational context for bounded analysis or explanation.
13. AI output SHALL preserve applicable confidence, uncertainty, provenance, validity, and semantic qualification.

The same lifecycle principles apply to GPS, IMU, maps, and other authorized sources.

---

## 6. Spatial World Model

The Spatial World Model is the central authoritative architectural component of VIGIL.

It represents the environment independently of how information was obtained while preserving evidence and derivation.

### 6.1 Current World State and World History

VIGIL SHALL distinguish between:

**Current World State** — the currently supported modelled belief about the environment.

**World History** — observations, detections, tracks, state changes, and events that explain how the current state developed.

History SHALL preserve sufficient provenance for the system to determine why a current belief exists when the required evidence remains available under retention policy.

### 6.2 User State

When VIGIL operates with a mobile user, the world model MAY contain:

- Position
- Altitude
- Heading
- Pitch
- Roll
- Timestamp
- Position uncertainty
- Orientation uncertainty
- Pose source and quality

When present, these values SHALL retain their source, validity, uncertainty, and time semantics.

### 6.3 Objects and World Entities

Objects SHALL be represented as generic spatial entities.

An object may represent:

- Vehicle
- Person, when authorized
- Sign
- Building feature
- Equipment
- Landmark
- Obstacle
- Detected item
- Custom-defined object
- Unknown object

A world entity may contain:

- Unique identifier
- Type and subtype
- Position or geometry
- Dimensions when known
- Derived distance when applicable
- Derived bearing when applicable
- Direction of travel when known
- Velocity when known
- Confidence
- Uncertainty
- Tracking state
- Observation sources
- First observed time
- Last observed time
- Validity/freshness
- Provenance
- Metadata

### 6.4 Detection, Track, and Entity Identity

VIGIL SHALL distinguish among:

- **Observation:** what an authorized source reported
- **Detection:** what perception derived from an observation
- **Track:** a temporal association among detections
- **World Entity:** the model-level entity represented in the current world model

The identity domains SHALL remain separate:

`Detection ID ≠ Track ID ≠ World Entity ID`

Associations SHALL be explicit, traceable, and qualified by confidence or other applicable evidence. An internally stable association SHALL NOT by itself establish physical identity.

### 6.5 Targets

The term **target** is generic in VIGIL.

A target can be:

- A destination
- A waypoint
- A point of interest
- A landmark
- A detected object
- A world entity
- A user-defined location
- Another spatial objective

A target is a selected spatial objective, not necessarily a distinct physical object. Target state SHALL reference the underlying world entity or location when applicable.

Target handling SHALL remain independent of any weapon or weapon-control system.

### 6.6 Areas and Zones

The world model SHALL support spatial regions as well as points and objects.

Examples include:

- Buildings
- Rooms
- Properties
- Roads
- Paths
- Parking areas
- Monitoring zones
- Restricted areas where authorized data is available
- Geographic regions

Objects and events MAY have relationships to areas, including entering, leaving, being inside, or being near a zone.

### 6.7 Relationships

VIGIL SHALL support explicit relationships among world entities.

Examples include:

- Observed-by
- Associated-with
- Inside
- Near
- Moving-toward
- Located-in
- Connected-to
- Related-to

Relationships SHALL preserve confidence and provenance where the relationship is inferred rather than directly established.

Derived relationships SHALL remain distinguishable from source-declared relationships.

### 6.8 Environment

The environment model represents persistent or semi-persistent environmental characteristics and their supporting evidence.

Examples include:

- Buildings
- Entrances and exits
- Roads
- Paths
- Terrain
- Obstacles
- Open areas
- Covered areas
- Accessibility information
- Visibility information
- Restricted areas where authorized data is available
- Environmental hazards

The environment model SHALL distinguish objective or directly supported characteristics from derived characteristics and AI interpretation. Unsupported tactical assumptions SHALL NOT be promoted to environmental facts.

### 6.9 Unknown and Insufficient Evidence

VIGIL SHALL explicitly represent unknown or insufficiently known values.

Unknown SHALL NOT be represented as zero, empty, false, or an AI guess.

A conclusion SHALL remain distinguishable from a measured fact and from an unresolved value.

---

## 7. Cameras as Spatial Sensors

Security cameras are a major VIGIL use case.

A camera SHALL be treated as a spatial sensor when its information contributes to spatial reasoning.

Each camera may have metadata such as:

- Camera identifier
- Location when known
- Orientation when known
- Field of view when known
- Intrinsic calibration when available
- Lens/distortion parameters when available
- Mounting information when known
- Calibration status
- Calibration timestamp
- Calibration uncertainty
- Stream status
- Timestamp quality
- Authorized access information

VIGIL's architecture supports multiple cameras observing the same environment.

The system SHALL combine observations from different cameras only when the applicable spatial, temporal, authorization, and evidence requirements permit that combination.

A camera failure, stale feed, obstructed view, moved camera, or uncertain calibration SHALL be represented explicitly when it affects derived state.

---

## 8. Object Tracking

Detection answers:

> What was detected in an observation?

Tracking answers:

> Which detections are associated through time under the available evidence?

VIGIL tracking SHALL support:

- Persistent track identifiers
- Position history
- Movement history
- Velocity estimates where available
- Confidence
- Uncertainty
- Lost and reacquired states
- Multiple observation sources

Tracking SHALL preserve uncertainty. An uncertain association SHALL NOT be treated as confirmed identity.

Track state SHALL remain separate from authoritative World Model state and SHALL cross that boundary only through the controlled update mechanism.

---

## 9. Event System

VIGIL SHALL distinguish between continuous state and events.

For example:

**State:** A vehicle is present in an area.

**Event:** A vehicle entered the area.

Events SHALL contain enough information to reconstruct the relevant occurrence, including source, applicable timestamp, relevant objects, confidence, uncertainty, and supporting observations where available and permitted.

Events that represent authoritative World Model changes SHALL be emitted only after the corresponding state mutation succeeds.

---

## 10. AI Reasoning Architecture

The AI SHALL operate on structured context produced by VIGIL rather than directly owning sensor state or authoritative spatial state.

A preferred bounded reasoning sequence is:

**Observation → Evidence → Analysis → Confidence → Uncertainty → Proposed Action → Verification**

For consequential software operations, the applicable authorization and human-decision boundaries SHALL be enforced before execution.

The AI SHALL be able to communicate when available information is insufficient, unknown, stale, invalid, conflicting, or otherwise unresolved when that condition is material to the requested answer.

The AI SHALL distinguish, where materially relevant, among established information, source observation, perception/detection, inference, estimate, uncertainty, unknown/unavailable information, stale/invalid information, conflicting evidence, recommendation, and AI interpretation.

The AI SHALL identify the evidence and material assumptions supporting an important conclusion when that information is available.

The AI SHALL NOT silently change authoritative World Model state merely because it generated an interpretation.

---

## 11. Security Architecture

Because VIGIL may process security-camera feeds and other sensitive environmental information, security is an architectural requirement rather than a user-interface-only concern.

The architecture SHALL provide defined boundaries for:

- Authentication
- Authorization
- Per-camera access controls
- Role-based permissions where appropriate
- Secure communications
- Protected credentials
- Audit logging
- Data retention controls
- Privacy controls
- Configurable recording policies
- AI capability boundaries

Security controls SHALL apply throughout the data flow rather than only at the user-interface layer.

The system SHALL maintain an audit trail for significant automated decisions and administrative actions according to applicable retention and privacy requirements.

---

## 12. Sensor Health and Data Quality

VIGIL SHALL represent sensor and data-source health explicitly where it affects processing or interpretation.

Possible states include:

- Healthy
- Degraded
- Stale
- Unavailable
- Invalid
- Calibration uncertain
- Timestamp uncertain

Data quality SHALL be available to downstream fusion, tracking, AI reasoning, and presentation where relevant.

A missing or unreliable source SHALL NOT silently appear equivalent to a healthy source.

---

## 13. Time Model

Time is a first-class part of the architecture.

Observations and events SHALL preserve, where available:

- Source event timestamp
- System ingestion timestamp
- Source clock information
- Timestamp uncertainty

The system SHALL account for time differences among multiple cameras and sensors when associating observations or reconstructing events.

The architecture SHALL distinguish physical event/observation time from ingestion and processing time and SHALL tolerate delayed or out-of-order information without treating ingestion order as physical-event order.

---

## 14. Simulation

VIGIL SHALL remain useful before specialized hardware exists.

The simulator SHALL provide virtual versions of system inputs through the same or contract-equivalent interfaces used by real sensors wherever practical.

The simulator MAY model:

- GPS position
- Heading and orientation
- Camera observations
- Moving objects
- Static objects
- Maps
- Routes
- Environmental features
- Camera failures
- Sensor uncertainty
- Sensor timing problems
- Object appearance/disappearance
- Events

Simulation SHALL support verification of core behavior without physical hardware.

---

## 15. Hardware Abstraction

Hardware-specific implementation SHALL remain behind interfaces.

Conceptually, VIGIL supports abstractions such as:

**Pose Provider**

Provides position and orientation.

Possible implementations:

- Simulated pose provider
- GPS/IMU provider
- Visual odometry provider
- Future sensor-fusion provider

**Object Detector**

Provides perception results or detections.

Possible implementations:

- Simulated detector
- Recorded-data detector
- Camera-based detector
- Future AI perception model

**Display Provider**

Presents VIGIL information.

Possible implementations:

- Desktop
- Phone
- VR
- AR glasses
- Future visor

The core SHALL NOT require knowledge of which physical device is being used.

---

## 16. Repository Structure

The initial repository is organized around responsibilities rather than individual hardware products.

A planned structure is:

- `docs/` — project documentation
- `spatial-core/` — spatial world model, coordinate systems, uncertainty, and deterministic spatial services
- `perception/` — perception interfaces and implementations
- `tracking/` — object tracking and identity association
- `events/` — event processing
- `environment/` — environmental model
- `navigation/` — navigation and route functionality
- `security/` — authorization, auditing, and security infrastructure
- `ai/` — AI integration and reasoning interfaces
- `simulator/` — hardware-independent simulation
- `presentation/` — user interfaces and display adapters

The exact programming-language and build-system choices remain technical-design decisions and SHALL be documented before implementation depends on them.

---

## 17. Development Order

VIGIL SHALL be developed from authoritative data foundations outward.

### Phase 1 — Architecture

Define responsibilities, interfaces, data ownership, security boundaries, semantic communication requirements, and testing strategy.

### Phase 2 — Spatial Core

Implement the Spatial World Model, coordinate/reference-frame handling, uncertainty representation, time semantics, provenance, and deterministic spatial calculations.

### Phase 3 — Simulator

Create simulated users, objects, sensors, cameras, areas, movement, and sensor-quality conditions.

### Phase 4 — Tracking and Events

Add temporal continuity, identity association, and event generation while preserving the Track → World Model boundary.

### Phase 5 — Perception

Add camera and recorded-data perception pipelines.

### Phase 6 — AI Integration

Add bounded AI reasoning over the structured world model and evidence/history with explicit semantic and authorization boundaries.

### Phase 7 — Security Monitoring

Add authorized multi-camera monitoring, alerts, audit logging, and administration.

### Phase 8 — Navigation, Targeting, and Environmental Awareness

Add route relationships, generic target selection/following, environmental characteristics, search, inspection, and related modes.

### Phase 9 — Presentation and Human Interaction

Build desktop and mobile interfaces, text interaction, authorized optional voice interaction, and later VR/AR integrations.

### Phase 10 — Hardware Integration

Integrate specialized sensors and future wearable hardware after the software architecture and contracts are stable.

---

## 18. Testing Strategy

Every major subsystem SHALL be testable independently within its defined contract boundary.

Testing SHALL include, as applicable:

- Unit tests
- Spatial math tests
- Coordinate transformation tests
- Time synchronization and association tests
- Sensor simulation tests
- Tracking tests
- Identity-association tests
- Event tests
- AI input/output contract tests
- Evidence/provenance tests
- Security tests
- Authorization tests
- Semantic-clarity tests for customer-facing communication
- Integration tests
- End-to-end simulated scenarios

The simulator SHALL form a core regression-testing capability.

A change to deterministic core behavior SHALL be verifiable without physical hardware.

Failed verification attempts SHALL be documented according to the Architecture Contract and applicable engineering documentation rules.

---

## 19. Architectural Boundaries

The following boundaries are intentional and contractual.

### VIGIL is not a camera manufacturer

Camera-specific code belongs behind sensor interfaces.

### VIGIL is not a GPS application

Location is one input to the shared spatial model.

### VIGIL is not an AI-only application

Deterministic spatial services remain separate from probabilistic AI reasoning.

### VIGIL is not tied to one display

The same world model supports desktop, mobile, VR, and future AR devices.

### VIGIL is not dependent on physical hardware

Simulation provides a hardware-independent path for development and verification.

### VIGIL targeting is generic spatial targeting

Targeting may select, follow, and present information about generic spatial objectives. It SHALL NOT provide weapon-control interfaces, firing solutions, ballistic calculations, or automated weapon engagement.

### VIGIL customer-facing communication is semantically constrained

Customer-facing text and voice SHALL communicate materially relevant information directly and accurately. Material uncertainty, limitation, ambiguity, conflict, staleness, invalidity, unavailable information, and authority boundaries SHALL be communicated when applicable. Customer-facing communication SHALL NOT create authority that does not exist in the underlying state or authorization model.

---

## 20. Architectural Decision Summary

The architecture establishes these decisions:

1. The Spatial World Model is the central shared representation and authoritative current projection of supported environmental belief.
2. Sensors produce observations rather than authoritative world state.
3. Perception produces detections and evidence rather than authoritative world state.
4. Tracking maintains temporal continuity and remains distinct from World Entity identity.
5. Spatial/temporal fusion combines compatible sources while preserving provenance, timing, confidence, uncertainty, freshness, validity, and material disagreement.
6. Fusion and tracking cross into authoritative world state only through the controlled WorldModelUpdater boundary.
7. Deterministic spatial services handle geometry and spatial relationships without becoming a competing world-state authority.
8. Detection, track, and world-entity identity are distinct concepts.
9. Events represent meaningful changes and are emitted after authoritative state mutation when they represent state changes.
10. Current world state and world history are distinct but connected.
11. Areas and relationships are first-class spatial concepts.
12. Unknown and insufficient evidence are explicitly representable.
13. AI reasons over structured context and evidence and reports material uncertainty and semantic status.
14. Application modes determine operational relevance, priority, and presentation without changing authoritative world state.
15. Generic spatial targeting is independent of weapon-control functionality.
16. Security, authorization, privacy, and auditing are architectural requirements across the system.
17. Sensor health, data quality, and time quality are represented explicitly where relevant.
18. Simulation is a first-class development and testing capability.
19. Hardware is accessed through abstractions.
20. Presentation and human interaction are separated from authoritative spatial state and physical-action authority.
21. Customer-facing communication must preserve semantic meaning and material limitations and must not create unsupported authority.
22. VIGIL remains a general environmental-awareness and spatial-information platform rather than a weapon-control system.

---

## 21. What Comes Next

This document is the approved architecture baseline for technical design. It establishes architectural responsibilities and boundaries; it does not replace component-specific technical contracts.

The Spatial World Model technical design defines the detailed data structures, coordinate reference systems, units, timestamps, uncertainty representation, sensor/frame transformations, object lifecycle, identity model, relationships, target model, world-state/history model, interfaces, and testing contracts for the Spatial Core.

Before implementation relies on a changed architectural behavior, affected technical contracts SHALL be reconciled and versioned according to the Architecture Contract.
