# Bounded Inference — Discovery Notes

## Why this note exists

We started by debugging apparently unstable LLM behavior in a PAY execution case. The investigation moved through Arc, LiteLLM, Ollama, endpoint semantics, prompt structure, graph semantics, and repeated inference.

The important outcome was not a better hand-written prompt.

We discovered a possible programming model for enterprise cognition.

This document records the hypothesis and the experimental evidence that led to it. It is intentionally a discovery note, not a finalized architecture.

## 1. The original failure

With the same PAY system state, the LLM could produce semantically different interpretations.

It invented unsupported facts and relationships and allowed machine-significant meaning to drift.

The problem was not merely different wording.

The problem was unbounded semantics.

## 2. Separate probabilistic language from machine consequence

The output has fields with different responsibilities.

decision
- machine-significant
- may drive system mutation
- must be bounded

code
- machine/governance-significant
- must remain coherent with the decision

reason
- audit/explanation surface
- may remain probabilistic
- wording does not need to be deterministic

The objective is therefore not deterministic language.

The objective is bounded machine consequence.

## 3. The first bounded primitive

The PAY use case eventually expressed intent through flat semantic controls:

```json
"controls": {
  "intent": "decide_payment_capture",
  "action": "determine",
  "subject": "primary_payment_request",
  "decision": "allow_capture",
  "basis": "related_system_facts",
  "goal": "decision"
}
```

Once the decision semantics were expressed this way, repeated execution of the same canonical state repeatedly produced:

decision = allow_capture

while reason and code continued to vary.

This was important.

The probabilistic model remained probabilistic, but the machine-significant decision became stable in the observed experiment.

## 4. Code exposed the same phenomenon independently

We then strengthened the test invariant to require:

decision = allow_capture
AND
code = 200

The test immediately failed.

Observed example:

decision = allow_capture
code = 1

This was expected in retrospect because the semantic controls contained:

decision = allow_capture

but contained no semantic definition for the success code.

The model had been free to generate values such as:

0
1
200
SUCCESS
empty strings
and other values

The code dimension had never been bounded.

## 5. One semantic primitive changed the result

We added exactly one control:

```json
"success": "200"
```

Nothing else changed.

The next five-run observation produced:

5/5 decision = allow_capture
5/5 code = 200
BUILD SUCCESS

This was the second independent example of the same pattern:

decision = allow_capture
-> bounded decision

success = 200
-> bounded success code

The model was not given a procedural instruction such as:

"Set result.code to 200."

Instead, the business meaning was expressed as a semantic primitive and the existing execution/output grammar remained unchanged.

## 6. The prompt is mostly static

An important realization followed.

Most of the LLM prompt is not application logic.

It is stable execution grammar:

- how to interpret context
- what the primary fact means
- how related facts are scoped
- what controls mean
- what may and may not be inferred
- output shape
- JSON constraints
- execution boundaries

Once correct, this should not be rewritten for every business execution.

Hand-editing this layer repeatedly feels increasingly analogous to hand-authoring static web pages.

Prompt engineering was useful as a microscope for discovering the execution grammar.

It does not appear to be the desired enterprise application programming model.

## 7. Controls are the axis

The controls section has a different role.

For PAY:

```text
intent   = decide_payment_capture
action   = determine
subject  = primary_payment_request
decision = allow_capture
basis    = related_system_facts
goal     = decision
success  = 200
```

Read structurally, this resembles a business-function specification:

```text
decide_payment_capture(
    action  = determine,
    subject = primary_payment_request,
    basis   = related_system_facts
)
→ decision
→ success semantics
```

The controls are our internal representation.

For a defined use case, they should be treated as a specification that can be created and then frozen rather than rewritten for every invocation.

## 8. Context is the truly dynamic portion

Controls and context must not be confused.

Controls define the cognitive/business function.

Context represents current enterprise state.

Example current context includes facts such as:

- PaymentRequest
- CustomerAccount
- RiskProfile
- PaymentMethod
- MerchantPolicy

That state changes continuously.

PAY-1001 is only one state.

PAY-1002 may have different amount, account status, risk, payment method, policy, and relationships.

Therefore the architecture is closer to:

```text
STATIC EXECUTION GRAMMAR
        +
FROZEN USE-CASE SPECIFICATION
        +
LIVE ENTERPRISE CONTEXT
        ↓
LLM COGNITION
        ↓
BOUNDED MACHINE CONSEQUENCE
```

The same specification can execute repeatedly against changing context.

The output should change when the relevant enterprise state changes.

The specification should not need to.

## 9. Static website -> dynamic application analogy

A useful analogy emerged during the experiment.

Hand-written prompt engineering resembles the static-web era.

The prompt engineer is analogous to someone hand-authoring the page:

- move this instruction
- change this wording
- add another sentence
- optimize this token sequence

That work is useful while discovering the medium, just as static HTML and visual web tooling were useful.

But applications emerged when computation and dynamic state could be injected behind the static surface.

The analogy is:

```text
STATIC WEB
HTML / presentation grammar
        +
CGI / application computation
        +
dynamic request/data
```

versus:

```text
LLM EXECUTION
static prompt / execution grammar
        +
CGO use-case specification
        +
dynamic enterprise context
```

Controls are not identical to CGI. The analogy is about the architectural transition:

from manually authoring static behavior
to executing a stable specification against dynamic state.

## 10. The enterprise does not need to be learned by the model

Another important hypothesis:

Enterprise intelligence already exists and changes continuously.

Orders, payments, inventory, policies, machines, customers, operations, relationships, history, and events are generated all day.

A foundation model does not need to have been trained on PAY-1001.

PAY-1001 may not even have existed when the model was trained.

The model needs sufficient general cognitive capability.

The runtime supplies the relevant enterprise intelligence when cognition is required.

Conceptually:

```text
general cognitive capability
        +
current enterprise state
        +
business/cognitive specification
        ↓
runtime cognition
        ↓
governed consequence
```

The enterprise should not have to train its changing operational state into the model before the model can reason over it.

State can be supplied at runtime.

## 11. Plug it into the firehose

Nobody should hand-write the JSON used in these experiments for every event.

The experiment is a laboratory specimen.

Production has to connect the cognitive specification to continuously changing enterprise state.

Conceptually:

```text
ENTERPRISE FIREHOSE
events / operations / state changes
        ↓
Motion
observation / perception / current state
        ↓
CGO Query Generation
frozen use-case specification
        +
relevant current context
        ↓
canonical intelligence request
        ↓
Arc
        ↓
LLM substrate
        ↓
bounded consequence
```

The developer should define the use case.

The runtime should materialize the current context and execute it repeatedly as state changes.

## 12. Prompt engineering as assembly

The experiments reinforced an earlier analogy.

Building large enterprise systems directly through hand-written prompt engineering resembles implementing application logic in assembly language.

It can work.

It is useful for understanding the machine.

It is the wrong abstraction level for building large enterprise applications.

CGO Query Generation is intended to move application developers above that level.

## 13. Observed bounded-inference evidence

The experiment repeatedly executed the same canonical PAY state.

Observed decision coherence before adding the code invariant reached:

510/510 decision = allow_capture

This included:

- repeated individual runs
- increasing iteration counts
- a 350-execution soak of approximately 47:54
- concurrent caller experiments

The important qualification:

This does NOT prove universal LLM determinism.
This does NOT establish a general reliability percentage.
This does NOT prove every context will produce a correct decision.

It establishes a narrower observation:

For this fixed PAY state and semantic specification, the machine-significant decision did not drift in the observed executions.

After adding the new governance invariant:

decision = allow_capture
AND
code = 200

the existing specification failed because code was unbounded.

After adding:

success = 200

the five-run observation produced:

5/5 decision = allow_capture
5/5 code = 200

That RED -> semantic primitive -> GREEN transition is especially important evidence for the controls hypothesis.

## 14. Throughput is a separate concern

We also experimented with concurrent callers.

Java could submit concurrent Arc invocations, but the local inference path did not provide useful throughput improvement.

That is not part of the semantic contract.

Inference throughput, batching, GPU scheduling, and serving concurrency belong to the inference substrate.

Arc/CGO should not distort intelligence semantics to compensate for a local serving implementation.

The drift bench therefore returns to a small repeated observation count.

Current philosophy:

five executions are sufficient for the prototype semantic smoke bench.

Large soak counts were useful for discovery, not intended as the permanent test execution model.

## 15. Current working model

The emerging decomposition is:

```text
STATIC PROMPT
    execution grammar

CONTROLS
    frozen use-case / cognitive specification

CONTEXT
    live enterprise state

LLM
    general cognitive execution substrate

OUTPUT CONTRACT
    governed machine boundary
```

Or, approximately:

```text
static prompt    ≈ runtime semantics
controls         ≈ business/cognitive specification
context          ≈ runtime state / arguments
LLM              ≈ cognitive evaluator
output contract  ≈ governed return boundary
```

This analogy is exploratory, not a claim that an LLM is literally a compiler or CPU.

## 16. Core thesis emerging from the experiment

Enterprise intelligence does not require deterministic language.

It requires bounded consequences.

The model may remain probabilistic where probabilism is harmless.

Machine-significant outputs must be governed.

The enterprise's changing intelligence does not have to live inside model weights.

It can flow through the runtime.

Specification stays.

State moves.

Cognition evaluates.

Consequences are bounded.

## 17. Current Arc baseline

At the time this note is created:

branch:
main

published HEAD:
f41edd51e11e180ade5f4dded749b995afdd03fd

commit:

test(arc): bound the consequence, not the language

The repository was clean and HEAD matched origin/main after publication.
