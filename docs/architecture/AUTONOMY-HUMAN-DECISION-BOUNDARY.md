# VIGIL Autonomy & Human Decision Boundary

**Version:** 0.2  
**Status:** Approved for implementation  
**Approval date:** 2026-09-02

This contract defines the hard boundary between VIGIL's information-processing capabilities and human decision/action. VIGIL is an information, perception, spatial-intelligence, and presentation system. It does not possess independent authority to perform consequential physical actions.

## Core Principle

> **VIGIL observes, understands, organizes, and presents. The human decides and acts.**

VIGIL may acquire information, detect and track entities, fuse evidence, maintain a spatial world model, analyze and prioritize information, communicate with people, and perform authorized software-level operations. The human remains the final interpreter, decision-maker, and actor for consequential physical activity.

## Human-in-the-Loop Boundary

```text
             ENVIRONMENT
                  │
                  ▼
        ┌───────────────────┐
        │       VIGIL       │
        │                   │
        │ Observe           │
        │ Detect            │
        │ Track             │
        │ Fuse              │
        │ Model             │
        │ Analyze           │
        │ Prioritize        │
        │ Present           │
        └─────────┬─────────┘
                  │
                  ▼
               HUMAN
                  │
          ┌───────┴───────┐
          │               │
       Interpret        Decide
          │               │
          └───────┬───────┘
                  ▼
                ACT
```

The transition from VIGIL information to consequential human action is an architectural boundary, not merely a user-interface convention.

## Allowed VIGIL Functions

VIGIL may:

- acquire observations from authorized sensors and data sources;
- perform detection and perception;
- maintain track continuity and track state;
- perform spatial and temporal fusion;
- maintain the Spatial World Model;
- provide spatial search, navigation, inspection, environmental awareness, and other information services;
- support generic spatial targeting of destinations, waypoints, POIs, landmarks, detected objects, world entities, areas, or user-defined locations;
- estimate location, distance, bearing, direction, movement, and related spatial context;
- manage relevance, priority, attention, and presentation;
- communicate through text and optional voice input/output;
- provide grounded summaries, explanations, recommendations, and environmental status information;
- perform human-authorized software-level operations within defined permissions;
- provide information to authorized external consumers and integrations.

## Generic Spatial Targeting Boundary

Generic spatial targeting means selecting, locating, following, or presenting a spatial objective such as a destination, waypoint, point of interest, landmark, detected object, world entity, area, or user-defined location.

It may expose information such as location, distance, bearing, direction, movement, context, and history. Generic spatial targeting is independent of weapon control or physical engagement.

The following are outside the VIGIL architecture:

- weapon-control interfaces;
- firing solutions;
- ballistic calculations for weapon use;
- automated weapon selection;
- automated physical engagement;
- trigger control;
- actuator interfaces used to cause consequential physical action.

## Prohibited Autonomous Authority

VIGIL must not independently:

- perform consequential physical action;
- control or operate a weapon;
- generate or execute firing or ballistic solutions for weapon use;
- select or engage a physical target as an autonomous actor;
- control triggers or physical actuators for consequential action;
- treat confidence, priority, AI output, tracking state, or any other internal state as authorization for consequential physical action;
- delegate consequential physical authority to an AI subsystem, voice interface, presentation layer, or external integration.

## Information Is Not Authority

A VIGIL output may be highly confident, highly relevant, urgent, or strongly recommended without becoming an authorization to act.

Confidence describes support for an information claim. Priority describes information importance to the user. Neither grants physical authority.

## Human Interaction and Voice

The Human Interaction / Voice Interface is part of the human decision boundary.

A person may ask VIGIL questions, request views or summaries, navigate information, or initiate other authorized software-level operations through text or voice. VIGIL may answer textually or verbally.

Voice input does not grant VIGIL independent authority. Speech recognition, natural-language interpretation, API authentication, session identity, and authorization remain distinct concerns.

## Proactive Information and Attention

VIGIL may proactively present information when configured to do so. It may elevate information because it is relevant, changing, unusual, nearby, persistent, or otherwise important to the user's task.

Proactive presentation remains informational. Alerting or prioritization does not constitute authorization for consequential action.

## External Integrations

VIGIL may provide authorized information to external systems through defined integration boundaries. An external consumer is responsible for its own permissions and action authority.

An integration must not be treated as a mechanism for bypassing VIGIL's human-decision boundary.

## AI Boundary

AI may assist with information analysis, natural-language interaction, summarization, explanation, query interpretation, and other bounded information tasks.

AI is not the authority for physical truth, authorization, or consequential physical action. AI output must remain subject to the same human-decision boundary as deterministic VIGIL output.

## Security and Authorization

Authentication and authorization must be explicit and auditable where applicable. Device authentication, API credentials, user/session identity, microphone permission, and authorization for requested operations are separate concepts.

Possession of an API credential does not by itself establish permission for every VIGIL operation.

## Testing Requirements

The implementation must include tests demonstrating that:

1. information processing can operate without physical-action authority;
2. confidence and priority cannot authorize physical action;
3. voice and AI interfaces remain inside the human-decision boundary;
4. generic spatial targeting remains independent of weapon-control functions;
5. external integrations cannot silently bypass authorization boundaries;
6. consequential physical actions are not exposed as VIGIL capabilities.

## Architectural Invariants

The following are invariants of the VIGIL architecture:

- VIGIL is an information/presentation system, not an autonomous physical-action system.
- VIGIL may observe, understand, organize, analyze, prioritize, and present information.
- The human remains responsible for consequential decisions and physical actions.
- Information quality does not create physical authority.
- AI does not create physical authority.
- Voice does not create physical authority.
- Generic spatial targeting does not imply weapon control.
- External integrations do not bypass the human-decision boundary.

## Relationship to Other Contracts

This contract is read together with:

- `VIGIL-ARCHITECTURE-SPEC.md`
- `HUMAN-INTERACTION-VOICE-CONTRACT.md`
- `SPATIAL-WORLD-MODEL.md`
- `TRACK-WORLD-MODEL-CONTRACT.md`
- `SPATIAL-TEMPORAL-FUSION-CONTRACT.md`
- `DOCUMENTATION-RULES.md`

**Approval:** This contract has completed architectural review and is approved as the implementation boundary for VIGIL autonomy and human decision/action.