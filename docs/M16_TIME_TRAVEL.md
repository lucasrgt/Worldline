# M16 Time-Travel Debug Contract

## Scope

M16 turns any public-grammar scenario into a deterministic debugging
session. Because every controlled run is reproducible from its prefix,
reverse jumps are exact: `worldline debug` re-executes the scenario prefix on
demand, so going backward is as faithful as stepping forward.

## Command

```text
worldline debug <scenario.wlscenario> <seed>
```

The scenario must be DSL-valid (M14). The session reads commands from stdin
and emits stable `WORLDLINE_DEBUG_*` lines, making scripted sessions fully
deterministic evidence artifacts.

## Commands

| Command | Effect |
| --- | --- |
| `step [n]` | advance `n` steps (default 1) by replaying the longer prefix |
| `back [n]` | return `n` steps; the world is rebuilt from scratch to that prefix |
| `goto <i>` | jump to an absolute step index (clamped to `0..steps`) |
| `observe` | dump the latest recorded observation (`field=value` per schema field) |
| `watch <field>` / `unwatch` | arm or clear a watchpoint on one trace field |
| `scenario` | list the numbered steps of the debugged scenario |
| `quit`, EOF | end the session with exit status 0 |

Watchpoints fire on recorded observations: trace records exist only where an
`observe:` step executed, so triggers report the exact observation label at
which a value changed (`WORLDLINE_DEBUG_TRIGGER=field:old->new@label`). A move
with no newer observation prints `UNCHANGED`. Unknown fields and commands fail
closed without killing the session.

## Implementation boundary

`worldline.minimization.ScenarioTimeTravel` is the neutral contract
(`prefix(scenario, seed, steps)`); adapters implement it over the controlled
runtime (`B173ScenarioRunner`). The CLI binds one reflectively via the same
`worldline.scenario.provider` property as `scenario run`.

## Evidence

The m16 smoke drives a five-step timeline through a thirteen-command script:
forward steps, reverse jumps, clamped goto, two watchpoint triggers, stale
observations between observe points, unknown-command rejection, and unwatch.
The filtered `WORLDLINE_DEBUG_*` transcript is hashed and frozen in
`smokes/m16-debug/smoke.properties`.

## Non-claims

M16 does not claim in-place state restoration (backward moves rebuild the run
from the start - exact because execution is deterministic), live probes between
observations (values are as of the last `observe:` record), watch expressions
beyond single trace fields, or multi-subject sessions.
