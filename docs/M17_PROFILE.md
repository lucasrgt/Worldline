# M17 Tick Profiling Contract

## Scope

M17 attaches the missing measurement loop to the optimization SDK:
per-tick wall-clock profiling of public-grammar scenarios with an optional
machine-relative budget gate. Timing values are honest about being
machine-relative - they appear in reports but are never frozen evidence;
frozen profile evidence asserts structure and gate outcomes only.

## Command

```text
worldline profile <scenario.wlscenario> <seed> [budget.properties]
```

The command executes every step of the scenario under `seed` against the
controlled runtime, sampling each controlled tick. Only ticks are sampled;
`block`, `tap`, `reseed`, and `observe` steps are setup and unmeasured. Each
sample records total tick time and, separately, time spent inside mod
callbacks (via the M11 hook layer), so mod cost is attributable.

Output is a canonical checksum-protected `WORLDLINE-PROFILE/1` report:
per-tick samples plus deterministic aggregates (`total`, `mean`,
nearest-rank `median` and `p95`, `max`) for tick time and mod totals with a
percent share. The same run also emits its behavioral trace digest so timing
and behavior travel together; traces never contain timings.

## Budget gate

Budget files are strict properties with optional keys:

```properties
tick.total.nanos.max=...
tick.mean.nanos.max=...
tick.median.nanos.max=...
tick.p95.nanos.max=...
tick.max.nanos.max=...
mod.share.percent.max=...
```

Unknown keys fail closed. With a budget, exit status is 0 within limits or 3
with one `violation=<key>=<actual><limit>` line per breach - suitable as a
pre-push regression gate on any machine. Budgets are local commitments, not
portable claims; reference optimization IDs stay owned by catalog records.

## Provider boundary

`worldline.profiling.ProfiledRunner` returns a `TickProfiledRun` binding
samples to the behavioral trace. Adapters implement it (`B173ProfiledRunner`);
the CLI binds reflectively via `worldline.profile.provider`.

## Evidence

The m17 smoke profiles an eight-tick scenario through the public launcher:
sample count and aggregate ordering asserted, identical behavioral digests
across runs, a deliberately tight budget rejected with exactly two violations,
and a generous budget passed. Frozen SHA-256 lives in
`smokes/m17-profile/smoke.properties`.

## Non-claims

M17 does not claim cross-machine comparability of absolute nanos, in-tick
phase attribution beyond the mod/game split, flamegraph export, statistical
warmup policies, or automated optimization-record updates.
