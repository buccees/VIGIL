# VIGIL Documentation Rules

**Status:** Architectural rule  
**Applies to:** README, architecture specifications, technical documentation, code comments, examples, issue descriptions, and other project-facing documentation

## Technical Terminology Rule

VIGIL documentation must use precise technical terminology that accurately describes sensing, perception, spatial computation, environmental modeling, information prioritization, human-machine interaction, and information presentation.

Documentation must avoid terminology that could reasonably imply that VIGIL is intended to provide offensive action, weapon control, autonomous physical intervention, or similar capabilities when that terminology is not technically required.

This is **not** a rule to remove technical detail. VIGIL documentation should remain technically rigorous and should use established engineering terminology wherever it accurately describes the system.

## Preferred Vocabulary

Use terminology such as:

- observation
- detection
- object/entity
- track / track maintenance
- spatial estimation
- motion estimation
- sensor fusion
- spatial world model
- environmental model
- change detection
- anomaly detection
- uncertainty quantification
- confidence
- data freshness / observation age
- relevance
- priority
- information prioritization
- attention management
- spatial visualization
- information presentation
- object of interest
- entity of interest
- spatial search
- spatial objective
- provenance
- sensor health
- sensor calibration
- temporal history

## Terminology to Avoid When Not Technically Necessary

Do not unnecessarily use terminology associated with:

- offensive action;
- weapon control;
- firing or fire control;
- autonomous engagement;
- weapons guidance;
- lethality;
- ballistic solutions;
- interception as a physical-response concept; or
- autonomous physical response.

When describing a generic user-selected spatial objective, prefer **target**, **object of interest**, **entity of interest**, or **spatial objective** according to context. The term `target` may remain in technical contexts where it accurately describes a generic spatial objective, but documentation should make the meaning clear from context.

## Documentation Boundary

VIGIL is an information and presentation system. Its purpose is to expand the user's effective environmental perception by sensing continuously, processing information rapidly, maintaining persistent spatial state, correlating information, and presenting relevant information to the user.

VIGIL has no physical actuators or appendages. The system presents information; the user remains responsible for interpretation, decisions, and physical responses.

AI is an optional analysis capability over structured VIGIL information. Documentation should not describe AI as the authority over the physical environment or as an autonomous actor.

## Writing Standard

Technical documentation should answer:

1. **What information does the system receive?**
2. **What does VIGIL derive from that information?**
3. **How certain and how fresh is the information?**
4. **How is the information retained and correlated?**
5. **Why is particular information prioritized for presentation?**
6. **What does the user see?**
7. **What remains the user's responsibility?**

The objective is precise engineering communication without terminology that mischaracterizes the project's purpose or capabilities.
