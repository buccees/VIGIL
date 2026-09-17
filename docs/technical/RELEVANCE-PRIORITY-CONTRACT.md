# VIGIL Relevance / Priority Contract

**Document:** Relevance / Priority Contract  
**Version:** 0.1  
**Status:** Initial implementation contract  
**Parent:** VIGIL System Architecture

## 1. Purpose

This contract defines the deterministic Relevance / Priority layer between the authoritative Spatial World Model and the later Presentation / Attention layer.

The layer answers two different questions:

- **Relevance:** how useful modeled information is to the user's current context or task.
- **Priority:** which relevant information should receive attention first when attention is limited.

Neither concept changes what is true in the World Model.

## 2. Architectural Boundary

The processing boundary is:

```text
Authoritative World Model
        |
        v
Relevance / Priority Engine
        |
        v
Prioritized Information
        |
        v
Presentation / Attention
```

The engine is a consumer of authoritative world state. It SHALL NOT mutate World Model entities, tracks, detections, fusion estimates, provenance, confidence, uncertainty, lifecycle, validity, or freshness.

It SHALL NOT create authoritative world entities or resolve Track ID, Fusion association ID, or World Entity ID.

## 3. Relevance Is Not Truth

Relevance is contextual usefulness, not truth, confidence, or certainty.

A low-relevance entity may remain fully valid and must remain available in the World Model. A highly relevant item may still have low confidence or high uncertainty.

The engine SHALL preserve those source properties rather than converting them into relevance or priority.

## 4. Priority Is Not Confidence

Priority expresses attention ordering under the active context. Confidence expresses evidentiary support.

The engine SHALL NOT use confidence as a substitute for priority. Confidence MAY be one explicit factor when a future policy requires it, but any such use must remain separately represented and explainable.

Uncertainty likewise SHALL remain distinct from priority.

## 5. Context

Priority evaluation SHALL be performed against an explicit context rather than hidden global state.

A context may contain:

- reference position when available;
- active task or mode identifier;
- selected entity or area when applicable;
- relevant zones or geographic scope;
- current evaluation time;
- configurable policy parameters.

Missing context SHALL be represented as unavailable. The engine SHALL use deterministic documented fallback behavior rather than inventing context.

## 6. Initial Priority Factors

The initial deterministic implementation SHALL support explicit, independently inspectable factors for:

- spatial proximity;
- movement or change rate;
- recent state change;
- zone relationship;
- active task relevance;
- unexpected appearance or disappearance;
- persistence or recurrence.

Not every factor must be available for every entity. Unavailable factors SHALL be treated according to explicit policy and SHALL NOT silently become favorable or unfavorable evidence.

Advanced machine-learned ranking, probabilistic prioritization, personalization models, and opaque AI ranking are outside this initial contract.

## 7. Determinism

Given identical World Model state, context, policy, and evaluation time, the engine SHALL produce the same relevance and priority result.

Tie-breaking SHALL be deterministic and SHALL NOT depend on collection iteration order, hash order, thread scheduling, or wall-clock time beyond the explicitly supplied evaluation time.

## 8. Explainability

Every priority result SHALL identify the factors that contributed to the result and their effective values or states.

A consumer must be able to distinguish:

- why an item was considered relevant;
- which factors increased or decreased priority;
- which factors were unavailable;
- the resulting priority value or ordered priority representation.

The explanation is diagnostic information. It SHALL NOT be represented as authoritative world truth.

## 9. Preservation of Low-Priority Information

Priority is an attention-management input, not a deletion mechanism.

Low-priority or irrelevant information SHALL remain available in the authoritative World Model subject to that model's existing validity and retention policies.

The Relevance / Priority layer SHALL NOT delete, invalidate, stale, suppress from history, or otherwise mutate World Model information merely because its priority is low.

## 10. Validity and Freshness

The engine SHALL respect World Model validity and freshness state.

Invalid information SHALL NOT be promoted to a normal high-priority result as though it were current valid information.

Stale information may remain useful for context or history, but its stale state SHALL remain visible in the result and SHALL be handled by explicit policy.

Unknown and unavailable values SHALL remain distinguishable from measured zero, false, or low priority.

## 11. Output Model

The initial output SHALL be a derived, immutable prioritization result containing at minimum:

```text
PriorityResult
├── worldEntityId
├── relevance
├── priority
├── evaluationTime
├── contributingFactors
├── unavailableFactors
└── sourceStateSummary
```

`worldEntityId` is a reference to an existing World Entity. It is not a new identity domain.

The result MAY include the source entity's confidence, uncertainty, validity, and freshness as contextual state, but those values SHALL retain their original meanings.

## 12. Ordering

The engine SHALL provide deterministic ordering for a collection of eligible results.

Ordering SHALL use the explicit priority representation first, followed by a documented stable tie-breaker such as World Entity ID.

The ordering is a presentation input only. It SHALL NOT alter World Model storage order or entity state.

## 13. Failure and Missing Context

If required context is unavailable, the engine SHALL return a deterministic degraded result or explicit unavailable-context result according to policy.

It SHALL NOT fabricate a user location, task, zone membership, confidence, or other missing input.

If an individual factor cannot be evaluated, the result SHALL record that factor as unavailable and continue only when the policy permits.

## 14. Policy Separation

Scoring policy SHALL be explicit and replaceable.

The engine implementation SHALL separate:

1. acquisition of World Model state;
2. context representation;
3. factor evaluation;
4. priority policy;
5. explanation construction; and
6. deterministic ordering.

This prevents presentation requirements from becoming hidden world-state logic.

## 15. Prohibited Responsibilities

The Relevance / Priority layer SHALL NOT:

- mutate authoritative World Model state;
- create or delete World Entities;
- change confidence or uncertainty;
- resolve identity;
- perform Fusion;
- rewrite provenance;
- authorize physical action;
- control actuators;
- silently discard low-priority information;
- make opaque AI decisions that cannot be explained by the active policy.

## 16. Testing Requirements

The initial implementation SHALL include deterministic boundary tests covering at least:

- identical inputs produce identical results;
- proximity changes priority without changing truth or confidence;
- state changes affect priority without mutating World Model state;
- task relevance is represented independently from confidence;
- low-priority entities remain present in the World Model;
- invalid/stale state is not silently treated as current valid state;
- unavailable context produces the documented fallback;
- unavailable factors are observable;
- equal-priority results use deterministic tie-breaking;
- explanations identify contributing and unavailable factors.

## 17. Initial Scope and Deferred Work

The initial milestone is intentionally deterministic and explainable.

Deferred until a documented trigger requires them:

- learned ranking models;
- probabilistic priority distributions;
- user-specific adaptive ranking;
- multi-user competing-context optimization;
- opaque AI prioritization;
- presentation-specific salience calculations.

Any deferred capability entering implementation SHALL receive an explicit contract revision, implementation change, boundary tests, and CI verification.

## 18. Exit Condition

The initial Relevance / Priority milestone is complete when:

1. relevance and priority are explicitly distinct from truth, confidence, uncertainty, and Fusion;
2. context and policy are explicit inputs;
3. priority factors are deterministic and explainable;
4. unavailable information is handled explicitly;
5. low-priority information remains in authoritative World Model state;
6. results use World Entity references without creating a new identity authority;
7. boundary behavior is covered by tests; and
8. CI verifies the implementation.
