# VIGIL Architecture Contract

**Project:** VIGIL  
**Document:** Architecture Contract  
**Version:** 0.2  
**Status:** Approved architectural baseline  
**Audience:** Developers, reviewers, maintainers, and future contributors

## 1. Purpose

This document converts the architectural decisions established by the VIGIL architecture specification and implementation contracts into normative requirements.

It is an architecture-level contract. Component-specific technical contracts remain authoritative for the detailed behavior of their respective boundaries.

The architecture shall be implemented as a layered information system in which source observations, derived perception, temporal continuity, spatial/temporal fusion, authoritative world state, deterministic spatial services, relevance and priority, presentation, human interaction, customer-facing communication, and optional AI analysis remain distinct responsibilities.

## 2. Normative Language

The terms **MUST**, **MUST NOT**, **SHALL**, **SHALL NOT**, **SHOULD**, and **MAY** are normative.

- **MUST / SHALL** defines an architectural requirement.
- **MUST NOT / SHALL NOT** defines a prohibited behavior.
- **SHOULD** defines a preferred behavior that may be deviated from only for a documented reason.
- **MAY** defines an allowed but optional capability.

Implementation details may change. The semantic requirements in this contract shall not be changed implicitly by implementation convenience.

## 3. System Boundary

VIGIL is an information, spatial-intelligence, environmental-awareness, and presentation system.

VIGIL SHALL:

- acquire information from authorized sources;
- preserve source identity, timing, quality, provenance, and uncertainty where available;
- derive detections and maintain temporal tracks;
- fuse compatible spatial and temporal evidence;
- maintain an authoritative Spatial World Model;
- provide deterministic spatial/environmental services;
- determine relevance and priority for presentation;
- present information to a human user;
- support authorized textual and optional voice interaction; and
- optionally provide bounded AI-assisted analysis and explanation.

VIGIL SHALL NOT independently perform consequential physical action or provide an architectural path that grants an AI subsystem, voice interface, presentation layer, confidence value, priority value, track state, or external integration such authority.

The human remains responsible for consequential decisions and physical actions.

## 4. Canonical Information Lifecycle

The canonical processing lifecycle SHALL be:

```text
Authorized Sources
       ↓
Observations
       ↓
Detections / Perception
       ↓
Track Continuity
       ↓
Spatial / Temporal Fusion
       ↓
Spatial World Model
       ↓
Spatial / Environmental Services
       ↓
Relevance / Priority
       ↓
Presentation / Attention
       ↓
Human User
       ↓
Human Decision / Action
```

Human interaction is a bidirectional information interface and SHALL NOT be interpreted as a physical-action path.

World-state changes SHALL produce downstream events/history only after the authoritative state change.

Optional AI analysis MAY operate over structured state, evidence, history, and authorized conversational context. AI SHALL NOT replace authoritative deterministic state with an unsupported interpretation.

## 5. Responsibility and Authority Boundaries

### 5.1 Observations

Observations SHALL represent source-reported information. They SHALL preserve provenance and timing semantics sufficient for downstream validation.

### 5.2 Detections

Detections SHALL represent perception results. A detection SHALL NOT by itself establish persistent identity or authoritative world state.

### 5.3 Tracks

Track maintenance SHALL own temporal continuity and track state. Track management SHALL NOT directly mutate the authoritative World Model.

### 5.4 Spatial / Temporal Fusion

Fusion SHALL combine only evidence that is compatible under the active fusion policy.

Fusion SHALL preserve confidence, uncertainty, freshness, validity, provenance, and material disagreement.

Fusion SHALL NOT directly mutate the authoritative World Model.

Fusion output SHALL cross into authoritative world state only through the controlled World Model update boundary defined by the Track → World Model contract.

Fusion SHALL NOT force identity, precision, or certainty when evidence is unresolved or insufficient.

### 5.5 World Model

The Spatial World Model SHALL be the authoritative current projection of VIGIL's supported environmental belief.

World Model identity SHALL remain distinct from detection and track identity.

The World Model SHALL expose validity and freshness and SHALL retain explicit unknown, stale, invalid, insufficient-evidence, calibration-uncertain, and timestamp-uncertain states where applicable.

### 5.6 World Model Updates

All track-derived or fusion-derived updates to authoritative world state SHALL pass through the controlled WorldModelUpdater boundary.

The updater SHALL validate input before committing state, preserve entity identity, reject older state from overwriting newer authoritative state, preserve provenance, and emit state-change events only after successful state mutation.

The updater SHALL NOT invent unavailable spatial information or silently convert unavailable values into zero, defaults, or fabricated precision.

### 5.7 Spatial / Environmental Services

Deterministic spatial services SHALL operate on authoritative World Model state or explicitly supplied validated inputs.

They SHALL NOT become a competing source of authoritative world state.

### 5.8 Relevance and Priority

Relevance SHALL describe usefulness to the user's context.

Priority SHALL determine which valid information receives attention first.

Priority SHALL NOT determine whether an entity exists in authoritative world state.

Confidence and uncertainty SHALL remain distinct from relevance and priority.

### 5.9 Presentation and Attention

Presentation SHALL manage human attention without becoming the source of environmental truth.

Lower-priority information MAY receive reduced visual salience, but SHALL NOT be deleted from authoritative state solely because it is not currently presented.

## 6. Identity Contract

VIGIL SHALL maintain separate identity domains:

```text
Detection ID ≠ Track ID ≠ World Entity ID
```

Association between domains SHALL be explicit and traceable.

An association claim SHALL NOT be treated as proof of physical identity merely because the association is internally stable.

Future multi-source identity resolution MAY replace an initial deterministic association policy, but SHALL preserve the same identity-domain separation.

## 7. Time, Freshness, and Ordering

VIGIL SHALL distinguish physical event/observation time from ingestion and processing time.

Implementations SHALL tolerate delayed and out-of-order information without allowing older evidence to overwrite newer authoritative current state.

Freshness SHALL be explicit wherever current-state claims depend on observation age.

Stale information SHALL NOT be represented as current merely because it remains stored.

Historical retention SHALL remain distinct from current-state validity.

## 8. Spatial Frames, Units, and Calibration

Spatial values SHALL have an explicit coordinate-frame interpretation.

Units SHALL be explicit and consistent with the Spatial World Model technical specification.

Coordinate transformations SHALL identify source and destination frames and SHALL be valid for the applicable time and calibration state.

Unknown, invalid, or incompatible transforms SHALL NOT be silently applied.

Calibration quality and sensor health SHALL affect the validity or quality of derived spatial information where applicable.

## 9. Confidence, Uncertainty, and Evidence Quality

Confidence and uncertainty SHALL remain independent dimensions.

- Confidence describes support for a classification, association, or conclusion.
- Uncertainty describes expected error or range in an estimate.

Missing uncertainty SHALL remain unknown rather than being replaced by a fabricated precise value.

Evidence quality, validity, freshness, calibration state, and provenance SHALL remain available to downstream consumers where relevant.

## 10. Provenance

Derived information SHALL retain a traceable path to its supporting evidence.

The minimum conceptual provenance chain SHALL be:

```text
World Entity / Fused Estimate
        ↓
Track / Fusion Evidence
        ↓
Detection
        ↓
Observation / Authorized Source
```

Implementations MAY use equivalent storage or references, provided the chain remains resolvable.

## 11. Human Interaction and Voice

Human interaction SHALL remain an information channel.

Text and voice MAY request information or authorized software-level operations, but neither modality SHALL create physical-action authority.

Authentication, authorization, session identity, conversational context, and speech recognition SHALL remain distinct concerns.

Conversational context SHALL NOT silently expand authorization.

Current-state responses SHALL respect freshness, uncertainty, provenance, and data availability.

When the requested information cannot be established reliably, the system SHALL communicate the limitation rather than fabricate certainty.

Microphone access SHALL be permission-controlled when speech input is enabled.

## 12. Customer-Facing Communication and Semantic Clarity

VIGIL's customer-facing interfaces, including AI-generated text and voice communication, SHALL communicate materially relevant information clearly, directly, and accurately according to the authoritative information available to VIGIL.

VIGIL SHALL NOT rely on ambiguity, suggestion, implication, conversational implication, euphemism, or unstated convention to communicate a material system state, limitation, authority boundary, or user-relevant fact when that information can be stated directly.

The AI MUST NOT present an observation, inference, estimate, interpretation, recommendation, assumption, or unresolved result as an established fact unless the available authoritative evidence supports that representation.

When information is unknown, unavailable, uncertain, stale, invalid, ambiguous, conflicting, or otherwise unresolved, the customer-facing response SHALL communicate that condition when it is material to the user's understanding of the information being presented.

The AI MUST distinguish, where materially relevant, among:

- established information or authoritative state;
- source observation;
- perception or detection result;
- inference or derived result;
- estimate;
- uncertainty;
- unknown or unavailable information;
- stale or invalid information;
- conflicting evidence;
- recommendation; and
- AI interpretation or analysis.

The AI MAY simplify or summarize information for usability, but SHALL NOT alter the semantic meaning, authority, confidence, uncertainty, validity, freshness, provenance, or material limitations of the underlying information.

Conversational fluency, brevity, politeness, persuasion, or natural-sounding language SHALL NOT take precedence over accurate representation of materially relevant system state.

Customer-facing language SHALL NOT intentionally leave a materially relevant conclusion for the user to infer when VIGIL can state the conclusion and its applicable qualification directly.

The customer-facing communication layer SHALL NOT create authority that does not exist in the underlying system state or authorization model.

## 13. AI and Configurable Behavior

AI SHALL be treated as an optional analysis and interaction capability over structured VIGIL information.

AI MAY:

- interpret natural-language requests;
- summarize structured state and history;
- explain observations, relationships, changes, and uncertainty;
- perform bounded information analysis; and
- generate grounded textual or optional verbal responses.

AI SHALL NOT:

- become the authoritative source of physical truth;
- silently rewrite authoritative World Model state;
- bypass authorization;
- convert uncertainty into unsupported certainty;
- acquire physical-action authority; or
- use conversational context to override higher-level security or integrity requirements.

Where configurable AI behavior is implemented, configuration SHALL remain explicitly layered:

```text
Protected System / Safety / Integrity Requirements
                    ↓
Application-Level Requirements
                    ↓
Authorized User Configuration
                    ↓
Current Conversational Context
```

Lower layers SHALL NOT override higher layers.

A user's configuration SHALL be isolated to the user's authorized environment and SHALL NOT silently become global application behavior or another user's configuration.

Developer conversation, shorthand, or informal development language SHALL NOT by itself constitute customer-facing AI behavior or product configuration.

The exact security, storage, authentication, and isolation mechanisms SHALL be defined by the applicable security contracts.

## 14. Application Modes and Spatial Objectives

Application modes MAY change relevance, priority, and presentation, but SHALL NOT create a competing authority over the World Model.

Generic spatial targeting SHALL mean selecting, locating, following, or presenting a spatial objective such as a destination, waypoint, point of interest, landmark, detected object, world entity, area, or user-defined location.

Generic spatial targeting SHALL remain independent of weapon-control or consequential physical-action functionality.

## 15. Security, Authorization, and Privacy

Only authorized sources and integrations SHALL contribute information within their permitted scope.

Authentication SHALL NOT be treated as equivalent to authorization.

API credentials SHALL NOT automatically confer unrestricted user authority.

Sensitive data, credentials, microphone input, transcripts, conversational context, and user configuration SHALL be subject to appropriate access and privacy controls.

Long-lived provider secrets SHALL NOT be embedded in client-side code or exposed through ordinary logs or conversational output.

## 16. Failure and Degraded-State Semantics

Failure, invalidity, insufficiency, degradation, and uncertainty SHALL be represented explicitly where they affect system behavior or user interpretation.

The system SHALL NOT silently convert:

- missing values into zero;
- unknown uncertainty into perfect precision;
- stale state into current state;
- invalid evidence into valid evidence; or
- ambiguous user intent into an unauthorized operation.

Material evidence disagreement SHALL remain visible to the fusion and downstream interpretation layers according to their contracts.

## 17. Contract Relationship and Precedence

This architecture contract SHALL be read together with the following normative or implementation contracts:

- `AUTONOMY-HUMAN-DECISION-BOUNDARY.md`
- `SPATIAL-WORLD-MODEL.md`
- `TRACK-WORLD-MODEL-CONTRACT.md`
- `SPATIAL-TEMPORAL-FUSION-CONTRACT.md`
- `HUMAN-INTERACTION-VOICE-CONTRACT.md`
- `DOCUMENTATION-RULES.md`

Component-specific technical contracts SHALL control detailed behavior within their defined boundaries.

Where a component contract imposes a stricter requirement than this architecture contract, the stricter requirement SHALL apply within that component boundary.

A proposed change that creates a semantic conflict between contracts SHALL NOT be implemented as a silent interpretation. The conflicting contracts SHALL be reconciled and versioned before dependent implementation proceeds.

Architecture-level changes SHALL be reflected in affected technical contracts before implementation relies on the changed behavior.

## 18. Implementation and Verification Requirements

Implementations SHALL provide tests or equivalent verification for architectural invariants applicable to the component being implemented.

At minimum, verification SHALL cover:

- identity-domain separation;
- timestamp and freshness semantics;
- explicit uncertainty handling;
- provenance preservation;
- invalid-input handling;
- deterministic behavior where the applicable contract requires it;
- controlled World Model mutation;
- human-decision boundaries;
- authorization boundaries; and
- customer-facing semantic clarity and explicit representation of materially relevant limitations.

Failed verification attempts SHALL be documented with the attempt number, failing check, corrective change, affected commit, and subsequent result. This history SHALL remain available in project-facing engineering documentation so repeated implementation pushes remain auditable.

## 19. Change Control

The architecture and technical contracts SHALL be treated as versioned engineering artifacts.

A code change SHALL NOT silently redefine a documented contract.

A contract change SHALL identify:

- the affected architectural boundary;
- the reason for the change;
- impacted contracts;
- compatibility or conflict analysis;
- required implementation changes; and
- required verification.

## 20. Current Baseline

At the time of this version:

- the architecture specification on the default branch is version 0.2;
- the Spatial World Model technical design is version 0.1 and proposed for implementation review; and
- this document establishes the contractual architecture layer without replacing detailed technical contracts.

Additional component contracts named in this document are normative dependencies when present and SHALL be reconciled with this contract before dependent implementation relies on them.
