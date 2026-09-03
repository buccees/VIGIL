# VIGIL

**Visual Intelligence & Geographic Information Layer**

> ## VIGIL is a spatial intelligence platform — not simply an application.
>
> The long-term goal is for VIGIL to operate as a **system-level spatial intelligence layer inside a headset or other spatial-computing operating environment**, providing shared environmental perception, spatial state, tracking, analysis, prioritization, and information presentation to the user and to authorized applications.
>
> The repository is therefore building the **core platform and middleware**, not a conventional standalone app. The current implementation is an incremental foundation toward that system-level deployment target; it is **not yet a complete operating system, headset runtime, or hardware integration**.

VIGIL is an information-first spatial and environmental awareness platform. It is designed to sense, process, correlate, retain, and present information about an environment that a user may not be able to perceive through normal human observation.

VIGIL is intentionally a **presentation and awareness system, not an autonomous action system**. It has no physical appendages or actuators. Its output is information presented to the user on a screen or future visual interface. The user remains responsible for interpreting the information and deciding how to react.

# Project Priorities & Delivery Outlook

> **Planning note:** Dates below are working targets, not promises. They are intended to keep implementation ordered, expose deferred work, and make it clear when each architectural concern is expected to be resolved. CI, contract changes, or newly discovered dependencies may move a target date.

## Current Priority — Spatial / Temporal Fusion

**Target completion:** **September 6, 2026**  
**Status:** 🟡 In progress

Before advancing to Relevance / Priority, the following must be completed:

- [x] Implement deterministic fusion foundation.
- [x] Preserve confidence, uncertainty, freshness, and provenance.
- [x] Prevent fusion from directly mutating the authoritative World Model.
- [x] Make the World Model update boundary explicit and observable.
- [x] Verify the boundary implementation in CI (#38 passed).
- [ ] Reconcile the Track → World Model contract with `FusedEstimate`.
- [ ] Make fusion exclusions/rejections explicitly observable.
- [ ] Clarify deterministic association keys versus authoritative World Entity identity.
- [ ] Correct and formalize `FusedEstimate.qualified` semantics.
- [ ] Establish and maintain the durable CI attempt log.
- [ ] Run final Fusion contract/integration verification.

**Exit condition:** Fusion behavior, contracts, diagnostics, provenance, and World Model mutation boundaries are consistent, tested, and CI-verified.

## Next — Relevance / Priority

**Target:** **September 7–11, 2026**  
**Priority:** 🔴 Highest after Fusion

Implement deterministic, explainable relevance and priority over authoritative World Model information.

Required outcomes:

- [ ] Define relevance independently from confidence and truth.
- [ ] Define priority independently from confidence and uncertainty.
- [ ] Preserve low-priority information in the World Model.
- [ ] Establish explainable priority factors and precedence.
- [ ] Test priority changes as spatial/contextual conditions change.
- [ ] Define failure and unavailable-context behavior.

**Exit condition:** Priority changes presentation relevance without altering authoritative world state, and the behavior is deterministic and explainable.

## Then — Attention / Presentation

**Target:** **September 12–18, 2026**  
**Priority:** 🟠 High

Build the human-scale presentation layer on top of World Model state and Relevance / Priority.

- [ ] Define attention-management contract.
- [ ] Represent persistence, freshness, uncertainty, and provenance in presentation.
- [ ] Prevent presentation from becoming a source of environmental truth.
- [ ] Preserve lower-priority information for inspection.
- [ ] Define latency and presentation-quality measurements.
- [ ] Establish the first implementation-level presentation interface.

**Exit condition:** VIGIL can convert prioritized environmental information into a bounded, understandable presentation without mutating authoritative state.

## Then — Human Interaction / Voice

**Target:** **September 19–25, 2026**  
**Priority:** 🟠 High

Implement bidirectional text and optional voice interaction within the approved human-decision boundary.

- [ ] Implement authorized information queries.
- [ ] Ground responses in current World Model state, history, provenance, freshness, and uncertainty.
- [ ] Define ambiguity and stale-data behavior.
- [ ] Keep AI/voice separate from authorization and physical action.
- [ ] Add interaction-level verification tests.

**Exit condition:** Human interaction can inspect and communicate with VIGIL without becoming an autonomy or authority mechanism.

## Integration / Testing

**Target:** **September 26–October 4, 2026**  
**Priority:** 🔴 Release-gating

Integrate the completed layers and verify the architecture end-to-end.

- [ ] Full pipeline integration tests.
- [ ] Timing, freshness, and ordering verification.
- [ ] Provenance verification.
- [ ] Failure/degraded-state verification.
- [ ] Performance and latency measurements.
- [ ] Architectural invariant checks.
- [ ] CI verification of the integrated baseline.

**Expected milestone:** **October 4, 2026** for the first complete software-layer baseline, assuming no major architectural rework is required.

## Deferred Engineering Direction

These capabilities are intentionally **not required to complete the first deterministic Fusion baseline**. They remain pinned here so they are not forgotten and so implementation can be scheduled deliberately rather than added opportunistically.

| Priority | Future capability | Planned window | Resolution trigger |
|---|---|---|---|
| P1 | Advanced probabilistic association | October 2026 | After deterministic association is stable and benchmarked |
| P1 | Advanced uncertainty propagation / covariance | October–November 2026 | When uncertainty requirements exceed the initial scalar model |
| P1 | Multi-hypothesis tracking | November 2026 | When ambiguous identity/association cases require multiple live hypotheses |
| P2 | Automatic calibration | November–December 2026 | After sensor/calibration interfaces are established |
| P2 | ML-assisted fusion | December 2026+ | Only after deterministic fusion provides a trustworthy baseline |
| P2 | AI-assisted identity resolution | December 2026+ | Only after identity contracts and provenance are mature |
| P2 | Distributed fusion | 2027 direction | When multiple VIGIL nodes require shared spatial state |
| P2 | Persistent fusion databases | 2027 direction | When operational history/replay volume requires dedicated persistence |

### Deferred-work rule

A deferred capability should move into active implementation only when its **resolution trigger** is met and the current architecture/contracts can support it without weakening established boundaries. Each activation should receive its own technical contract, implementation milestone, tests, and CI verification.

The deferred list is a planning tool, not permission to skip current dependencies.

## What We Are Building

VIGIL should ultimately function as a **spatial intelligence substrate for a headset OS or spatial-computing platform**. Instead of every application independently implementing its own perception and environmental model, VIGIL can provide shared system services such as:

- sensor and authorized-data ingestion;
- environmental observations and detections;
- persistent track continuity;
- spatial and temporal fusion;
- a shared Spatial World Model;
- spatial search, relationships, zones, and environmental services;
- historical state and replayable provenance;
- relevance and priority computation;
- attention-aware information presentation; and
- structured information and event interfaces for authorized consumers.

The intended separation is:

```text
             HEADSET HARDWARE / SENSORS
                        ↓
              HEADSET OS / RUNTIME
                        ↓
        ┌──────────────────────────────┐
        │             VIGIL            │
        │   Spatial Intelligence Layer │
        │                              │
        │  Perception                  │
        │  Tracking                    │
        │  Spatial / Temporal Fusion   │
        │  Spatial World Model         │
        │  Spatial Services            │
        │  Relevance / Priority       │
        │  Attention / Presentation    │
        └──────────────┬───────────────┘
                       ↓
          OS / System UI / Applications
                       ↓
                      USER
                       ↓
             Human Decision / Action
```

This architecture is deliberately designed so that **hardware, headset OS, presentation technology, and individual applications can evolve independently of the spatial intelligence core**. The current Java/Gradle spatial-core project is the first implementation layer of that larger platform vision.

### Deployment Target vs. Current State

The distinction matters:

| Layer | Current state |
|---|---|
| Spatial intelligence architecture | Defined |
| Observation → Detection → Track | Implemented foundation |
| Track → World Model | Implemented foundation |
| Spatial / temporal fusion | In implementation / verification |
| Spatial/environmental services | Planned |
| Relevance / priority | Architecture defined; implementation ahead |
| Attention-aware presentation | Architecture defined; implementation ahead |
| Headset sensor integration | Not yet implemented |
| XR/display runtime integration | Not yet implemented |
| OS/device integration | Not yet implemented |
| Complete headset operating environment | Future deployment target |

So the repository should be understood as **platform infrastructure under construction**, rather than as a finished headset application.

## Core Objective

VIGIL should provide capabilities beyond unaided human perception by combining:

- continuous machine-speed sensing;
- rapid perception and spatial processing;
- persistent tracking and environmental state;
- multi-sensor correlation and spatial reasoning;
- retention of information that a human may miss or cannot continuously remember; and
- prioritization and presentation of information within human attention limits.

The central design goal is:

> **Machine-speed acquisition and processing → persistent environmental information → human-speed interpretation and decision.**

VIGIL should continue processing the environment even when the user is not looking at a particular area or object. Information can remain available for the user to inspect and act upon later.

## Architectural Principle

VIGIL follows a strict separation between sensing, perception, world state, analysis, and presentation:

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

VIGIL may also expose structured information, events, notifications, attention requests, navigation/search/inspection requests, and other authorized outputs through an **Information & Event Output / Integration** boundary. This is an information/integration interface, not a physical-action or actuator layer.

The pipeline is intentionally one-way with respect to the physical environment. VIGIL provides information; it does not physically act on the environment.

## Core Concepts

### Observations

Observations represent sensor or authorized data-source reports. They retain provenance, timestamps, source identity, sensor information, and validity context so downstream conclusions can be traced back to evidence.

### Detections

Detections represent things identified within observations. A detection is a perception result, not automatically a persistent identity or world-model entity.

### Tracks

Tracks represent continuity across successive detections. They allow VIGIL to maintain the movement and history of something observed over time without silently inventing identity.

### Spatial / Temporal Fusion

Fusion combines compatible evidence across sensors, coordinate frames, and time. It preserves provenance, confidence, uncertainty, and conflicting or insufficient evidence rather than forcing unsupported certainty.

Tracking establishes temporal continuity; fusion combines compatible evidence and estimates across sources and time.

### World Model

The World Model represents VIGIL's current structured understanding of the environment. It should support positions, areas and zones, relationships, confidence, uncertainty, sensor provenance, and current state while keeping historical information separately available for replay and analysis.

The World Model is the authoritative spatial representation used by downstream services. Sensors do not directly modify AI understanding, and AI does not silently modify authoritative world state.

### Relevance

Relevance describes how useful a piece of environmental information is to the user's current context. Relevance is distinct from confidence and from raw detection validity.

### Priority

Priority determines which valid information deserves attention first. Priority is dynamic and may change as an object's position, movement, relationship to the user, zone membership, behavior, or current task changes.

Priority affects presentation; it does **not** determine whether something exists in the World Model.

### Confidence

Confidence describes how strongly the system supports an observation, detection, track, or derived conclusion. Confidence must remain separate from priority. A lower-confidence event can still deserve immediate attention if its potential relevance is high.

### Persistence

Persistence allows VIGIL to retain useful information after a human-visible event has passed. A transient detection can become a track and remain available through world state and history even after it leaves the immediate sensor view.

### Attention

Human attention is a limited resource. VIGIL may process substantially more information than a person can consciously consume at once. The presentation system therefore needs an explicit attention-management layer that converts machine-scale information into a usable human-scale display.

The system should maintain a distinction between:

- **Machine bandwidth:** everything the sensing and processing pipeline can continuously handle.
- **Human bandwidth:** what the user can meaningfully perceive and act upon at a given moment.

VIGIL's presentation layer bridges those two bandwidths without discarding the underlying world state.

## Fast Path and Slow Path

The immediate perception path should remain fast and deterministic wherever practical:

```text
Sensor
  → Observation
  → Detection
  → Track
  → Spatial / Temporal Fusion
  → World Model
  → Presentation
```

More expensive analysis can operate asynchronously from the World Model:

```text
World Model
  → Historical Analysis
  → Environmental Analysis
  → Anomaly Analysis
  → Contextual Interpretation
  → Optional AI Assistance
```

Expensive processing must not block immediate presentation of current environmental information. For example, a slow analysis operation must not prevent an already-established track from being displayed.

## Latency Is a First-Class Concern

VIGIL's meaningful performance metric is not simply frame rate. The system should measure the time required for environmental information to become useful information on the user's display.

Important latency measurements include:

- sensor acquisition latency;
- observation ingestion latency;
- perception latency;
- detection-to-track latency;
- spatial fusion latency;
- world-model update latency;
- prioritization latency;
- rendering latency; and
- end-to-end environment-to-display latency.

Observation age and data freshness should be visible concepts throughout the system. A displayed fact should be possible to associate with when it was observed and how old that information is.

## Presentation Is a Core Subsystem

Presentation is not a cosmetic layer added after perception. It is a primary part of VIGIL's purpose.

The presentation architecture should treat the following as first-class concepts:

- relevance;
- priority;
- confidence;
- uncertainty;
- freshness / observation age;
- persistence;
- user attention;
- current user context or task; and
- explainability / provenance.

The display should be capable of showing the most important information first while retaining lower-priority information for inspection.

A useful mental model is:

```text
                 USER ATTENTION
                       ▲
                       │
                Highest Priority
                       │
             ┌─────────┴─────────┐
             │                   │
          Important           Relevant
             │                   │
             └─────────┬─────────┘
                       │
                  Background
                       │
              World Model / History
```

VIGIL should avoid overwhelming the user with every available detection. It should continuously process the broader environment while selecting information appropriate for the user's limited attention at that moment.

## Priority Is Not a Truth Mechanism

The priority engine must not alter the underlying environmental truth merely because something is unimportant to display.

For example, VIGIL may maintain ten objects in the World Model while displaying only the two that currently deserve attention. The other eight remain available for context, history, queries, or later presentation.

Priority may consider deterministic, explainable factors such as:

- proximity to the user;
- movement toward or away from the user;
- rate of change;
- entry into a defined zone;
- relationship to the user's current task;
- unexpected appearance or disappearance;
- intersection with a relevant path or area;
- persistence and recurrence; and
- other explicitly defined environmental relationships.

Priority should be explainable rather than an opaque assertion that something is simply "important."

## Confidence and Priority Are Independent

VIGIL must not equate uncertainty with irrelevance.

For example, an approaching object with moderate confidence may deserve more immediate attention than a stationary object detected with very high confidence. The presentation should be capable of communicating both dimensions rather than collapsing them into one score.

## Human-in-the-Loop Boundary

The user is the final decision-maker.

VIGIL may detect, correlate, analyze, prioritize, explain, and present information. It must not autonomously execute consequential physical actions.

The intended interaction is:

```text
VIGIL perceives
      ↓
VIGIL maintains information
      ↓
VIGIL prioritizes presentation
      ↓
VIGIL informs user
      ↓
USER evaluates
      ↓
USER decides
      ↓
USER acts
```

AI, if used, is an optional information-analysis interface over structured system state. It is not the authority over the physical environment, sensor authorization, security permissions, or user actions.

## Spatial Model

Spatial information is a first-class concern throughout the architecture. The system should explicitly distinguish:

- global/geographic coordinates;
- local Cartesian coordinates;
- sensor coordinates;
- device/user coordinates; and
- display coordinates.

Units, coordinate frames, transformations, calibration, uncertainty, and timestamps must be explicit.

Camera and other spatial sensor calibration should include physical location, orientation, field of view, focal characteristics, distortion, mounting orientation, calibration status, calibration timestamp, and uncertainty where applicable.

Sensor health should be explicit, including states such as healthy, degraded, stale, unavailable, invalid, calibration uncertain, or timestamp uncertain.

## Identity Separation

VIGIL must distinguish:

- detection identity — an observation-level perception result;
- track identity — continuity across detections; and
- world-entity identity — an entity represented in the current world model.

The system should support an explicit unknown state. Unknown must not silently become zero, absent, or an AI guess.

## Relationships, Areas, and Zones

The World Model is not limited to point objects. It should represent meaningful spatial structures such as:

- buildings;
- rooms;
- roads and road segments;
- zones;
- monitoring areas;
- regions of interest; and
- other spatial areas.

Relationships should be explicit where useful, including concepts such as near, inside, observed-by, moving-toward, moving-away-from, associated-with, and intersecting.

## History and Replay

Current world state and historical state are separate concerns.

The current World Model answers:

> **What does VIGIL currently believe about the environment?**

History answers:

> **What did VIGIL observe and believe previously, and why?**

Historical data should preserve enough provenance and timing information to support diagnostics, review, replay, testing, and future analysis.

## Security and Authorization

VIGIL is intended to operate on authorized sensors and data sources. Authorization, auditability, and data provenance should follow information through the pipeline.

The system should not silently access unauthorized sensors, change permissions, or allow an analysis component to bypass established security boundaries.

## Offline-First Direction

The core spatial and perception pipeline should be capable of operating locally. Cloud services may provide optional additional capabilities, but basic sensing, spatial state, tracking, prioritization, and presentation should not inherently depend on a remote service.

## Project Structure

The repository is organized around the architecture rather than around a monolithic application:

```text
VIGIL/
├── docs/
│   ├── architecture/
│   │   ├── VIGIL-ARCHITECTURE-SPEC.md
│   │   └── DOCUMENTATION-RULES.md
│   └── technical/
│       └── SPATIAL-WORLD-MODEL.md
├── spatial-core/
│   ├── src/
│   │   ├── main/java/com/buccees/vigil/
│   │   │   ├── spatial/
│   │   │   └── world/
│   │   └── test/java/com/buccees/vigil/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
└── .github/
    └── workflows/
        └── spatial-core.yml
```

The current implementation is intentionally focused on the spatial foundation. The repository should grow by adding well-defined architectural layers rather than allowing sensor-specific or UI-specific code to become the system's source of truth.

## Current Engineering Direction

The current spatial-core implementation establishes the early perception chain:

**Observation → Detection → Track**

The next architectural step is to integrate persistent tracks into the World Model while preserving the distinction between current world state, perception evidence, and historical track information.

Future work should then build toward spatial/temporal fusion, world-state services, relevance and priority, attention-aware presentation, navigation/search/inspection capabilities, environmental analysis, and optional AI-assisted interpretation.

## Documentation Rules

The project-wide terminology and documentation standard is defined in [`docs/architecture/DOCUMENTATION-RULES.md`](docs/architecture/DOCUMENTATION-RULES.md). Technical documentation should remain rigorous while using terminology that accurately describes VIGIL's sensing, perception, spatial computation, environmental modeling, information prioritization, and human-machine presentation capabilities without unnecessarily implying offensive action, weapon control, or autonomous physical intervention.

## Non-Goals

VIGIL is not designed as an autonomous physical-response platform. The architecture does not provide for:

- autonomous physical actuation;
- weapon control or firing solutions;
- automated engagement;
- autonomous interception or evasion;
- AI authority to execute consequential physical actions; or
- replacing the user's decision-making with an autonomous agent.

The system's capability comes from **seeing more, processing faster, remembering longer, correlating better, and presenting useful information at the right time**.

## Documentation

See the architecture and technical specifications under `docs/` for the detailed system contracts, data models, implementation order, testing requirements, security boundaries, and definition of done.
