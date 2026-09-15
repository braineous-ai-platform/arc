# Arc

**Intelligence Bridge**
![This is an image](parallax-image.jpg)

Arc is the Intelligence Bridge between **linear enterprise computation** and **non-linear machine intelligence**.

Arc is not an LLM abstraction layer.

Models and providers are implementation details.

Arc defines the boundary through which an enterprise runtime can invoke non-linear intelligence while remaining an enterprise runtime.

---

## What is Arc?

Enterprise software is exceptionally good at executing known computation.

Events arrive. APIs are invoked. State changes. Rules execute. Data is persisted. Workflows advance.

These systems are naturally expressed as explicit computational paths.

Machine intelligence introduces a different capability.

The useful path through context, relationships, ambiguity, latent meaning, and competing interpretations does not always need to be prescribed by the application beforehand.

Arc bridges those two worlds.

```text
Linear Enterprise Computation
              │
              │
              ▼
            ARC
     Intelligence Bridge
              │
              ╰───────────────╮
                              ▼
                   Non-linear Machine
                       Intelligence
```

The enterprise application does not need to become an AI system.

It crosses the intelligence boundary through Arc.

---

## Why does Arc exist?

Not every enterprise problem has a useful linear solution.

Traditional enterprise computation works extremely well when the path is known:

```text
Input
  ↓
Process
  ↓
State
  ↓
Output
```

But some questions require interpretation rather than another explicitly authored computational path.

Arc provides a canonical bridge from linear enterprise computation into non-linear machine intelligence without prescribing how that intelligence must be used.

Arc can be used by:

- Continuous Intelligence systems
- Perceptive Intelligence systems
- Agentic systems
- Enterprise applications
- Other architectures that require access to non-linear machine intelligence

Arc does not prescribe the architecture consuming it.

Within BraineousAI, the primary focus is **Continuous Intelligence** and **Perceptive Intelligence**.

That is a BraineousAI architectural choice.

It is not an Arc constraint.

### Developer experience

```text
Clone.
Connect.
Invoke.
```

Arc is designed to stand on its own.

BraineousAI is not required.

---

## Why isn't Arc just another LLM abstraction?

Because Arc does not abstract LLM providers.

**Arc abstracts the intelligence boundary.**

In its initial implementation, Arc can use proven infrastructure such as LiteLLM internally rather than reinventing model routing, provider compatibility, fallback, and related plumbing.

Conceptually:

```text
Application
    │
    ▼
   ARC
    │
    ▼
 LiteLLM
    │
    ▼
Models / Providers
```

LiteLLM is an implementation detail behind Arc.

It does not define Arc's public contract.

No LiteLLM-specific concept should need to bleed through Arc's public API.

Tomorrow, the implementation behind Arc may change.

Another substrate may coexist with LiteLLM.

LiteLLM may eventually disappear entirely.

The intelligence itself may no longer be provided primarily by an LLM.

None of those changes should require the application above Arc to adopt a new intelligence boundary.

From the application perspective:

> **Arc is the black box.**

---

## Why the name Arc?

Arc is a smooth bridge from **linear enterprise reality** into **non-linear machine intelligence**.

The enterprise side remains familiar:

```text
Events
APIs
State
Flows
Operations
Computation
```

The intelligence side is free to evolve.

Today:

```text
LLMs
```

Tomorrow:

```text
World Models
JEPA
Perception Models
Other Intelligence Substrates
```

What provides the intelligence is deliberately irrelevant to the caller.

Swap it.

Combine it.

Replace it.

That evolution belongs behind Arc.

```text
LINEAR ENTERPRISE REALITY
          │
          │
          ▼
        ┌─────┐
        │ ARC │
        └─────┘
          ╰────────────────╮
                           ▼
                 NON-LINEAR MACHINE
                     INTELLIGENCE
```

A useful distinction is:

> **LiteLLM is a linear abstraction across model providers. Arc is a non-linear abstraction across the intelligence boundary.**

The left side can remain boring enterprise software.

The right side can evolve rapidly.

**Arc absorbs the boundary.**

---

## Does Arc solve Agentic architecture?

No.

Arc solves the **intelligence boundary**, not the architecture consuming it.

An agent may use Arc to access machine intelligence.

That does not mean Arc provides:

- planning
- memory
- goal decomposition
- tool selection
- execution loops
- autonomy
- action governance
- runaway-execution protection
- agent cost control
- correctness of autonomous actions

Those concerns belong to the architecture using Arc.

The same principle applies to Continuous Intelligence and Perceptive Intelligence.

Arc provides access to non-linear machine intelligence.

The consuming architecture determines what happens before and after that boundary.

> **Arc connects intelligence. It does not orchestrate behavior.**

---

## Architecture Boundary

Arc's current architectural thesis is intentionally small.

```text
Linear Enterprise Reality
          │
          ▼
         ARC
          │
          ▼
Non-linear Machine Intelligence
```

The intelligence behind Arc may initially be provided through LLMs.

That is not the architectural limit of Arc.

```text
                    ARC
             Intelligence Bridge
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
         LLM      World     Perception
                  Model       Model
```

The exact Arc feature set, runtime contracts, and implementation architecture are **not defined here yet**.

Those will be designed before they are documented.

This README currently locks only the architectural thesis and boundary.

---

## Status

**Architecture thesis: initial lock.**

Implementation has not started.

Feature design has not been frozen.

Arc is not yet being presented as a completed public component.

---

## License

Apache License 2.0
