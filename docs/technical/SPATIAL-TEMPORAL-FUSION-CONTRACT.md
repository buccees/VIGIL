# Spatial / Temporal Fusion Contract

**Version:** 0.2  
**Status:** Active implementation / verification

## Purpose

Spatial / Temporal Fusion combines compatible evidence into a better-supported estimate while preserving uncertainty, provenance, disagreement, freshness, and data-quality information.

Fusion does **not** create physical truth. It must not silently turn incomplete, stale, invalid, or materially conflicting evidence into certainty.

## Scope

```text
Tracks / Compatible Evidence
        |
        v
Spatial / Temporal Fusion
        |
        v
Fused Estimate
        |
        v
WorldModelUpdater
        |
        v
World Model
```

Fusion is an estimation boundary. It must not directly mutate authoritative World Model state.

## Responsibilities

Fusion is responsible for:

- accepting structured perception evidence;
- evaluating temporal and spatial compatibility;
- establishing a common temporal and spatial interpretation before combining evidence;
- determining whether evidence can reasonably refer to the same modeled entity/state;
- combining compatible evidence under deterministic policy;
- preserving confidence, uncertainty, freshness, validity, and provenance;
- representing meaningful disagreement rather than hiding it through averaging;
- applying source/data-quality policy;
- producing deterministic results for identical valid inputs and configuration.

Fusion is not responsible for:

- directly mutating the authoritative World Model;
- deciding user-facing attention or presentation priority;
- performing advanced probabilistic multi-hypothesis tracking in the initial implementation;
- inventing missing measurements or unsupported certainty.

## Input Contract

Each evidence item should provide, where available:

- stable source/evidence identity;
- event/observation time;
- ingestion/processing time;
- coordinate-frame identity;
- spatial state;
- kinematics;
- classification/type;
- confidence;
- uncertainty;
- validity/quality state;
- freshness;
- provenance.

Missing values remain explicitly unavailable. Fusion must not interpret missing position, velocity, uncertainty, timestamp, or calibration information as zero, perfect certainty, or a default guess.

## Temporal Alignment

Fusion must distinguish environment event time from receipt/processing time. Temporal alignment must use a common time interpretation and a configured temporal policy.

The implementation must define behavior for:

- normal timestamp differences;
- allowable event-time skew;
- out-of-order evidence;
- stale evidence;
- future-dated evidence;
- missing or uncertain timestamps;
- interpolation/extrapolation, if supported;
- evidence that cannot be temporally aligned.

The initial implementation must not hard-code a universal timing threshold into the architecture contract. Timing thresholds belong to configuration/policy.

Temporally invalid evidence must not silently contribute as current evidence. If excluded for temporal incompatibility, the exclusion should have a structured diagnostic reason.

## Spatial Frame Alignment

Fusion inputs must have an explicit coordinate-frame interpretation.

The implementation may support frame relationships such as:

```text
Sensor Frame -> Device/Body Frame -> Local/World Frame -> Geographic Frame
```

A transform used for fusion must identify:

- source frame;
- destination frame;
- validity state;
- calibration/provenance where applicable;
- timestamp-consistent interpretation.

For the initial deterministic implementation, the supported transform is a translation between local Cartesian frames. Rotation, scale, and arbitrary nonlinear/geographic transforms remain outside this first-pass abstraction until the spatial primitives and calibration contracts require them.

Evidence with an unknown, missing, invalid, or incompatible transform must not be silently fused. Missing frame relationships are excluded as `INCOMPATIBLE_FRAME`; a known but invalid transform is excluded as `INVALID_TRANSFORM`. Both are observable through structured fusion diagnostics.

Transformed evidence must be compared in the selected fusion frame. The fused estimate must preserve transform provenance for every applied transform.

The current translation-only transform abstraction is applied consistently to frame-dependent spatial state. Position is translated directly; velocity passes through the same explicit transform boundary and remains unchanged by a pure translation.

## Calibration and Sensor Validity

Fusion should distinguish at least:

- healthy;
- degraded;
- stale;
- unavailable;
- invalid;
- calibration uncertain;
- timestamp uncertain.

A degraded source must not automatically invalidate all other compatible evidence. Degraded evidence may be reduced, rejected, or deferred according to explicit policy.

Calibration uncertainty should be distinguishable from measurement uncertainty when the data model supports that distinction.

## Association

Fusion must make association an explicit claim rather than an implicit assumption.

Association factors may include:

- spatial consistency;
- temporal consistency;
- motion consistency;
- type compatibility;
- source validity;
- expected measurement error;
- existing association state.

Fusion must not force incompatible evidence into the same entity merely because the observations are nearby.

Advanced probabilistic association and multi-hypothesis tracking are deferred capabilities.

## Fused State

A fused state should preserve, where supported:

- deterministic association reference;
- position;
- velocity;
- heading, if available;
- confidence;
- uncertainty;
- freshness/observation age;
- fusion timestamp;
- contributing sources/tracks/detections;
- consistency/quality metadata;
- transform provenance for any evidence converted into the fusion frame.

Fusion must not emit unsupported precision. If uncertainty is unavailable, it remains unavailable rather than becoming zero.

## Confidence and Uncertainty

Confidence and uncertainty are independent concepts.

- Confidence expresses trust in the evidence/result.
- Uncertainty expresses how precisely the physical state is known.

Fusion must not substitute one for the other.

Priority is outside this contract and belongs to a later relevance/priority layer.

## Conflicting Evidence

When compatible evidence materially conflicts, Fusion must use a deterministic, policy-driven response.

Acceptable outcomes include:

- a fused estimate with elevated uncertainty;
- a qualified subset;
- an explicitly unqualified deterministic subset;
- an unresolved association;
- no fused estimate.

Fusion must not blindly average materially inconsistent evidence. Provenance and quality metadata must explain the selected outcome.

`MATERIAL_DISAGREEMENT` is observable and does not mean that the entire fusion operation failed. The `qualified` field means that the result is suitable as a qualified result under policy; it does not mean that the result is conflict-free.

## Freshness

Fusion must preserve:

- source observation time;
- latest contributing evidence time;
- fusion processing time;
- current observation age.

An old but valid estimate may remain useful as historical/contextual state, but it must not appear to have been newly observed.

Loss of new evidence must not silently erase established identity.

## Provenance

Fusion results must retain a traceable path back to contributing evidence.

Where transforms are applied, transform provenance must identify the frame conversion used to place evidence into the fusion frame.

Rejected, excluded, or downweighted evidence should have a structured reason available to diagnostics.

Stable initial exclusion categories include:

- `INCOMPATIBLE_FRAME`;
- `INVALID_TRANSFORM`;
- `INCOMPATIBLE_TYPE`;
- `TEMPORAL_SKEW`;
- `STALE_OR_FUTURE_DATED`;
- `INVALID_SOURCE_STATE`;
- `OUTSIDE_CONFLICT_DISTANCE`;
- `MATERIAL_DISAGREEMENT`.

## Output Contract

Fusion may produce:

1. a fused estimate; and
2. zero or more structured exclusion diagnostics.

A valid fused estimate is suitable for the WorldModelUpdater boundary. Fusion itself must not mutate the authoritative World Model.

## Deterministic Processing Flow

```text
Evidence
   |
   v
Validate
   |
   v
Temporal Alignment
   |
   v
Spatial Alignment
   |
   v
Quality / Calibration
   |
   v
Association
   |
   v
Consistency
   |
   v
Fusion / Estimation
   |
   v
Attach Confidence / Uncertainty / Freshness / Provenance
   |
   v
Fused Estimate + Exclusion Diagnostics
   |
   v
WorldModelUpdater
   |
   v
World Model
```

## Invalid Input Rules

Evidence must be rejected, quarantined, or excluded when required inputs are missing or invalid, including:

- missing required identity;
- invalid timestamps;
- non-finite spatial/kinematic values;
- unknown or incompatible coordinate frames;
- invalid transforms;
- invalid confidence;
- unsupported/invalid types;
- unusable calibration;
- explicit invalid source state.

Invalid evidence must not silently mutate authoritative world state. Where practical, the reason should be observable through structured diagnostics.

## Determinism Requirements

Given identical:

- valid evidence;
- timestamps;
- transforms;
- calibration state;
- source quality state;
- association state;
- fusion configuration;

Fusion must produce the same result and equivalent provenance/qualification/exclusion metadata.

Input ordering must not change the logical result.

## Concurrency and Ordering

Concurrent evidence processing may be supported if logical fusion results remain deterministic and well-defined.

Mutation of authoritative World Model state remains serialized through the WorldModelUpdater boundary.

Performance optimization must not weaken correctness, provenance, determinism, or exclusion diagnostics.

## Required Tests

The initial implementation must include tests for at least:

1. compatible evidence produces one fused estimate;
2. incompatible coordinate frames are rejected or unresolved;
3. valid transforms allow compatible evidence to be compared in a common frame;
4. event time and ingestion/processing time are distinguished;
5. out-of-order evidence follows configured policy;
6. stale evidence cannot masquerade as current evidence;
7. missing uncertainty remains unknown rather than zero;
8. confidence and uncertainty remain separate;
9. degraded/invalid source state affects contribution according to policy;
10. incompatible evidence is not blindly averaged;
11. association is explicit and traceable;
12. provenance preserves contributing source/track/detection identity;
13. excluded evidence produces a structured stable diagnostic;
14. material disagreement is observable rather than silently averaged;
15. invalid evidence cannot corrupt authoritative World Model state;
16. identical valid inputs and configuration produce deterministic results;
17. Fusion does not directly mutate authoritative World Model state;
18. valid fused estimates enter the World Model only through the updater boundary;
19. unresolved evidence does not force unsupported identity or precision;
20. valid spatial transforms are applied deterministically and their provenance is preserved;
21. invalid spatial transforms are excluded explicitly rather than silently ignored;
22. translation-only spatial transforms preserve velocity while applying the explicit velocity transform boundary.

## Deferred Capabilities

The following remain intentionally deferred until later architectural layers or contracts require them:

- advanced probabilistic association;
- multi-hypothesis tracking;
- full covariance/state estimation where unsupported by current primitives;
- automatic calibration;
- distributed fusion;
- persistent fusion database;
- ML-based fusion decisions;
- AI-based identity resolution;
- advanced uncertainty propagation across arbitrary transforms;
- presentation/attention prioritization.

## Implementation Boundary

The initial implementation should remain:

- small;
- deterministic;
- inspectable;
- based on existing spatial primitives, units, identity, timestamps, and Track -> World Model boundary;
- replaceable at the algorithm level without changing the architectural contract.

Spatial transform support should remain similarly replaceable. The initial translation-only abstraction establishes the boundary without prematurely creating a general geometry or calibration framework.

## Mandatory Invariants

- Fusion combines compatible evidence but does not create authoritative world state.
- Association is a supported claim, not proof of physical identity.
- Confidence, uncertainty, freshness, and provenance remain distinct concepts.
- Material disagreement is represented explicitly.
- Evidence exclusions remain observable.
- Qualification semantics remain explicit.
- Authoritative World Model state changes only through the designated updater boundary.
- Spatial frame conversion is explicit, deterministic, and provenance-bearing.
