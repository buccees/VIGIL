# VIGIL Architecture Specification

**Project:** VIGIL  
**Full name:** Visual Intelligence & Geographic Information Layer  
**Document version:** 0.4  
**Status:** Architecture approved for technical design  
**Audience:** Project developers, reviewers, maintainers, and future contributors

---

## 1. Purpose

VIGIL is an information-first spatial and environmental awareness platform. It combines information from multiple authorized sources into a coherent, time-aware representation of an environment and presents useful information to a human user.

VIGIL also provides a human interaction layer through which users can communicate with the system and receive responses textually and, optionally, verbally.

Possible sources include cameras, GPS/GNSS, compass, IMU, maps, geographic databases, and other authorized sensors or data feeds.

VIGIL is designed for security monitoring, navigation, generic spatial targeting, search, inspection, environmental awareness, training, and simulation. The architecture is independent of particular hardware.

VIGIL is an information and presentation system, not an autonomous physical-action system. It has no physical appendages or actuators. The human user remains the final interpreter, decision-maker, and actor.

---

## 2. Architectural Goals

VIGIL shall be:

- **Hardware independent** — the core does not depend on a particular camera, phone, computer, wearable, or display.
- **Sensor independent** — sources can be replaced without rewriting the spatial core.
- **Spatially aware** — the system maintains a shared representation of the environment.
- **Time aware** — state, observations, and changes are explicitly time-qualified.
- **Uncertainty aware** — confidence and uncertainty are preserved rather than hidden.
- **AI compatible** — AI reasons over structured information instead of owning deterministic spatial truth.
- **Human interactive** — users can query, inspect, configure supported presentation behavior, and receive grounded system responses through text and optional voice.
- **Extensible** — new sensors, perception models, application modes, displays, and interaction providers can be added without redesigning the core.
- **Secure and authorized** — sensitive sources, credentials, interactions, and data are controlled and auditable.
- **Testable** — important behavior can be exercised through simulation and recorded data without physical hardware.

---

## 3. Core Architectural Principle

> **Observation is not the same thing as understanding.**

VIGIL separates source observations, perception, temporal continuity, spatial/temporal fusion, authoritative world state, derived spatial services, relevance and priority, presentation, human interaction, and optional AI analysis.

The canonical information path is:

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

Human interaction is a bidirectional interface with the user rather than a physical-action path:

```text
                 USER
                ↕   ↕
             Text  Voice
                ↕   ↕
       Human Interaction Layer
                ↕
     Structured VIGIL Information
                ↕
 World Model / Services / History
```

World-state changes produce history/events after the state change:

```text
World-state change
        ↓
 Events / History
```

Tracking is part of the pre-World-Model perception/fusion path. Events are not a perception stage; they describe meaningful changes to established or updated world state.

AI operates as an optional information-analysis and query layer over structured world state, evidence, history, and conversational context. It is not the authority that determines physical truth, authorization, or physical action.

---

## 4. Canonical Processing Lifecycle

The architecture describes the complete intended lifecycle even though implementation is incremental.

1. **Acquire** information from authorized sensors and data sources.
2. **Record Observations** with source identity, timing, quality, provenance, and uncertainty where available.
3. **Perceive Detections** from observations using deterministic or probabilistic perception.
4. **Maintain Tracks** to establish temporal continuity and estimate motion while preserving association uncertainty.
5. **Fuse Spatially and Temporally** compatible evidence across sources, coordinate frames, and time.
6. **Update the Spatial World Model** with the current supported belief about entities, areas, relationships, sensor state, and environment.
7. **Update dependent spatial/environmental services** such as distance, bearing, proximity, area membership, and other deterministic relationships.
8. **Record World-State Changes** as events/history while retaining the evidence and prior state needed for reconstruction.
9. **Analyze optional context** through AI or other information-analysis services without silently replacing authoritative state.
10. **Determine relevance and priority** for the user's current context and task.
11. **Manage attention and presentation** so the most useful information receives appropriate visual salience without discarding lower-priority world state.
12. **Present information** through a device-independent presentation model.
13. **Communicate with the user** through textual and optional verbal interaction channels.
14. **User interprets and decides** what to do with the presented information.

The implementation shall grow through this lifecycle rather than allowing an incomplete implementation to redefine the architecture.

---

## 5. Major Architectural Layers

### 5.1 Sensors and Authorized Data Sources

Provide raw information. They do not own VIGIL's world state.

### 5.2 Observations

Immutable source-level records describing what a source reported, including event time, ingest time where available, source identity, sensor type, quality, provenance, and payload references.

### 5.3 Detections / Perception

Represent perception results such as detected objects, features, scene changes, or sensor conditions. A detection is evidence, not automatically a persistent identity.

### 5.4 Tracking / Track Continuity

Associates compatible detections through time. Tracking maintains continuity, movement history, and estimated motion while preserving uncertainty and association confidence. A track is not automatically a permanent real-world identity.

### 5.5 Spatial / Temporal Fusion

Combines compatible evidence across sensors, coordinate frames, and time. It preserves provenance, confidence, uncertainty, and conflicting or insufficient evidence rather than forcing unsupported certainty.

Tracking and fusion have distinct responsibilities: tracking establishes temporal continuity; fusion combines compatible evidence and estimates across sources and time.

### 5.6 Spatial World Model

The Spatial World Model is VIGIL's authoritative current representation of environmental belief. It represents entities, locations, areas, relationships, sensors, environmental features, targets, confidence, uncertainty, provenance, and current validity.

The World Model owns current spatial truth within VIGIL. It does not claim objective certainty for every value.

### 5.7 Spatial / Environmental Services

Deterministic services operate on the World Model. Examples include distance, bearing, relative direction, coordinate transformation, proximity, point-in-area, geometry relationships, route/path relationships, visibility where sufficient evidence exists, and spatial filtering.

### 5.8 Relevance and Priority

Relevance describes usefulness to the user's context. Priority determines which valid information deserves attention first. Priority does not decide whether an entity exists in the World Model.

Priority may consider proximity, movement, change rate, zone relationships, task relevance, unexpected appearance/disappearance, persistence, recurrence, and other explicit factors. Priority should be explainable and dynamically updated.

Confidence and priority remain independent.

### 5.9 Presentation / Attention

Presentation is a core subsystem, not merely a rendering detail. It manages human attention by selecting appropriate visual salience while retaining lower-priority information in world state and history.

It must remain independent of the physical display technology.

### 5.10 Information & Event Output / Integration

VIGIL may expose structured information, events, notifications, user-attention requests, navigation/search/inspection requests, simulation results, and other authorized outputs to external consumers.

This is an **Information & Event Output / Integration layer**, not a physical-action or actuator layer. It exists so VIGIL can communicate useful information to authorized applications, services, user interfaces, or other systems without making VIGIL itself an action system.

Any external consequential action is outside VIGIL's core information/presentation boundary and requires its own authorized control and safety architecture.

### 5.11 Human Interaction / Voice Interface

The Human Interaction layer provides the bidirectional communication channel between VIGIL and its human user.

It supports:

- textual input and output;
- optional microphone-based speech input;
- speech recognition and transcript metadata;
- intent and query interpretation;
- authorization and session validation;
- conversational context;
- textual response generation;
- optional text-to-speech output;
- system status and environmental summaries; and
- grounded answers about current state and history.

The canonical interaction path is:

```text
Text Input ───────────────┐
                          ↓
Microphone → Speech-to-Text → Interaction Request
                                  ↓
                         Authorization / Context
                                  ↓
                         World Model / Services
                                  ↓
                         Grounded Response Text
                           ↙             ↘
                     Text Output      Optional TTS
                                           ↓
                                       Voice Output
```

Speech recognition is not identity or authorization. API authentication is not user authorization. Natural-language interpretation is not permission.

VIGIL may describe its current operating condition in conversational language. For example, a "day" or "session" summary may characterize the environment as quiet, active, high-change, sensor-degraded, or unusually busy when those descriptions are grounded in measurable state such as observation volume, track activity, change rate, events, sensor health, and deviation from a defined baseline.

Such summaries do not imply that VIGIL has human feelings or consciousness. Conversational personality is a presentation characteristic; underlying claims remain grounded in system state.

The interaction layer may request information or supported software-level presentation operations, but it does not gain physical-action authority.

---

## 6. Application Modes

Application modes determine what information is relevant and how it is presented. Initial modes include:

- Security monitoring
- Navigation
- Generic spatial targeting
- Search
- Inspection
- Environmental awareness
- Training and simulation

**Generic spatial targeting** means selecting and following a spatial objective such as a destination, waypoint, point of interest, landmark, detected object, world entity, area, or user-defined location. It may expose location, distance, bearing, direction, movement, and contextual information.

Targeting is independent of weapon control. Weapon-control interfaces, firing solutions, ballistic calculations, automated weapon selection/engagement, trigger control, and actuator interfaces are outside VIGIL's architecture.

Application modes may change relevance, priority, and presentation without creating a competing world-state authority.

---

## 7. World State, History, and Events

VIGIL distinguishes:

- **Current World State** — what VIGIL currently believes exists or is happening.
- **World History** — observations, detections, tracks, state transitions, and events that explain how current state developed.
- **Events** — meaningful changes or occurrences derived from world-state transitions and supporting evidence.

Events occur after the relevant world-state change. They do not replace the state that produced them.

History must preserve provenance, timing, supporting evidence, and enough prior state to answer questions such as why a belief changed or when an entity entered an area.

Stale information becomes stale state; it is not silently erased from history.

---

## 8. Identity and Uncertainty

VIGIL distinguishes:

```text
Observation → Detection → Track → World Entity
```

These identities are related but not interchangeable. Association confidence and provenance must be retained.

Unknown, stale, invalid, insufficient-evidence, calibration-uncertain, and timestamp-uncertain states must be representable explicitly.

Confidence describes support for a classification, association, or conclusion. Uncertainty describes possible error or range in an estimate. They are separate dimensions.

---

## 9. Spatial Model

VIGIL explicitly supports:

- Global/geographic coordinates
- Local world coordinates
- Sensor coordinates
- Device/user coordinates
- Display coordinates

Transform chains, units, calibration, timing, and uncertainty must be explicit.

The World Model supports point objects, geometry, areas/zones, relationships, routes, environmental features, sensors, and targets.

---

## 10. Sensors, Health, and Time

Spatially relevant sensors have stable identities and explicit state including health, validity, calibration, data quality, and timestamp quality.

Recommended sensor states include healthy, degraded, stale, unavailable, invalid, calibration uncertain, and timestamp uncertain.

Observations should preserve event time, ingest time, source-clock information, and timestamp uncertainty where available. Processing must tolerate delayed and out-of-order observations and clock differences.

---

## 11. Security, Authorization, and Privacy

VIGIL may process sensitive environmental information. Authentication, authorization, data classification, auditability, retention, privacy controls, and provenance verification are architectural requirements.

Only authorized sensors and data sources may be used. Security boundaries apply throughout the data lifecycle, not only at presentation.

Human interaction adds separate security requirements for microphone permissions, user/session identity, API credentials, capability scopes, speech/transcript handling, and conversational history.

Provider API keys and other long-lived service secrets must not be embedded in client-side code or exposed through ordinary logs. Client applications should use protected device credentials or server-mediated/short-lived session credentials where an external provider requires a secret key.

---

## 12. AI Boundary

AI consumes structured world state, evidence, history, and, where authorized, conversational context. It may perform analysis, explanation, correlation, summarization, question answering, uncertainty assessment, or other information-analysis functions.

AI output should identify supporting evidence, confidence, uncertainty, model/version where applicable, and whether the result is observation, inference, recommendation, or interpretation.

AI must not silently rewrite authoritative world state, bypass authorization, or become an autonomous physical-action authority.

---

## 13. Simulation and Hardware Abstraction

The architecture must be testable without specialized hardware. Simulated sensors and recorded data should use the same contracts as practical real sources.

Hardware-specific implementations remain behind interfaces such as pose providers, object detectors, sensor adapters, microphone/audio providers, speech-recognition providers, speech-synthesis providers, and display providers.

Simulation should cover moving/static entities, sensor timing and uncertainty, camera conditions, failures, calibration problems, occlusion, areas, events, and human interaction failures.

---

## 14. Performance and Latency

Latency is measured from environmental acquisition to useful presentation, not merely frame rate.

Important measurements include sensor acquisition, observation ingestion, perception, detection-to-track association, fusion, World Model update, prioritization, presentation, speech recognition, intent interpretation, and end-to-end environment-to-useful-response latency.

Observation age and data freshness are first-class information.

The fast path should remain low latency; expensive analysis or speech synthesis must not block immediate useful presentation.

---

## 15. Development Strategy

The architecture is complete even though implementation is not. Engineering proceeds incrementally through the defined lifecycle.

The implementation sequence is:

1. Spatial foundations and deterministic math
2. Observation → Detection → Track
3. Track → Spatial World Model
4. Track/entity lifecycle and stale-state behavior
5. Temporal model and uncertainty
6. Sensor abstraction and coordinate transformations
7. Multi-sensor fusion
8. Events/history and message contracts
9. Performance and latency instrumentation
10. Relevance, priority, and attention
11. Presentation model
12. Human interaction and voice interfaces
13. Device integration
14. Simulation, replay, and validation
15. Application modes and authorized integrations
16. Optional AI-assisted conversational analysis

Incomplete implementation must not be treated as evidence that an architectural stage does not exist.

---

## 16. Repository Structure

```text
VIGIL/
├── docs/
│   ├── architecture/
│   │   ├── VIGIL-ARCHITECTURE-SPEC.md
│   │   └── DOCUMENTATION-RULES.md
│   └── technical/
│       ├── SPATIAL-WORLD-MODEL.md
│       ├── TRACK-WORLD-MODEL-CONTRACT.md
│       ├── SPATIAL-TEMPORAL-FUSION-CONTRACT.md
│       └── HUMAN-INTERACTION-VOICE-CONTRACT.md
├── spatial-core/
├── perception/
├── tracking/
├── events/
├── environment/
├── navigation/
├── security/
├── ai/
├── simulator/
└── presentation/
```

The repository is organized around responsibilities rather than particular hardware products.

---

## 17. Architectural Decision Summary

The approved baseline establishes that:

1. VIGIL is an information and presentation system, not an autonomous physical-action system.
2. Tracking is part of the pre-World-Model perception/fusion path.
3. Spatial/temporal fusion combines compatible evidence while preserving uncertainty and provenance.
4. The Spatial World Model owns authoritative current spatial belief.
5. Spatial/environmental services operate on that shared world state.
6. Relevance and priority control attention, not truth.
7. Presentation manages human attention without becoming the source of spatial truth.
8. World-state changes produce events/history after the state change.
9. AI is an optional information-analysis/query layer over structured state and authorized conversational context.
10. Information & Event Output / Integration provides authorized external information interfaces without making VIGIL an action/actuation system.
11. Human Interaction / Voice provides bidirectional communication through text and optional speech.
12. Speech recognition, identity, authentication, authorization, and intent interpretation are distinct concerns.
13. Conversational summaries such as "what kind of day is VIGIL having?" must be grounded in measurable system state rather than implied human experience.
14. Generic spatial targeting is informational and independent of weapon control.
15. The full architecture lifecycle is the intended implementation target; code is being built incrementally through it.
16. Security, authorization, privacy, provenance, uncertainty, time, simulation, and testability are architectural requirements.

---

## 18. Next Engineering Milestones

The architecture is now established through the human interaction boundary. The next implementation milestones are:

1. **Spatial / Temporal Fusion** — implement the approved fusion contract and deterministic fused-estimate boundary.
2. **Human Interaction foundation** — define request/response models and text interaction interfaces.
3. **World Model query services** — expose grounded structured queries for conversational use.
4. **Voice adapters** — add optional speech-to-text and text-to-speech provider interfaces with explicit authentication and privacy boundaries.
5. **Conversation and session context** — support follow-up questions, status summaries, and grounded session/day descriptions.
6. **Presentation and device integration** — connect interaction outputs to future headset/display/audio runtimes.

Implementation should continue to preserve the separation between environmental truth, analysis, communication, and human decision-making.
