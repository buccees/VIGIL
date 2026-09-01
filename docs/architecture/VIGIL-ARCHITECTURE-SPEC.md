# VIGIL Architecture Specification

**Project:** VIGIL  
**Full name:** Visual Intelligence & Geographic Information Layer  
**Document version:** 0.2  
**Status:** Architecture approved for technical design  
**Audience:** Project developers, reviewers, maintainers, and future contributors

---

## 1. Purpose

VIGIL is an AI-powered environmental awareness and security platform.

Its purpose is to combine information from multiple authorized sources and turn that information into a coherent understanding of an environment.

Possible information sources include:

- Security and IP cameras
- Phone and wearable cameras
- GPS and other location systems
- Compass and orientation sensors
- IMU sensors
- Maps and geographic databases
- Other authorized sensors and data feeds

VIGIL is intended to understand objects, locations, movement, events, environmental characteristics, and changes over time.

The system should be useful for applications such as security monitoring, navigation, targeting, search, inspection, environmental awareness, and simulation.

The architecture must remain independent of any particular camera, phone, AR headset, vehicle, or other hardware.

---

## 2. Architectural Goals

VIGIL should be:

### Hardware independent

The core system must not require a particular camera, GPS receiver, phone, computer, or wearable display.

### Sensor independent

Different sensors should be replaceable without rewriting the rest of the system.

### Spatially aware

VIGIL should maintain a shared representation of what exists in an environment and where those things are located.

### Time aware

VIGIL should understand that the environment changes. Objects move, appear, disappear, and change state.

### Uncertainty aware

Measurements and AI conclusions are not always certain. The system should preserve confidence and uncertainty rather than pretending every result is exact.

### AI compatible

The AI should reason over structured information rather than being responsible for basic deterministic geometry or sensor plumbing.

### Extensible

New sensors, perception models, displays, application modes, and AI models should be addable without redesigning the entire system.

### Secure

Access to cameras, stored data, alerts, and system capabilities must be controlled and auditable.

### Testable

Important functionality must be testable without physical hardware through simulation and recorded data.

---

## 3. Core Architectural Principle

The most important rule in VIGIL is:

> **Observation is not the same thing as understanding.**

A camera may observe an object. A perception system may classify it. A spatial service may estimate its position. A tracking service may determine that it is the same object seen previously. The AI may then reason about what that object and its behavior mean in context.

These responsibilities must remain separate.

The basic flow is:

**Sensor → Observation → Perception → Spatial/Temporal Fusion → Spatial World Model → Reasoning → Application Mode → Presentation**

Supporting services such as tracking, event detection, security, logging, evidence/history, sensor health, and simulation operate around this flow.

---

## 4. High-Level Architecture

VIGIL is organized into the following major layers and supporting services.

### Layer 1: Sensors and Data Sources

Provides raw information to VIGIL.

Examples:

- Cameras
- GPS
- Compass
- IMU
- Maps
- Geographic databases
- Other authorized sensors

This layer does not decide what the information means.

### Layer 2: Observation

Converts raw sensor input into timestamped observations.

An observation may say that a camera saw something at a particular time, that a GPS receiver reported a position, or that an IMU reported an orientation.

Observations preserve their source, event timestamp, ingestion timestamp where applicable, quality, clock information, and uncertainty.

### Layer 3: Perception

Turns observations into useful interpretations.

Examples:

- Object detected
- Vehicle detected
- Sign detected
- Building feature detected
- Person detected where the deployment is authorized to process people
- Scene changed
- Camera view became obstructed

Perception produces evidence. It does not own the authoritative spatial world state.

### Layer 4: Spatial and Temporal Fusion

Combines observations and perception results from different sources while preserving provenance, timing, confidence, and uncertainty.

This layer is responsible for resolving or representing relationships among coordinate systems, sensor frames, device pose, and time.

It must be possible to retain conflicting or insufficient evidence rather than forcing a false single answer.

### Layer 5: Spatial World Model

The Spatial World Model is VIGIL's shared representation of the environment.

It contains structured information about:

- Objects
- Locations
- Areas and zones
- Targets
- Cameras and sensors
- Routes
- Points of interest
- Hazards
- Environmental features
- Relationships
- Current world state
- Evidence and observations
- World history
- Confidence and uncertainty

The world model is the authoritative representation of VIGIL's current **belief about the environment**, not a claim that every value is objectively certain.

### Layer 6: Spatial Services

Deterministic services operate on the world model.

Examples:

- Distance calculation
- Bearing calculation
- Relative direction
- Elevation relationships
- Coordinate conversion
- Object proximity
- Movement and velocity estimation
- Route relationships
- Visibility relationships where sufficient data exists
- Collision and proximity analysis

These calculations should be reproducible and testable.

### Layer 7: Tracking and Event Processing

Tracking maintains continuity through time.

Event processing identifies meaningful changes.

Examples include:

- Object appeared
- Object disappeared
- Object moved
- Object changed location
- Object entered or left an area
- Camera view changed
- Environmental condition changed
- A configured monitoring condition occurred

Tracking and events use the current world state together with historical evidence rather than replacing that history.

### Layer 8: VIGIL AI

The AI reasons over the structured information produced by the lower layers.

The AI should be able to:

- Combine information from multiple sources
- Explain observations
- Compare current and previous states
- Identify relationships
- Assess confidence
- Identify uncertainty
- Summarize events
- Answer questions about the environment
- Explain the evidence supporting a conclusion
- Recommend appropriate actions within the authorized application mode

The AI should not be responsible for basic geometry that can be calculated deterministically.

### Layer 9: Application Modes

Application modes determine what VIGIL is being used for and what information is relevant.

Initial modes include:

- Security monitoring
- Navigation
- Targeting
- Search
- Inspection
- Environmental awareness
- Training and simulation

**Targeting** is a generic spatial mode. It allows a user or authorized application to select and follow a spatial objective such as a destination, waypoint, point of interest, landmark, detected object, or user-defined location. It may present location, distance, bearing, direction, movement, and contextual information about the selected target.

Targeting is independent of weapon control. Weapon-control interfaces, firing solutions, ballistic calculations, and automated weapon engagement are outside VIGIL's architecture.

A mode may change priorities and presentation without changing the underlying world model.

### Layer 10: Presentation

Presentation converts VIGIL information into an interface appropriate for the user and device.

Possible interfaces include:

- Desktop dashboard
- Web interface
- Phone
- Tablet
- VR headset
- Future AR glasses
- Future visor or wearable display

The presentation layer should not become the source of truth for spatial state.

---

## 5. Data Flow

A typical camera observation should travel through the system approximately as follows:

1. A camera provides a frame.
2. The sensor adapter records the source and timestamps.
3. The perception system analyzes the frame.
4. An object observation is created.
5. Spatial/temporal fusion incorporates sensor pose, calibration, timing, and other relevant evidence.
6. Tracking determines whether the observation corresponds to an existing tracked object.
7. Spatial services calculate useful spatial relationships.
8. The Spatial World Model is updated with the resulting state and provenance.
9. The event system determines whether a meaningful change occurred.
10. The AI receives the relevant structured context and supporting evidence.
11. The AI produces an analysis with confidence and uncertainty.
12. The active application mode determines what should be presented or recommended.
13. The presentation layer displays the result or creates an authorized alert/action request.

This same general pattern should work for GPS, IMU, maps, and other sensors.

---

## 6. Spatial World Model

The Spatial World Model is the central architectural component of VIGIL.

It should represent the world independently of how that information was obtained.

### 6.1 Current World State and World History

VIGIL must distinguish between:

**Current World State** — what VIGIL currently believes exists and is happening.

**World History** — the observations, detections, tracks, state changes, and events that explain how the current state developed.

History should preserve provenance so the system can answer questions such as:

> Why does VIGIL believe this object is here?

### 6.2 User State

When VIGIL is operating with a mobile user, the world model may contain:

- Position
- Altitude
- Heading
- Pitch
- Roll
- Timestamp
- Position uncertainty
- Orientation uncertainty
- Pose source and quality

### 6.3 Objects

Objects are generic spatial entities.

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

An object may contain:

- Unique identifier
- Type and subtype
- Position
- Dimensions when known
- Distance when derived
- Bearing when derived
- Direction of travel when known
- Velocity when known
- Confidence
- Uncertainty
- Tracking state
- Observation sources
- First observed time
- Last observed time
- Metadata

### 6.4 Detection, Track, and Entity Identity

VIGIL should distinguish among:

- **Observation:** what a sensor reported
- **Detection:** what perception inferred from an observation
- **Track:** a temporal association of observations believed to refer to the same physical object
- **World Entity:** the spatial object represented in the current world model

Identity associations should carry confidence and must be allowed to remain uncertain.

### 6.5 Targets

The word **target** is intentionally generic in VIGIL.

A target can be:

- A destination
- A waypoint
- A point of interest
- A landmark
- A detected object
- A user-defined location
- Another spatial objective

A target is a selected spatial objective, not necessarily a distinct physical object. Target state should reference the underlying world entity or location when applicable.

Target handling must remain independent of any weapon or weapon-control system.

### 6.6 Areas and Zones

The world model must support spatial regions as well as points and objects.

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

Objects and events may have relationships to areas, such as entering, leaving, being inside, or being near a zone.

### 6.7 Relationships

VIGIL should support explicit relationships among world entities.

Examples include:

- Observed-by
- Associated-with
- Inside
- Near
- Moving-toward
- Located-in
- Connected-to
- Related-to

Relationships must preserve confidence and provenance where the relationship is inferred rather than directly established.

### 6.8 Environment

The environment describes persistent or semi-persistent characteristics of an area.

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

The environment model should describe objective characteristics and evidence rather than making unsupported tactical assumptions.

### 6.9 Unknown and Insufficient Evidence

VIGIL must explicitly represent unknown or insufficiently known values.

Unknown must not be represented as zero, empty, or an AI guess.

A conclusion should be distinguishable from a measured fact and from an unresolved value.

---

## 7. Cameras as Spatial Sensors

Security cameras are a major VIGIL use case.

A camera should be treated as a spatial sensor rather than simply as a video player.

Each camera can have metadata such as:

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

VIGIL should eventually support multiple cameras observing the same environment.

The system should be capable of combining observations from different cameras when the available information and confidence justify doing so.

A camera failure, stale feed, obstructed view, moved camera, or uncertain calibration should be represented explicitly rather than silently ignored.

---

## 8. Object Tracking

Detection answers:

> What is visible now?

Tracking answers:

> Is this the same object we saw previously, and how has it changed?

VIGIL tracking should support:

- Persistent object identifiers
- Position history
- Movement history
- Velocity estimates
- Confidence
- Uncertainty
- Lost and reacquired states
- Multiple observation sources

Tracking must preserve uncertainty. An uncertain association should not be treated as a confirmed identity.

---

## 9. Event System

VIGIL should distinguish between continuous state and events.

For example:

**State:** A vehicle is present in an area.

**Event:** A vehicle entered the area.

This distinction is important for both AI reasoning and security monitoring.

Events should contain enough information to reconstruct what happened, including source, timestamp, relevant objects, confidence, uncertainty, and supporting observations where appropriate.

---

## 10. AI Reasoning Architecture

The AI should operate on structured context produced by VIGIL rather than directly owning sensor state.

A preferred reasoning cycle is:

**Observation → Evidence → Analysis → Confidence → Uncertainty → Proposed Action → Verification**

For important actions, the system should support human review and approval.

The AI should be able to say when available information is insufficient.

The AI should be able to explain the evidence and assumptions supporting an important conclusion.

The AI should not silently change authoritative world state merely because it generated an interpretation.

---

## 11. Security Architecture

Because VIGIL may process security-camera feeds and other sensitive environmental information, security is part of the architecture rather than an afterthought.

The system should eventually include:

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

Security controls should apply throughout the data flow rather than only at the user-interface layer.

The system should maintain an audit trail for significant automated decisions and administrative actions.

---

## 12. Sensor Health and Data Quality

VIGIL should represent sensor and data-source health explicitly.

Possible states include:

- Healthy
- Degraded
- Stale
- Unavailable
- Invalid
- Calibration uncertain
- Timestamp uncertain

Data quality should be available to downstream fusion, tracking, AI reasoning, and presentation.

A missing or unreliable source should not silently appear equivalent to a healthy source.

---

## 13. Time Model

Time is a first-class part of the architecture.

Observations and events should preserve, where available:

- Source event timestamp
- System ingestion timestamp
- Source clock information
- Timestamp uncertainty

The system should account for time differences among multiple cameras and sensors when associating observations or reconstructing events.

---

## 14. Simulation

VIGIL must be useful before specialized hardware exists.

The simulator will provide virtual versions of the system's inputs.

It should eventually simulate:

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

The simulator should feed the same interfaces used by real sensors whenever practical.

This allows core functionality to be tested without physical hardware.

---

## 15. Hardware Abstraction

Hardware-specific implementation must remain behind interfaces.

Conceptually, VIGIL should support abstractions such as:

**Pose Provider**

Provides position and orientation.

Possible implementations:

- Simulated pose provider
- GPS/IMU provider
- Visual odometry provider
- Future sensor-fusion provider

**Object Detector**

Provides object observations.

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

The core should not need to know which physical device is being used.

---

## 16. Repository Structure

The initial repository should be organized around responsibilities rather than individual hardware products.

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

The exact programming-language and build-system choices will be documented in the technical design phase.

---

## 17. Development Order

VIGIL should be developed from the center outward.

### Phase 1 — Architecture

Define responsibilities, interfaces, data ownership, security boundaries, and testing strategy.

### Phase 2 — Spatial Core

Implement the Spatial World Model, coordinate/reference-frame handling, uncertainty representation, and deterministic spatial calculations.

### Phase 3 — Simulator

Create simulated users, objects, sensors, cameras, areas, movement, and sensor-quality conditions.

### Phase 4 — Tracking and Events

Add persistent objects, movement tracking, identity association, and event generation.

### Phase 5 — Perception

Add camera and recorded-data perception pipelines.

### Phase 6 — AI Integration

Add AI reasoning over the structured world model and evidence/history.

### Phase 7 — Security Monitoring

Add authorized multi-camera monitoring, alerts, audit logging, and administration.

### Phase 8 — Navigation, Targeting, and Environmental Awareness

Add route relationships, generic target selection/following, environmental characteristics, search, inspection, and related modes.

### Phase 9 — Presentation

Build desktop and mobile interfaces, followed later by VR and AR integrations.

### Phase 10 — Hardware Integration

Integrate specialized sensors and future wearable hardware after the software architecture is stable.

---

## 18. Testing Strategy

Every major subsystem should be testable independently.

Testing should include:

- Unit tests
- Spatial math tests
- Coordinate transformation tests
- Time synchronization/association tests
- Sensor simulation tests
- Tracking tests
- Identity-association tests
- Event tests
- AI input/output contract tests
- Evidence/provenance tests
- Security tests
- Integration tests
- End-to-end simulated scenarios

The simulator should become an important part of regression testing.

A change should not require physical hardware to verify basic core behavior.

---

## 19. Architectural Boundaries

The following boundaries are intentional.

### VIGIL is not a camera manufacturer

Camera-specific code belongs behind sensor interfaces.

### VIGIL is not a GPS application

Location is one input to the shared spatial model.

### VIGIL is not an AI-only application

Deterministic spatial services remain separate from probabilistic AI reasoning.

### VIGIL is not tied to one display

The same world model should support desktop, mobile, VR, and future AR devices.

### VIGIL is not dependent on physical hardware

Simulation comes first so development can continue without specialized equipment.

### VIGIL targeting is generic spatial targeting

Targeting may select, follow, and present information about generic spatial objectives. It does not provide weapon-control interfaces, firing solutions, ballistic calculations, or automated weapon engagement.

---

## 20. Architectural Decision Summary

The approved architecture establishes these decisions:

1. The Spatial World Model is the central shared representation of the environment.
2. Sensors produce observations rather than authoritative world state.
3. Perception produces evidence and detections.
4. Spatial/temporal fusion combines sources while preserving provenance, timing, confidence, and uncertainty.
5. Deterministic spatial services handle geometry and spatial relationships.
6. Tracking maintains object continuity over time.
7. Detection, track, and world-entity identity are distinct concepts.
8. Events represent meaningful changes in state.
9. Current world state and world history are distinct but connected.
10. Areas and relationships are first-class spatial concepts.
11. Unknown and insufficient evidence are explicitly representable.
12. AI reasons over structured context and evidence and reports uncertainty.
13. Application modes determine operational priorities and presentation.
14. Targeting is a generic spatial application mode.
15. Security and auditing are architectural requirements across the system.
16. Sensor health and time quality are represented explicitly.
17. Simulation is a first-class development and testing capability.
18. Hardware is accessed through abstractions.
19. Presentation is separated from core spatial state.
20. The architecture supports both fixed security cameras and mobile/wearable sensors.
21. VIGIL remains a general environmental-awareness platform rather than a weapon-control system.

---

## 21. What Comes Next

This document is the approved architecture baseline, not an implementation specification.

The next document should define the **Technical Design for the Spatial World Model**.

That design should specify the actual data structures, coordinate reference systems, units, timestamps, uncertainty representation, sensor/frame transformations, object lifecycle, identity model, relationships, target model, world-state/history model, interfaces, and testing contracts.

Only after that technical design is reviewed should implementation begin.
