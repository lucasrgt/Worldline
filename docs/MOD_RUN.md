# M12 Attested Mod Test Run Contract

## Scope

M12 collapses the mod testing loop into one command that executes the mod
inside the controlled runtime and binds the produced trace to that execution.
`mod test record` remains available for caller-supplied traces and keeps its
original format; it still does not attest execution.

## Command

```text
worldline mod test run <mod.jar> <seed> <ticks> <result.wlmtest>
```

The command inspects the package, loads the descriptor-selected entrypoint,
boots the controlled b1.7.3 runtime under `seed`, installs the mod, advances
exactly `ticks` controlled ticks, records one observation per tick, and writes
a canonical result with create-new semantics. Exit status is 0 on success, 1
on invalid input or failure, 2 on usage errors.

## Result format 2

`WORLDLINE-MOD-TEST/2` extends the v1 body with three attestation fields
between `worldline.api` and `trace.sha256`:

```text
execution=controlled-runtime
seed=<decimal long>
ticks=<1..100000>
```

Parsing accepts both versions and rejects unknown executions, malformed
numbers, and any non-canonical byte layout. Comparison semantics are
unchanged: behavior divergence is trace divergence regardless of version.

## Provider binding

The CLI loads the runner reflectively via the `worldline.modtest.provider`
system property (default `worldline.b173.B173ModTestRunner`). The neutral
`worldline.modtest.ModTestRunner` interface lives in a game-independent
module; adapters own implementations.

## Evidence

The m12 smoke drives the public launcher end to end: format 2 inspection, two
byte-identical executed results under one seed, EQUAL diff, and corruption
rejection. The frozen report intentionally excludes the result digest (JAR
packaging embeds timestamps) and freezes the stable trace digest instead.

## Non-claims

M12 does not claim parallel execution, distributed runners, mod marketplace
trust, signature-based authorship, or replay of results without the pinned
runtime. Attestation proves the controlled run happened inside this command;
it does not certify mod intent or safety.
