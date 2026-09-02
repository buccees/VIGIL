# VIGIL Human Interaction & Voice Interface Contract

**Version:** 0.2  
**Status:** Approved for implementation  
**Approval date:** 2026-09-02

## 1. Purpose

The Human Interaction / Voice Interface provides a deliberate communication channel between people and VIGIL. It allows users to ask questions, request information, receive explanations and summaries, and perform authorized software-level interactions through text or optional speech.

This interface does not change VIGIL's autonomy boundary. VIGIL remains an information and presentation system; the human remains responsible for consequential decisions and physical actions.

## 2. Communication Modes

VIGIL supports the following interaction modes:

- text input;
- text output;
- optional microphone-based speech input;
- optional verbal output through a speaker or headset;
- conversational context across related requests when enabled.

Implementations may support any subset of these modes, provided the same authorization and human-decision boundaries apply.

## 3. Architecture Boundary

```text
              HUMAN
                │
        ┌───────┴────────┐
        │ Text / Speech  │
        └───────┬────────┘
                ▼
        ┌─────────────────┐
        │ Human Interaction│
        │    Interface     │
        ├─────────────────┤
        │ Auth / Session   │
        │ Speech Recognition│
        │ Intent / Query   │
        │ Context          │
        │ Authorization    │
        └────────┬────────┘
                 │
                 ▼
              VIGIL
        ┌─────────────────┐
        │ World State      │
        │ History          │
        │ Spatial Services │
        │ Analysis         │
        └────────┬────────┘
                 │
                 ▼
        ┌─────────────────┐
        │ Response /       │
        │ Presentation     │
        └────────┬────────┘
                 ▼
          Text / Voice
                 │
                 ▼
              HUMAN
```

Human interaction is bidirectional information exchange. It is not a physical-action path.

## 4. Spoken Input Pipeline

When speech input is enabled, the conceptual flow is:

```text
Microphone
   ↓
Speech Capture
   ↓
Speech Recognition
   ↓
Structured HumanInteractionRequest
   ↓
Authentication / Session Validation
   ↓
Authorization
   ↓
Query / Software Operation
   ↓
Grounded VIGIL Response
```

Speech recognition and speaker identity are separate concerns. Voice biometrics must not be assumed merely because speech recognition is available.

## 5. HumanInteractionRequest Semantics

A structured interaction request should preserve, as applicable:

- request identifier;
- user/session identity;
- authentication state;
- authorization context;
- input modality;
- recognized text or structured query;
- conversational context;
- requested operation or information need;
- requested scope, area, entity, or time range;
- request timestamp;
- provenance sufficient to explain how the request was interpreted.

The exact wire format is implementation-specific; the semantic fields are not.

## 6. What VIGIL May Say

VIGIL may communicate:

- current spatial/environmental state;
- changes and notable events;
- track and entity information;
- historical observations and world-state history;
- confidence and uncertainty;
- sensor health and data quality;
- explanations of why information was prioritized;
- grounded recommendations;
- navigation and spatial guidance information;
- summaries of selected areas, time periods, or entities;
- operational and environmental status.

VIGIL must distinguish observed or estimated information from interpretation or recommendation.

## 7. “What Kind of Day Is VIGIL Having?”

VIGIL may provide a natural-language operational/environmental summary describing what it has observed and processed over a selected period.

A day/session summary may include:

- observation volume;
- detection volume;
- track activity;
- environmental change rate;
- notable events;
- recurring patterns;
- sensor health and data quality;
- uncertainty or degraded coverage;
- user-requested areas or subjects;
- deviations from established baselines.

Such a summary must be grounded in recorded VIGIL state and history rather than invented narrative.

## 8. Grounded Natural-Language Queries

Examples include:

- “What changed in this area?”
- “Show me everything moving nearby.”
- “Where was that object five minutes ago?”
- “Navigate me to the nearest exit.”
- “What have you seen today?”
- “How has this area changed since this morning?”
- “Are any sensors degraded?”
- “Why did you prioritize that?”

The response must be grounded in available world state, history, provenance, freshness, and uncertainty.

## 9. Authorization and API Keys

VIGIL may use API credentials for authenticated service access. API authentication and user authorization are distinct.

An API key proves or enables access to an API boundary; it does not automatically establish that a particular user is authorized for every requested VIGIL operation.

Authorization should be evaluated for the requested operation, data scope, and session context.

Credentials must be handled securely and must not be exposed through ordinary conversational output.

## 10. Human Authorization Boundary

The interface may support human-authorized software-level operations such as changing views, requesting reports, selecting an area of interest, starting an allowed analysis, or configuring presentation behavior.

Voice or text input does not grant VIGIL independent authority to perform consequential physical action.

The autonomy boundary is defined separately in `AUTONOMY-HUMAN-DECISION-BOUNDARY.md` and is normative.

## 11. Conversational Context

When conversational context is enabled, VIGIL may retain relevant prior interaction state so that follow-up questions can be interpreted correctly.

Context must not silently broaden authorization. A previous authorized request does not automatically authorize a new operation outside the current session or permission scope.

## 12. Voice Output

When verbal output is enabled, VIGIL may communicate:

- alerts and notable changes;
- environmental summaries;
- current system/data status;
- answers to user questions;
- navigation and spatial information;
- explanations and recommendations;
- confirmation or failure of authorized software-level operations.

Speech output should identify uncertainty or degraded information when it materially affects the answer.

## 13. Freshness and Grounding

Responses about current state must account for observation age and data freshness.

VIGIL must not present stale, missing, or uncertain information as current fact. When the requested answer cannot be established reliably, VIGIL should communicate the limitation rather than inventing an answer.

## 14. Provenance and Auditability

Where appropriate, responses should be traceable to the structured VIGIL state that supports them.

The system should preserve sufficient provenance to explain:

- which world-state entities or events informed the answer;
- which observations, detections, tracks, or fused estimates contributed;
- the relevant timestamps;
- source and sensor information where available;
- confidence and uncertainty;
- interpretation or transformation applied by analysis components.

## 15. Microphone Privacy

Microphone access is optional and permission-controlled.

Implementations must make speech capture state clear to the user and should minimize retention of raw audio. Speech processing should operate only when permitted and configured.

Speech data must not be treated as silently available environmental data.

## 16. Offline-First and Provider Independence

The interaction layer should not require a specific cloud speech or AI provider as an architectural dependency.

Offline-capable speech recognition, local processing, and local response generation may be used where supported. Provider-specific implementations must preserve the same request, authorization, grounding, and autonomy semantics.

## 17. AI Boundary

AI may assist with:

- natural-language understanding;
- query interpretation;
- summarization;
- explanation;
- conversational response generation;
- bounded information analysis.

AI output must be grounded in structured VIGIL state where the answer concerns VIGIL's environment or operational state.

AI does not become the source of physical truth, authorization, or consequential physical action merely because it interprets or generates language.

## 18. Failure Handling

The interface must handle, explicitly where applicable:

- speech recognition failure;
- ambiguous requests;
- unavailable world-state information;
- stale data;
- authorization failure;
- authentication failure;
- unavailable analysis providers;
- degraded sensors;
- unavailable voice output.

Failure should be communicated clearly and should not be silently converted into fabricated certainty.

## 19. Testing Requirements

The implementation must include tests demonstrating that:

1. text and voice requests are represented as structured human interactions;
2. authentication and authorization remain distinct;
3. conversational context does not silently expand permissions;
4. responses are grounded in available VIGIL state/history;
5. freshness and uncertainty are preserved in responses;
6. AI cannot bypass the human-decision boundary;
7. voice cannot bypass the human-decision boundary;
8. microphone access is permission-controlled;
9. software-level operations respect authorization;
10. unavailable or ambiguous information produces an explicit limitation rather than fabricated certainty.

## 20. Initial Implementation Strategy

Initial implementation should proceed in bounded stages:

1. define the structured request/response model;
2. implement text query and response paths;
3. connect queries to structured World Model and history;
4. implement authorization/session handling;
5. add optional speech recognition;
6. add optional verbal output;
7. add conversational context;
8. add grounded AI assistance where useful;
9. validate provenance, freshness, failure handling, and autonomy invariants.

## 21. Architectural Invariants

- Human interaction is a first-class VIGIL subsystem.
- Text and voice are communication modalities, not autonomy mechanisms.
- VIGIL may communicate proactively or in response to requests.
- VIGIL may explain what it is observing, what changed, what it knows, and where uncertainty exists.
- VIGIL may summarize its operational/environmental state and describe “what kind of day” it is having when grounded in recorded state.
- Authentication, authorization, and conversational context are distinct concerns.
- API credentials do not automatically grant unrestricted user authority.
- AI may assist communication and analysis but does not gain physical authority.
- Voice does not gain physical authority.
- Current-state answers must respect data freshness and uncertainty.
- Human users remain responsible for consequential decisions and physical actions.

## 22. Relationship to Other Contracts

This contract is read together with:

- `VIGIL-ARCHITECTURE-SPEC.md`
- `AUTONOMY-HUMAN-DECISION-BOUNDARY.md`
- `SPATIAL-WORLD-MODEL.md`
- `TRACK-WORLD-MODEL-CONTRACT.md`
- `SPATIAL-TEMPORAL-FUSION-CONTRACT.md`
- `DOCUMENTATION-RULES.md`

**Approval:** This contract has completed architectural review and is approved for implementation as the normative Human Interaction and Voice Interface boundary for VIGIL.