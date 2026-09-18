# VIGIL Attention / Presentation Contract

**Version:** 1.0  
**Status:** Initial deterministic milestone contract  
**Purpose:** Define the boundary between derived Relevance / Priority results and user-facing Attention / Presentation state.

## 1. Scope

The Attention / Presentation layer consumes derived priority information and produces presentation state suitable for downstream device, headset, text, visual, or audio interfaces.

It does not establish environmental truth.

The architectural flow is:

`World Model → Relevance / Priority → Attention / Presentation → Human Interaction / Voice`

## 2. Authority Boundary

The World Model remains authoritative for modeled environmental state.

Relevance / Priority remains authoritative only for its derived evaluation result.

Attention / Presentation owns presentation state only.

Presentation state MUST NOT:

- mutate or delete World Entities;
- change World Entity confidence, validity, freshness, provenance, Track ID, or Fusion association;
- invent environmental entities or facts;
- reinterpret a low-priority result as an absent World Entity;
- become an alternate environmental state store.

## 3. Presentation Input

An attention item MUST retain a reference to its source World Entity ID and the derived priority result from which the presentation decision was made.

The initial milestone should preserve, at minimum:

- World Entity ID;
- relevance;
- priority;
- evaluation time;
- source validity;
- source freshness;
- source confidence;
- contributing priority factors;
- unavailable priority factors.

Presentation MAY add presentation-specific metadata, but it must not overwrite the source-derived values.

## 4. Attention Item Lifecycle

The initial deterministic implementation should define explicit lifecycle states:

- **NEW** — eligible information has entered attention management.
- **ACTIVE** — currently presented or eligible for immediate presentation.
- **ACKNOWLEDGED** — the human or presentation policy has acknowledged the item without changing its source truth.
- **EXPIRED** — the presentation item's retention window has ended.
- **DISMISSED** — presentation has been intentionally ended without modifying source state.

Lifecycle transitions MUST be deterministic and observable.

A lifecycle transition MUST NOT imply that the corresponding World Entity was removed, resolved, or changed.

## 5. Deterministic Ordering

When multiple attention items are eligible for presentation, ordering MUST be deterministic.

The initial ordering key is:

1. descending priority;
2. descending relevance;
3. ascending World Entity ID.

No arrival-order or hash-order behavior may determine the result.

## 6. Replacement and Persistence

Attention management MUST distinguish between:

- a new item;
- an updated evaluation for an existing World Entity;
- replacement of presentation content; and
- expiration of presentation state.

A newer evaluation for the same World Entity may update its presentation item without creating duplicate authoritative entities.

Low-priority information may remain in the World Model even when it is not selected for presentation.

## 7. Presentation Policy

Presentation policy determines whether and how an attention item is surfaced.

The initial deterministic milestone should support explicit policy inputs rather than hidden heuristics.

Policy decisions MAY include:

- minimum priority threshold;
- maximum active items;
- persistence duration;
- acknowledgment behavior;
- replacement behavior.

Policy MUST be replaceable and testable independently from source World Model state.

## 8. Failure and Missing Context

Missing or unavailable source information MUST remain explicit.

Attention management MUST NOT fabricate missing priority factors or source state.

If a source priority result is invalid for presentation, the attention item may be rejected or expired according to explicit policy, but the source World Entity remains untouched.

## 9. Human Boundary

Presentation is an information-delivery layer.

It may organize, persist, suppress, replace, acknowledge, or expire presentation items according to explicit policy.

It MUST NOT autonomously act on the environment or imply that presentation selection constitutes a decision made by VIGIL.

The human remains the decision-maker.

## 10. Determinism and Testing

The initial implementation MUST include boundary tests covering at least:

- identical inputs produce identical attention state;
- priority ordering is deterministic;
- World Entity ID provides a stable final tie-break;
- lifecycle transitions are deterministic;
- repeated evaluation updates an existing attention item rather than duplicating it;
- expiration affects presentation state only;
- acknowledgment affects presentation state only;
- low-priority items remain in the World Model;
- source confidence, validity, freshness, and provenance remain unchanged;
- unavailable source factors remain explicit.

## 11. Initial Exit Condition

Attention / Presentation is complete for the initial deterministic milestone when:

1. this contract is implemented;
2. presentation state is explicitly separated from World Model state;
3. attention lifecycle behavior is deterministic;
4. ordering and replacement are deterministic;
5. source identity and provenance are preserved;
6. boundary tests pass;
7. CI verifies the implementation; and
8. correction history is recorded in `docs/architecture/PROJECT-HANDOFF.md`.

## Core Principle

> **Attention decides what is presented; it does not decide what is true.**

This contract preserves the VIGIL boundary:

> **VIGIL observes, understands, organizes, and presents information. The human decides and acts.**
