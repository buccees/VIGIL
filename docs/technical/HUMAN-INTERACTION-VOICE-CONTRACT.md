# VIGIL Human Interaction & Voice Interface Contract

**Project:** VIGIL  
**Document version:** 0.1  
**Status:** Proposed for implementation review  
**Audience:** Project developers, reviewers, maintainers, and future contributors

---

## 1. Purpose

VIGIL must support a deliberate human↔system communication channel. A person must be able to communicate with VIGIL, and VIGIL must be able to communicate useful information back to that person.

The Human Interaction layer is therefore a first-class architectural subsystem, not a UI convenience.

The interaction model supports:

- textual input from a user;
- textual output from VIGIL;
- optional spoken input through a microphone;
- optional spoken output through a speaker, headset, or other audio interface;
- conversational context where appropriate;
- queries against structured VIGIL state and history;
- explanations and summaries of VIGIL's observations and analysis; and
- human-authorized software-level interaction with supported VIGIL functions.

The interface does not change VIGIL's autonomy boundary. VIGIL communicates and presents information; the human remains responsible for consequential decisions and physical action.

---

## 2. Architectural Boundary

The Human Interaction layer sits beside the core spatial pipeline and communicates with structured VIGIL state through defined interfaces.

```text
                 VIGIL SPATIAL INTELLIGENCE

Sensors → Observations → Detections → Tracks → Fusion
                                                ↓
                                           World Model
                                                ↓
                                    Spatial / Environmental
                                           Services
                                                ↓
                                  Relevance / Priority
                                                ↓
                                     Presentation Layer
                                                ↓
                                               USER
                                                ↕
                              HUMAN INTERACTION / VOICE
```

The interaction layer may request information from the World Model, services, history, and approved analysis components. It must not become an alternative source of spatial truth.

---

## 3. Communication Modes

VIGIL shall support two independently selectable output modes:

### 3.1 Textual

VIGIL may communicate through text displayed by a device, headset UI, application, terminal, or other authorized presentation surface.

Text is the canonical representation of a response where practical because it is inspectable, searchable, and suitable for logs and accessibility.

### 3.2 Verbal

VIGIL may optionally convert an approved textual response into speech using a text-to-speech service or local speech synthesizer.

Speech is an alternate presentation channel, not a separate source of system truth.

The user must be able to enable or disable spoken output without disabling textual communication.

---

## 4. Spoken Input

When enabled and authorized, the voice-input path is:

```text
Microphone
   ↓
Audio Capture
   ↓
Speech Recognition
   ↓
Transcript + Recognition Metadata
   ↓
Intent / Query Interpretation
   ↓
Authorization + Context Validation
   ↓
VIGIL Information Request
   ↓
Response
```

Speech recognition converts audio into text. It does not by itself establish who is speaking or authorize an operation.

If speaker identification or voice biometrics are later supported, that capability must be explicitly separated from speech recognition and treated as an authentication signal with its own confidence, enrollment, privacy, and authorization rules.

---

## 5. Human Interaction Semantics

A user request should be represented as a structured interaction request containing, where applicable:

- interaction/session identity;
- user identity or authenticated principal;
- device identity;
- input modality;
- original text or transcript;
- speech-recognition confidence when voice input was used;
- interpreted intent or query;
- relevant user context/task;
- requested VIGIL capability;
- authorization scope;
- request timestamp;
- world-state/history context timestamp; and
- provenance linking the response to the information used.

The interpretation layer may use deterministic parsing, a language model, or another analysis mechanism. Regardless of implementation, interpreted intent is not authorization and is not world-state truth.

---

## 6. What VIGIL May Say

VIGIL may communicate information derived from observations, tracks, fused state, world state, history, deterministic spatial services, and approved analysis.

Examples include:

- current environmental summaries;
- what VIGIL is observing;
- what changed recently;
- what has been persistent or recurring;
- the state or health of authorized sensors;
- spatial relationships and navigation information;
- historical observations and state changes;
- explanations of why information was prioritized;
- confidence, uncertainty, and data freshness;
- system status and processing conditions; and
- summaries of its current analytical workload or environmental assessment.

VIGIL may also communicate a human-readable description of its current operating condition — for example, that the environment is quiet, busy, changing rapidly, data quality is degraded, or several events require attention.

These statements must be grounded in available system state. VIGIL must not imply subjective feelings, consciousness, or experiences unless the product explicitly defines such language as a presentation metaphor.

---

## 7. “What Kind of Day Is VIGIL Having?”

The phrase "what kind of day is VIGIL having?" is treated as a request for an operational/environmental summary rather than a claim that VIGIL has human emotions.

A Day/Session Summary may synthesize measurable characteristics such as:

- observation volume;
- detection volume;
- track activity;
- rate of environmental change;
- notable events;
- recurring patterns;
- sensor health and data quality;
- uncertainty or degraded coverage;
- user-requested areas of interest; and
- notable differences from a defined baseline or earlier period.

For example, a response may characterize a session as **quiet**, **active**, **high-change**, **sensor-degraded**, or **unusually busy**, provided those labels are backed by explicit system metrics or defined rules.

The summary must distinguish observation from interpretation. A natural conversational voice is permitted, but the underlying claim must remain traceable to system state.

---

## 8. Questions and Queries

VIGIL should be able to answer natural-language questions by translating them into structured queries against authoritative state and history.

Examples:

- "What changed in this area?"
- "What are you seeing right now?"
- "Where was that entity five minutes ago?"
- "Which sensors are degraded?"
- "Why did you bring that to my attention?"
- "What is moving nearby?"
- "What happened here earlier?"
- "How confident are you about that estimate?"

A query response should preserve the distinction between:

- observed fact;
- derived spatial relationship;
- inference or analysis;
- recommendation or interpretation; and
- unknown/insufficient evidence.

---

## 9. Authorization and API Credentials

Authentication and authorization are separate concerns.

An API key may authenticate access to a service, but possession of an API key must not automatically grant unlimited user authority over VIGIL.

The architecture should distinguish:

```text
Device Identity
      ↓
Service/API Authentication
      ↓
User / Session Identity
      ↓
Requested Capability
      ↓
Authorization Scope
      ↓
Allowed VIGIL Operation
```

API credentials must be scoped, protected, rotatable, and excluded from client-side source code and ordinary logs. Long-lived provider secrets should remain on an authorized server/service boundary or protected device credential store rather than being embedded in a public client.

For browser or mobile voice integrations, use a server-mediated or short-lived session credential model where the external provider requires secret credentials. Do not expose provider master API keys to the client.

---

## 10. Human Authorization Boundary

Voice or text input does not bypass VIGIL's authorization model.

A natural-language request must be treated as a request to interpret and evaluate, not as automatic permission to perform any requested operation.

VIGIL may support human-authorized software-level operations such as:

- changing presentation preferences;
- selecting an area, entity, or navigation destination;
- requesting searches or inspections;
- requesting summaries or analysis;
- changing supported non-consequential configuration; and
- controlling VIGIL's information presentation.

VIGIL does not autonomously cause consequential physical action. Selecting, describing, highlighting, or analyzing an object of interest does not itself authorize physical action against it.

Any external system that can perform consequential action must have its own explicit authorization and safety boundary outside VIGIL's information/presentation core.

---

## 11. Conversational Context

VIGIL may maintain short-term conversational context so follow-up questions can refer to previously discussed entities, areas, time ranges, or results.

Context must remain bounded and inspectable enough to avoid silently changing the meaning of a request.

For example:

```text
User: What changed near the entrance?
VIGIL: Two detections changed from stationary to moving.
User: When did that start?
VIGIL: About three minutes ago, based on the earliest supporting track evidence.
```

Conversation context must not override current authoritative world state or authorization.

---

## 12. Voice Output Behavior

Spoken output should be concise enough for real-time use while retaining the ability to provide detail through text.

The system should support:

- selectable voice output on/off;
- interruption/barge-in where supported;
- queued versus immediate speech;
- prioritization of urgent information;
- suppression of redundant spoken messages;
- textual transcript of spoken responses; and
- explicit indication when a response is delayed by analysis or unavailable data.

Speech generation must not block the core spatial pipeline or prevent current information from being presented textually.

---

## 13. Freshness and Grounding

Conversational answers that describe current conditions must be grounded in the current World Model and must carry appropriate freshness semantics.

A response should not present stale information as current.

Where useful, responses should communicate qualifiers such as:

- "last observed";
- "currently estimated";
- "about two minutes old";
- "confidence is moderate"; or
- "sensor coverage is degraded."

---

## 14. Provenance and Auditability

Where practical, a conversational response should be traceable to the information used to produce it.

The provenance chain may include:

```text
Response
  ↓
Intent / Query
  ↓
World Model / History Query
  ↓
World Entity / Event / Service Result
  ↓
Track / Detection
  ↓
Observation / Source
```

Voice transcripts, interpreted requests, and generated responses should follow the applicable privacy, retention, and audit policies. Audio should not be retained merely because speech input was enabled.

---

## 15. Privacy and Microphone Use

Microphone access must be explicit and permission-controlled.

VIGIL must not covertly listen, record, transcribe, or retain audio outside the authorized interaction mode.

The implementation should make microphone state visible and distinguish at least:

- microphone unavailable;
- permission denied;
- microphone disabled;
- microphone active/listening;
- speech being processed; and
- speech input unavailable.

Audio retention, transcript retention, and conversational history retention must be separately controllable where practical.

---

## 16. Offline-First and Provider Independence

The Human Interaction layer should not require a single cloud provider.

Speech recognition and speech synthesis may be implemented through:

- local/on-device services;
- authorized remote services;
- interchangeable provider adapters; or
- future hardware/runtime services.

The VIGIL contract defines the interaction semantics and security boundaries, not a particular speech model or vendor.

---

## 17. AI Boundary

AI may assist with:

- natural-language interpretation;
- conversational responses;
- summarization;
- explanation;
- contextual analysis; and
- translation between natural language and structured queries.

AI does not own:

- sensor authorization;
- authoritative spatial truth;
- user authorization;
- credential management;
- physical-action authority; or
- the definition of whether an observation actually occurred.

AI-generated language must not conceal uncertainty or convert an inference into an asserted observation.

---

## 18. Failure Handling

The interaction layer must explicitly handle:

- unavailable microphone;
- denied microphone permission;
- speech-recognition failure;
- low-confidence or ambiguous transcript;
- unsupported language;
- unavailable TTS;
- API authentication failure;
- authorization failure;
- unavailable network service;
- stale world state;
- ambiguous entity or area references;
- insufficient evidence;
- long-running analysis; and
- conflicting information.

The preferred response to ambiguity is clarification rather than silent guessing.

---

## 19. Testing Requirements

Tests shall cover at minimum:

1. text request → structured query → textual response;
2. voice transcript → structured query → textual response;
3. textual response → optional speech output;
4. microphone permission denial;
5. speech-recognition failure and low-confidence input;
6. authentication failure;
7. authorization denial;
8. stale world-state response handling;
9. conversational follow-up context;
10. provenance from response to world-state evidence;
11. interruption/barge-in behavior where supported;
12. urgent versus low-priority spoken information;
13. provider failure/fallback behavior where multiple providers exist; and
14. confirmation that voice interaction cannot bypass the human authorization boundary.

---

## 20. Initial Implementation Strategy

The first implementation should establish interfaces and deterministic behavior before adding sophisticated conversational AI.

Recommended initial sequence:

1. Define `HumanInteractionRequest` and `HumanInteractionResponse` contracts.
2. Define text input/output adapters.
3. Define speech-to-text and text-to-speech provider interfaces.
4. Define authentication/session and authorization boundaries.
5. Add structured World Model query support for conversational requests.
6. Add a deterministic intent/query adapter for a small supported command vocabulary.
7. Add optional voice input/output adapters.
8. Add conversational context.
9. Add optional AI-assisted natural-language interpretation.
10. Add session/day summaries and grounded conversational status reporting.
11. Instrument interaction latency from speech input to useful response.

The core spatial pipeline must remain independent of these interaction implementations.

---

## 21. Architectural Invariants

The following rules are mandatory:

1. **VIGIL can communicate without becoming an autonomous physical-action system.**
2. **Text and speech are presentation channels over structured VIGIL information.**
3. **Speech recognition is not identity or authorization.**
4. **API authentication is not user authorization.**
5. **Natural-language intent is not permission.**
6. **AI interpretation is not authoritative world state.**
7. **Current-condition answers must respect data freshness.**
8. **Conversational summaries must remain grounded in measurable system state.**
9. **Voice input requires explicit microphone permission and visible interaction state.**
10. **Provider credentials must not be exposed in client-side code.**
11. **The human remains the final decision-maker for consequential action.**
12. **The interaction layer must not become a competing spatial-world authority.**

---

## 22. Definition of Done for the Contract

The contract is ready for implementation when the repository has:

- a stable human-interaction request/response model;
- explicit text and optional voice channels;
- authentication and authorization boundaries;
- provider-independent speech interfaces;
- grounded World Model query semantics;
- privacy and microphone-state handling;
- conversational context rules;
- provenance requirements; and
- tests covering the human authorization boundary.
