# M14 Scenario DSL Contract

## Scope

M14 promotes the M9 evidence vocabulary into a public, versioned grammar for
scenario steps. Scenarios remain canonical `WORLDLINE-SCENARIO/1` artifacts;
the DSL defines which opaque steps any conforming adapter must understand,
with one canonical spelling per step.

## Grammar

`worldline-scenario-dsl/1` accepts exactly these verbs:

| Step | Meaning | Bounds |
| --- | --- | --- |
| `tick` / `tick:<n>` | advance `n` controlled ticks | `1..4096` |
| `reseed:<long>` | reseed the controlled RNG | decimal long |
| `tap:<key>` | press and release a key code | `0..255` |
| `observe:<label>` | record an observation under `label` | `[a-z0-9_]{1,32}` |
| `block:x,y,z:id[:meta]` | write a legacy block state | coords within ±33554432, id `0..255`, meta `0..15` |

Rendering is canonical: single ticks render as `tick`, omitted metadata means
`0`, and every parsed step round-trips through `ScenarioDsl.render`. Parsing
is strict and fail closed; unknown verbs, malformed numbers, and out-of-bounds
values are rejected.

## Execution

`worldline.minimization.ScenarioRunner` is the neutral execution contract:
implementations boot the controlled runtime under a seed, apply every step in
order, and return the canonical trace. The CLI binds an adapter reflectively:

```text
worldline scenario validate <scenario.wlscenario>
worldline scenario run <scenario.wlscenario> <seed> <trace.wltrace>
```

`validate` parses every step without loading Minecraft. `run` requires the
prepared runtime and writes the trace with create-new semantics.

## Minimization integration

DSL scenarios are ordinary `Scenario` artifacts, so the M9 minimizer, budgets,
and one-minimality proofs apply unchanged. Adapter-private vocabulary (for
example lab/noise steps) stays outside the public grammar and remains valid
input to the minimizer's disposable-step heuristics.

## Evidence

The m14 smoke authors a five-step scenario through the CLI, validates the
typed rendering, rejects an unknown verb, executes twice under seed 4242 with
byte-identical traces, and freezes the result in
`smokes/m14-scenario-dsl/smoke.properties`.

## Non-claims

M14 does not claim conditional branches, loops, assertions inside scenarios,
parallel execution, cross-runtime portability of numeric observations beyond
the recorded schema, or a GUI authoring surface.
