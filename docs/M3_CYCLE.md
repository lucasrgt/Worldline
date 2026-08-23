# Worldline v0.1.0 M3 Completion Audit

Status: **GO**

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Neutral public API | `worldline-api` compiles independently with no adapter or Minecraft classpath | PASS |
| Preserve v0.0.1 | Original first-tick signature remains `ac13115...780` | PASS |
| World API | Oracle-matched logical time and immutable entity snapshot | PASS |
| Block API | Oracle-matched stone/air reads and air-to-glass ID+metadata mutation | PASS |
| Entity API | Player appears as a live entity with matched ID, type, alive state, and position | PASS |
| Player API | Oracle-matched username, health, teleport, and hotbar selection | PASS |
| Lifecycle safety | Kernel rejects domain access outside `WORLD_LOADED`; retained adapter handles fail after close | PASS |
| Exact mappings | Client-cycle verifier checks 50 named/client/server symbol rows | PASS |
| Differential oracle | Two neutral-API and two official-JAR JVMs emit the same trace | PASS |
| Determinism | Pairwise fresh-process traces equal the frozen M3 signature | PASS |

Frozen signature:

```text
d38186377edc68f8080e568ffaba6559c4b3980fcf2a5311aac1b6ec7ebcc13c
```

Canonical qualification command:

```text
java tools/harness/Gate.java --smoke
```

The release is GO only when this command also passes v0.0.1 server/client
oracles, laboratory regression evidence, source budgets, strict Java 8
compilation, unit tests, release metadata checks, and the legal artifact
boundary.

## Cold reconstruction qualification

On 2026-08-16, the generated RetroMCP `minecraft` workspace was moved aside
and the canonical command rebuilt it from the pinned toolchain and
hash-verified official client JAR. The full server, client, M3, and laboratory
suite passed from that reconstruction. The rebuilt mapped client entrypoint
matched the pre-audit SHA-256:

```text
F3ABA176750D89E28559B9C85B070D1819ED310B83A6703DF002E768AD8EE14A
```

The prior generated workspace was then sent to the Windows Recycle Bin; it is
not part of the release tree or evidence boundary.
