# VIGIL Spatial / Temporal Fusion Technical Contract

**Document:** Spatial / Temporal Fusion Technical Contract  
**Version:** 0.2  
**Status:** Proposed for implementation  
**Parent:** Spatial World Model Technical Design

## 1. Purpose

This contract defines the boundary between perception state from one or more sources and a fused spatial/temporal estimate suitable for the authoritative World Model.

Fusion combines compatible evidence into a better-supported estimate while preserving uncertainty, provenance, disagreement, freshness, and data-quality information. Fusion does not create physical truth and must not silently turn incomplete evidence into certainty.

The contract defines semantics first. Algorithms, filters, storage, frameworks, and hardware-specific implementations are implementation details unless explicitly required here.

## 2. Scope

This milestone covers the conceptual and technical boundary:

`Tracks / Compatible Evidence → Spatial / Temporal Fusion → Fused Estimate → WorldModelUpdater → World Model`

It extends the existing lifecycle:

`Observation → Detection → Track → Spatial / Temporal Fusion → Spatial World Model → Spatial / Environmental Services → Relevance & Priority → Presentation / Attention`

The first implementation may begin with Tracks as its primary inputs. Future implementations may accept other explicitly compatible evidence without changing the World Model boundary.

## 3. Responsibilities

### 3.1 Fusion Layer

The fusion layer is responsible for:

- accepting structured perception evidence;
- validating temporal and spatial compatibility;
- aligning evidence to a common temporal and spatial interpretation;
- evaluating whether evidence may refer to the same modeled entity or state;
- combining compatible evidence into an estimate;
- preserving confidence, uncertainty, freshness, validity, and provenance;
- representing or exposing meaningful disagreement between sources;
- applying configured source/data-quality policy; and
- producing deterministic fusion results for identical valid inputs and configuration.

Fusion must not directly mutate the authoritative World Model.

Evidence that is excluded from a fusion result must remain observable through structured diagnostic information. Exclusion must not become silent data loss.

### 3.2 WorldModelUpdater

`WorldModelUpdater` remains the controlled boundary into authoritative world state.

The fused estimate enters the World Model through the same controlled update path used by track-derived state. Fusion must not bypass validation, temporal ordering, provenance, or event semantics defined by the Track → World Model contract.

### 3.3 World Model

The World Model remains responsible for authoritative current spatial state. It does not become a fusion engine and does not need to know which estimation algorithm produced a valid fused state.

## 4. Terminology

To avoid ambiguity, VIGIL uses the following meanings:

- **Evidence:** structured information supplied to fusion from an authorized source or upstream perception component.
- **Alignment:** placing evidence into a compatible temporal and spatial interpretation before comparison or combination.
- **Association:** an explicit claim that two or more evidence items may describe the same modeled entity or state.
- **Fusion:** combining compatible evidence into a joint estimate.
- **Estimate:** a derived representation of spatial or temporal state that includes its supporting uncertainty and provenance.
- **Confidence:** strength of support for a classification, association, or conclusion.
- **Uncertainty:** expected error or imprecision in an estimated quantity.
- **Freshness:** how current an estimate is relative to its relevant observation/update time and configured policy.
- **Disagreement:** materially inconsistent evidence that cannot be treated as mutually equivalent without further policy or information.
- **Qualified:** an estimate that is suitable as a qualified fusion result under the configured policy. Qualification does not mean that all evidence agreed, nor does it mean that a conflict occurred.
- **Exclusion:** a structured record that identifies evidence not included in the resulting fusion and the policy-defined reason for its exclusion.

Fusion must not use these terms interchangeably.

## 5. Input Contract

Each fusion input must provide, directly or through resolvable upstream references, as much of the following as its source supports:

- stable source identity;
- stable detection/track/evidence identity;
- event/observation time;
- ingestion/processing time when available;
- coordinate-frame identity;
- spatial state, when available;
- kinematic state such as velocity or heading, when available;
- entity/type classification, when available;
- confidence;
- uncertainty, when available;
- validity/quality state;
- freshness information; and
- provenance references.

Missing values must remain explicitly unavailable. Fusion must never interpret missing position, velocity, uncertainty, timestamp, or calibration information as zero, perfect certainty, or a default guess.

## 6. Temporal Alignment

Fusion must distinguish when evidence applies to the environment from when VIGIL received or processed it.

The fusion layer shall use an explicit common time interpretation and configured temporal policy.

The policy must define behavior for:

- normal timestamp differences;
- allowable temporal skew;
- out-of-order evidence;
- stale evidence;
- future-dated evidence;
- missing or uncertain timestamps;
- interpolation, when supported;
- extrapolation, when supported; and
- evidence that cannot be temporally aligned with sufficient validity.

The initial implementation must not hard-code a universal timing threshold into the architecture contract. Runtime configuration may define thresholds appropriate to the participating sensors and workload.

An input whose temporal validity cannot be established must not silently contribute as though it were current.

When evidence is excluded for temporal incompatibility, the fusion result should expose a structured exclusion reason when supported by the implementation.

## 7. Spatial Frame Alignment

Fusion inputs must have an explicit coordinate-frame interpretation.

Supported frame relationships may include:

```text
Sensor Frame
     ↓
Device / Body Frame
     ↓
Local / World Frame
     ↓
Geographic Frame, when applicable
```

A transform used for fusion must have:

- source frame;
- destination frame;
- validity state;
- calibration/provenance information where applicable; and
- an interpretation consistent with the relevant timestamp.

Evidence with an unknown, invalid, or incompatible spatial transform must not be silently fused.

VIGIL's existing spatial primitives and documented units remain authoritative. Fusion must not introduce competing coordinate or unit conventions.

When evidence is excluded for frame incompatibility, the fusion result should expose a structured exclusion reason when supported by the implementation.

## 8. Calibration and Sensor Validity

Fusion must treat calibration and sensor/data health as inputs to fusion quality.

Relevant states may include:

- healthy;
- degraded;
- stale;
- unavailable;
- invalid;
- calibration uncertain; and
- timestamp uncertain.

The fusion policy may reduce, reject, or defer contributions from degraded evidence according to explicit rules.

A degraded source must not automatically invalidate all other compatible evidence.

Calibration uncertainty must remain distinguishable from measurement uncertainty when the implementation supports that distinction.

## 9. Evidence Association

Fusion must explicitly evaluate whether evidence may refer to the same entity or state.

For example:

```text
Camera → Track A
Depth  → Track B
IMU    → Motion Evidence C
             ↓
       Association Analysis
             ↓
       Same-state evidence?
             ↓
        Fused Estimate
```

Association is an evidence-based claim, not proof of physical identity.

An association policy must be capable of considering compatible factors such as:

- spatial consistency;
- temporal consistency;
- motion consistency;
- type compatibility;
- source validity;
- expected measurement error; and
- existing association state.

The policy must not silently force incompatible evidence into one entity merely because it is nearby.

Sophisticated probabilistic or multi-hypothesis identity resolution is deferred, but the contract must preserve a place for competing or uncertain associations.

## 10. Fused State Contract

A fused estimate should represent the best supported state available under the configured policy, while retaining how that state was obtained.

Where supported, the fused state may contain:

- entity/association reference;
- position;
- velocity;
- heading/orientation;
- confidence;
- uncertainty;
- validity;
- freshness/observation age;
- fusion timestamp;
- contributing sources;
- contributing tracks;
- contributing detections/evidence; and
- consistency or quality metadata.

The fusion layer must not fabricate unsupported precision.

A fused position without a supported uncertainty estimate must expose uncertainty as unavailable rather than zero.

A fusion operation may also produce structured exclusion diagnostics alongside the estimate. These diagnostics identify evidence that was not included and the policy-defined reason for exclusion.

## 11. Confidence and Uncertainty

Confidence and uncertainty are independent.

- Confidence describes how strongly the system supports a classification, association, or conclusion.
- Uncertainty describes the expected error or imprecision in an estimated quantity.

Combining multiple sources does not automatically mean confidence becomes higher or uncertainty becomes lower. Those changes must follow the selected fusion method and evidence quality.

Fusion must preserve source uncertainty where supported and must not claim precision unsupported by the inputs or estimator.

Priority is outside this contract. Fusion must not determine whether an entity deserves user attention.

## 12. Conflicting Evidence

Fusion must explicitly account for material disagreement.

For example:

```text
Source A → position estimate A
Source B → position estimate B
             ↓
       Consistency Check
          ↙        ↘
     Compatible    Conflict
         ↓            ↓
      Combine    Down-weight / defer / reject
```

The selected behavior must be deterministic and policy-driven.

Fusion must not simply average materially inconsistent measurements without first applying the configured consistency policy.

When evidence remains unresolved, the result may be:

- a fused estimate with elevated uncertainty;
- a qualified estimate using a subset of evidence;
- an explicitly unqualified estimate using a deterministic subset of evidence;
- an unresolved association; or
- no fused estimate.

The choice must remain explainable through provenance, qualification state, and quality metadata.

When material disagreement causes evidence to be excluded, the exclusion must be observable through structured diagnostic information. `MATERIAL_DISAGREEMENT` identifies this condition; it does not by itself mean that the entire fusion operation failed.

`qualified` means that the resulting estimate is suitable as a qualified result under policy. Compatible or single-source evidence may therefore be qualified. Material disagreement may produce an unqualified estimate when policy permits a deterministic subset to remain usable. Qualification is not a synonym for conflict-free evidence.

## 13. Freshness and Observation Age

Freshness is a first-class property of fused state.

The fusion layer must preserve the relationship between:

- source observation time;
- latest contributing evidence time;
- fusion processing time; and
- current age of the resulting estimate.

An old but valid estimate may remain useful historical or contextual information, but it must not be represented as newly observed merely because fusion processing occurred recently.

A loss of new evidence must not silently erase historical identity from the World Model.

## 14. Provenance

Every fused estimate must preserve a traceable evidence path.

At minimum, provenance should support:

```text
Fused Estimate
    ↓
Contributing Tracks / Evidence
    ↓
Detections
    ↓
Observations / Authorized Sources
```

Fusion must not collapse provenance into only the final numerical estimate.

Where evidence is rejected, excluded, or down-weighted because of quality or inconsistency, the reason should be available to diagnostics and future presentation/explainability layers when supported.

Exclusion diagnostics must identify the affected evidence and a stable reason category. The initial implementation defines the following categories:

- `INCOMPATIBLE_FRAME`;
- `INCOMPATIBLE_TYPE`;
- `TEMPORAL_SKEW`;
- `OUTSIDE_CONFLICT_DISTANCE`; and
- `MATERIAL_DISAGREEMENT`.

The diagnostic record does not itself establish physical identity or authority. It explains the fusion decision for the affected evidence.

## 15. Output Contract

A fusion operation produces a structured result containing:

- an optional fused estimate; and
- zero or more structured exclusion diagnostics.

A successful fusion operation produces one of:

1. a valid fused estimate, with any applicable exclusions exposed;
2. an explicitly unresolved or unqualified result, with applicable diagnostics exposed; or
3. no fused estimate because the evidence is insufficient or invalid, with applicable diagnostics exposed.

A valid fused estimate must be suitable for submission to `WorldModelUpdater`.

Fusion must not directly emit a World Model state mutation.

The logical flow is:

```text
Evidence
   ↓
Validate
   ↓
Temporal Alignment
   ↓
Spatial Alignment
   ↓
Quality / Calibration Evaluation
   ↓
Association Evaluation
   ↓
Consistency Evaluation
   ↓
Fusion / Estimation
   ↓
Attach Confidence / Uncertainty / Freshness / Provenance
   ↓
Fused Estimate + Exclusion Diagnostics
   ↓
WorldModelUpdater
   ↓
World Model
```

Exclusion diagnostics are explanatory fusion output, not authoritative World Model state.

## 16. Invalid Input Rules

Fusion must reject, quarantine, or exclude evidence that violates required semantics, including:

- missing stable source/evidence identity where required;
- invalid timestamps;
- non-finite spatial or kinematic values;
- unknown or incompatible coordinate frames;
- invalid transforms;
- invalid confidence values;
- unsupported or invalid entity types where required;
- unusable calibration state; and
- evidence explicitly marked invalid by its source or upstream processing.

Invalid evidence must not silently mutate authoritative world state.

Where an invalid or incompatible input is excluded as part of a fusion operation, the implementation should expose an appropriate structured diagnostic rather than silently dropping the evidence.

## 17. Determinism

Given the same:

- valid input evidence;
- timestamps;
- coordinate transforms;
- calibration state;
- source-quality state;
- existing association state; and
- configuration;

VIGIL shall produce the same fused result and equivalent provenance, qualification, and exclusion metadata.

The initial implementation does not require AI, stochastic identity decisions, or nondeterministic estimation.

## 18. Concurrency and Ordering

Fusion may process multiple inputs concurrently in future implementations, but logical fusion results must have a defined temporal and spatial ordering.

A fused estimate must identify the evidence and state version from which it was derived where the runtime supports versioning.

World Model mutation remains serialized through the controlled `WorldModelUpdater` boundary according to that component's temporal contract.

Performance optimization must not weaken temporal correctness, provenance, determinism, or diagnostic observability.

## 19. Required Tests

The implementation is not complete until tests demonstrate at least:

1. compatible evidence can produce one fused estimate;
2. incompatible coordinate frames are rejected or explicitly unresolved;
3. valid transforms allow compatible evidence to be compared in a common frame;
4. event time is distinguished from ingestion/processing time;
5. out-of-order evidence follows the configured temporal policy;
6. stale evidence cannot silently masquerade as current evidence;
7. missing uncertainty remains unknown rather than becoming zero;
8. confidence and uncertainty remain separate;
9. degraded/invalid source state affects contribution according to policy;
10. incompatible evidence is not blindly averaged;
11. association decisions remain explicit and traceable;
12. fused provenance preserves contributing source/track/detection references;
13. excluded evidence produces observable structured diagnostics with a stable reason;
14. material disagreement is observable and does not silently become an averaged estimate;
15. invalid evidence cannot corrupt authoritative World Model state;
16. identical valid inputs and configuration produce deterministic results;
17. fusion does not directly mutate World Model state;
18. a valid fused estimate enters the World Model only through `WorldModelUpdater`; and
19. unresolved evidence does not force an unsupported identity or precision claim.

## 20. Deferred Capabilities

The following are intentionally deferred rather than left ambiguous:

- advanced probabilistic data association;
- multi-hypothesis tracking/association;
- full covariance and state-estimation mathematics where not yet supported by current primitives;
- automatic sensor calibration;
- distributed fusion across devices;
- persistent fusion databases;
- machine-learning-based fusion decisions;
- AI-based identity resolution;
- advanced uncertainty propagation across arbitrary transforms; and
- presentation/attention prioritization.

These capabilities must integrate through this contract rather than bypassing the World Model boundary.

## 21. Initial Implementation Strategy

The first implementation should favor a small, deterministic, inspectable fusion mechanism that demonstrates the contract without prematurely selecting a heavyweight estimation framework.

The implementation should preserve the repository's existing spatial primitives, units, identity separation, timestamp semantics, and Track → World Model boundary.

Algorithm selection should remain replaceable behind the fusion contract.

Technology versions and libraries remain implementation choices. The repository's supported toolchain should be used rather than inheriting dependency versions from unrelated external applications.

## 22. Architectural Invariants

The following invariants are mandatory:

> **Fusion combines compatible evidence; it does not create authoritative world state.**

> **Association is a claim supported by evidence, not automatic proof of physical identity.**

> **Confidence, uncertainty, freshness, and provenance remain distinct properties.**

> **Material disagreement must be represented or resolved by explicit policy; it must not be hidden by unsupported averaging.**

> **Fusion exclusions are observable diagnostics, not silent data loss.**

> **Qualification describes whether an estimate is suitable as a qualified result under policy; it is not a synonym for conflict-free evidence.**

> **The World Model is authoritative current spatial state, and fused estimates enter it only through the controlled WorldModelUpdater boundary.**
