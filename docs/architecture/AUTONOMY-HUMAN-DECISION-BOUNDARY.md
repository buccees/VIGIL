# VIGIL Autonomy & Human Decision Boundary

**Project:** VIGIL  
**Document version:** 0.1  
**Status:** Proposed for implementation review  
**Audience:** Project developers, reviewers, maintainers, and future contributors

---

## 1. Purpose

This document defines a mandatory architectural boundary for VIGIL: VIGIL is an information, perception, spatial-intelligence, and presentation system. It does not independently perform consequential physical action.

The core principle is:

> **VIGIL observes, understands, organizes, and presents. The human decides and acts.**

VIGIL may process environmental information at machine speed, maintain persistent spatial state, analyze relationships, prioritize information, and communicate with a human through visual, textual, and optional verbal interfaces. These capabilities do not transfer consequential physical-action authority to VIGIL.

---

## 2. Architectural Boundary

The canonical human-in-the-loop path is:

```text
                 ENVIRONMENT
                      │
                      ▼
               ┌──────────────┐
               │    VIGIL     │
               │              │
               │ Observe      │
               │ Detect       │
               │ Track        │
               │ Fuse         │
               │ Model        │
               │ Analyze      │
               │ Prioritize   │
               │ Present      │
               └──────┬───────┘
                      │
                      ▼
                    HUMAN
                      │
                ┌─────┴─────┐
                │           │
             Interpret    Decide
                │           │
                └─────┬─────┘
                      ▼
                     ACT
```

The final decision and consequential action remain outside VIGIL's autonomous authority.

---

## 3. What VIGIL Is Allowed to Do

VIGIL may:

- acquire information from authorized sensors and data sources;
- create and maintain observations, detections, and tracks;
- estimate spatial and temporal state;
- fuse compatible evidence;
- maintain a Spatial World Model;
- retain temporal history and meaningful world-state changes;
- calculate deterministic spatial relationships;
- identify objects, entities, destinations, areas, and other objects of interest for informational purposes;
- perform generic spatial targeting for navigation, search, inspection, and other information purposes;
- estimate motion, proximity, bearing, direction, and related spatial information;
- determine relevance and priority for human attention;
- highlight or otherwise present information to the user;
- answer text and voice queries;
- provide explanations, summaries, and recommendations as information;
- communicate system and environmental status;
- request human authorization for supported software-level operations; and
- expose authorized information and event outputs to external consumers.

These capabilities are informational and computational. They do not by themselves constitute physical action authority.

---

## 4. Generic Spatial Targeting

VIGIL may use the concept of a spatial target when an application requires a spatial objective.

A target may be:

- a destination;
- waypoint;
- point of interest;
- landmark;
- detected object;
- world entity;
- area or zone; or
- user-defined location.

Generic spatial targeting may provide information such as location, distance, bearing, direction, movement, route relationships, proximity, and contextual state.

**Targeting is not equivalent to physical engagement or autonomous action.** Selecting, tracking, highlighting, analyzing, or describing an object of interest does not authorize VIGIL to act against it.

---

## 5. Prohibited Autonomous Authority

VIGIL must not independently:

- initiate consequential physical action against a person, object, or environment;
- control or command a weapon or other harmful physical mechanism;
- generate or provide firing solutions or ballistic control outputs for weapon use;
- autonomously select a weapon or physical means of engagement;
- autonomously trigger, fire, strike, disable, or otherwise physically affect a target;
- treat a detection, track, fused estimate, AI inference, or priority score as authorization for physical action; or
- delegate consequential physical-action authority to an AI model, voice interface, presentation layer, or external integration on VIGIL's behalf.

Weapon-control, firing, ballistic, trigger, and actuator interfaces are outside VIGIL's architecture.

This boundary applies regardless of whether the information was produced deterministically, probabilistically, or with AI assistance.

---

## 6. Information Is Not Authority

VIGIL must preserve the distinction between information and authority.

```text
Observation
    ↓
Detection
    ↓
Track
    ↓
Fusion
    ↓
World Model
    ↓
Analysis
    ↓
Relevance / Priority
    ↓
Presentation / Communication
    ↓
Human Interpretation
    ↓
Human Decision
    ↓
Human Action
```

No stage before the human decision may silently acquire authority that belongs to the human.

In particular:

- confidence is not authorization;
- priority is not authorization;
- identity or association is not authorization;
- AI intent interpretation is not authorization;
- a voice command is not automatically authorization for every capability;
- an API key is not equivalent to human authorization; and
- an external consumer receiving VIGIL information does not receive physical-action authority from VIGIL.

---

## 7. Human Interaction and Voice

The Human Interaction & Voice Interface is a communication channel within this boundary.

Its purpose is to allow a person to communicate with VIGIL and receive information from VIGIL through text and optional speech.

The interaction layer may:

- accept questions and requests;
- query current and historical VIGIL state;
- explain observations and analysis;
- report system and environmental status;
- summarize activity over a selected period;
- change supported non-consequential presentation settings; and
- request supported information services.

The interaction layer does not gain independent physical-action authority through natural-language interpretation, speech recognition, AI assistance, or conversational context.

See `docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md` for the detailed communication contract.

---

## 8. Proactive Information and Attention

VIGIL may proactively communicate information when authorized by the product's attention and notification policy.

For example, VIGIL may tell a user that:

- a high-priority change occurred;
- an object of interest changed movement state;
- sensor coverage degraded;
- an area changed significantly; or
- a condition deserves human attention.

A proactive notification is still information presentation. It does not authorize VIGIL to take consequential physical action.

The Relevance & Priority Engine determines attention priority, not physical-action authority.

---

## 9. External Integrations

VIGIL may provide structured information, events, notifications, navigation/search/inspection requests, and other authorized outputs to external systems.

An external system may have capabilities that VIGIL itself does not have. Those capabilities must remain under the external system's own explicit authorization, safety, and control architecture.

VIGIL must not represent an external system's physical action as an autonomous VIGIL action or silently delegate VIGIL's human-decision boundary to that system.

The integration boundary is therefore:

```text
VIGIL World Model / Services
            ↓
 Information & Event Output
            ↓
   Authorized External System
            ↓
External authorization / safety / control
```

The external system's authority does not become VIGIL's authority merely because VIGIL supplied information to it.

---

## 10. AI Boundary

AI may assist VIGIL with information analysis, natural-language interpretation, summarization, explanation, correlation, and conversational interaction.

AI must not own:

- authoritative physical-world truth;
- authorization policy;
- credential ownership;
- consequential physical-action authority; or
- the decision to independently act against an object, person, or environment.

AI-generated content must preserve the distinction between observation, inference, recommendation, interpretation, and unknown information.

An AI system cannot promote itself from an information-analysis role to a physical-action authority.

---

## 11. Security and Authorization

The autonomy boundary is enforced through architecture, not merely through user-interface wording.

Authorization must be evaluated independently of perception and interpretation.

At minimum, systems should distinguish:

```text
Source / Device Identity
        ↓
Service Authentication
        ↓
User / Session Identity
        ↓
Requested Capability
        ↓
Authorization Scope
        ↓
Allowed Information Operation
```

No credential, transcript, API key, or interpreted intent should silently bypass the human-decision boundary.

---

## 12. Testing Requirements

The boundary must be testable.

Tests shall verify at minimum that:

1. a detected object cannot cause an autonomous physical-action request;
2. a tracked object cannot cause an autonomous physical-action request;
3. a high-priority object cannot cause an autonomous physical-action request;
4. an AI-generated recommendation cannot cause an autonomous physical-action request;
5. a voice transcript cannot bypass authorization;
6. a natural-language intent cannot become permission merely by being interpreted;
7. API authentication cannot be treated as unlimited user authorization;
8. presentation and attention components cannot invoke prohibited physical-action interfaces;
9. external integrations cannot silently become VIGIL physical-action authorities; and
10. generic spatial targeting remains informational and separate from prohibited physical-action capabilities.

Negative tests are especially important: prohibited interfaces should be absent, inaccessible, or rejected at the appropriate architectural boundary rather than merely hidden from the user interface.

---

## 13. Architectural Invariants

The following rules are mandatory:

1. **VIGIL observes, understands, organizes, and presents; the human decides and acts.**
2. **VIGIL is an information and presentation system, not an autonomous physical-action system.**
3. **Generic spatial targeting is informational and does not imply weapon control or physical engagement.**
4. **An object of interest may be observed, tracked, analyzed, prioritized, and discussed without becoming an autonomous-action target.**
5. **Information, confidence, priority, and prediction are not authorization.**
6. **Natural-language interpretation is not permission.**
7. **Voice interaction does not change the autonomy boundary.**
8. **AI does not gain physical-action authority.**
9. **External integrations do not inherit authority from VIGIL.**
10. **Consequential physical action remains outside VIGIL's autonomous authority and under human decision.**

---

## 14. Relationship to Other Contracts

This document establishes the top-level human-decision and autonomy boundary.

Related contracts define how VIGIL implements specific information capabilities within that boundary:

- `docs/technical/HUMAN-INTERACTION-VOICE-CONTRACT.md` — human↔VIGIL communication through text and optional voice.
- `docs/technical/SPATIAL-TEMPORAL-FUSION-CONTRACT.md` — deterministic spatial/temporal evidence fusion.
- `docs/technical/TRACK-WORLD-MODEL-CONTRACT.md` — controlled Track-to-World-Model boundary.
- `docs/technical/SPATIAL-WORLD-MODEL.md` — authoritative spatial world-state model.

None of these contracts may weaken or override the autonomy and human-decision boundary defined here.
