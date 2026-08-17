# Worldline M2 Controlled Runtime GO Audit

Status: **GO**

M2 does not change the public product version. Worldline remains v0.7.0 / M9.
This audit promotes already-executable boundary evidence to a stable milestone.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Virtual clock | Subject asserts 16 ticks advance the installed clock by 800 ms to `1_000_800` and that the client `systemTime` reads the same value | PASS |
| Programmable input | Tick 2 injects the same key events on subject and official oracle; both end at hotbar slot 2 | PASS |
| RNG reseed | Controlled world/player streams are reseeded; `rngSeed` is a v2 field and matches across four JVMs | PASS |
| Filesystem journal | Subject records `world.loadInfo` and `chunk.load`; `failNext("world.loadInfo")` fails closed | PASS |
| Offline network | Subject requires `networkConnected() == false`; neither side constructs a network handler | PASS |
| Scheduler | Subject queues the hotbar tap with `afterTicks(2, ...)`; the official side injects the same keys at tick 2 | PASS |
| Thread control | Subject observes the vanilla timer thread alive during the run and stopped after close | PASS |
| Differential oracle | Two mapped and two official client JVMs emit the same 16-tick v2 state signature | PASS |
| Boundary marker | Subject emits `WORLDLINE_BOUNDARIES=clock,input,rng,scheduler,filesystem,network,threading` | PASS |

Clock, filesystem failure injection, offline-network, and timer-thread
lifecycle are subject-side assertions. The official-JAR match freezes the
observable tick, input, and RNG effects, not a second virtual clock.

Frozen 16-tick state signature:

```text
e8cdeba39a44b772a70c48c0acd9ae3983f3d95a8c10c545df5d66fb953db554
```

Canonical qualification command:

```text
java tools/harness/Verify.java --smoke
```

The release is GO only when this command also passes the v0.0.1 server/client
oracles, later milestone smokes, laboratory regression evidence, source
budgets, strict Java 8 compilation, unit tests, release metadata checks, and
the legal artifact boundary. The contract and non-claims are in
`docs/M2_RUNTIME.md`.
