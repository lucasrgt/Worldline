# Worldline

Current official milestone: **Worldline v0.6.0 - M8 Differential Mod Testing (GO)**.

Worldline is an experimental controlled runtime for Minecraft Beta 1.7.3. Its
first goal is deliberately small: boot the real game headlessly, load a world,
advance one externally controlled tick, and compare the observable result with
the official vanilla artifact.

The controlled-runtime baseline and first laboratory cycle are complete. The repository contains a
small public lifecycle contract, an independently compiled kernel, an
independent canonical-trace protocol, and a fail-closed verification gate. Its
strongest smoke constructs the real mapped client tick object graph without a
window, loads a deterministic in-memory world, advances exactly one requested
`Minecraft.runTick()`, and matches the equivalent execution from the official
client JAR through 16 ticks in four fresh JVMs. A second two-process lab proof
adds replay-backed checkpoints, restoration, branching, semantic inventory GUI
control, and descriptor-selected independently packaged benchmark mod JARs.

The stable M3 surface adds `AutomatedMinecraftRuntime`, `GameWorld`,
`GamePlayer`, `GameEntity`, `BlockPosition`, `BlockState`, and `GamePosition`.
It supports neutral world time and block access, block mutation, active-entity
enumeration, player identity/state, teleportation, and hotbar selection. See
`docs/M3_API.md` for lifecycle rules and non-claims.

M4 adds `SnapshotMinecraftRuntime` and immutable `RuntimeSnapshot` artifacts.
The b1.7.3 adapter captures a canonical replay-backed document with an embedded
checksum, restores it in a fresh runtime, and rejects corrupt, non-canonical,
wrong-version, or wrong-runtime input. See `docs/M4_SNAPSHOT.md` for the exact
format and portability boundary.

M5 adds a canonical `ReproductionBundle` that embeds the M4 snapshot and
declares the exact Worldline version, runtime ID, official client SHA-256, and
RetroMCP revision required to replay it. A neutral CLI loads a runtime provider
and restores the bundle without distributing the official JAR or mapped game
classes. See `docs/M5_BUNDLE.md` for the format and command contract.

M6 adds a strict parser for schema-bearing `v2` state traces, a stable tabular
viewer, and a first-divergence analyzer that reports the earliest seed, schema,
record, or field mismatch with exact indices and values. Both viewer and diff
are available through the neutral CLI without requiring Minecraft runtime
inputs. See `docs/M6_TRACE.md` for ordering and exit-code semantics.

M7 adds strict, bounded mod-JAR descriptors, SHA-256 provenance, explicit
runtime/API compatibility, and isolated descriptor-selected entrypoint loading.
The neutral CLI can inspect compatibility without executing mod code or loading
Minecraft. See `docs/M7_MODS.md` for the exact package and trust boundary.

M8 adds canonical `.wlmtest` results that bind an exact mod artifact and
descriptor to a canonical state trace. The neutral CLI records results and
compares mod versions with M6 first-divergence semantics. See
`docs/M8_RESULTS.md` for the result format and attestation boundary.

## Verify

Requirements:

- JDK 21 for the repository harness;
- `tokei` 14 or newer for source budgets.

Run the canonical gate from the repository root:

```text
java tools/harness/Verify.java
```

The gate checks product and harness line budgets, compiles product modules to
Java 8 bytecode in their declared dependency order, compiles the tests, and
runs every registered test suite. Derived output is written to the ignored
`.worldline/build/` directory.

Runtime-bound work uses the stricter profile:

```text
java tools/harness/Verify.java --runtime
```

It additionally requires `local/artifacts/minecraft-b1.7.3-client.jar` and
verifies its byte length, SHA-1, and SHA-256 against the committed descriptor.
The descriptor is metadata; the JAR remains ignored and local.

Bootstrap the pinned RetroMCP upstream checkout and CLI under `local/` with:

```text
java tools/toolchains/Bootstrap.java retromcp
```

The bootstrap refuses modified or foreign checkouts, detaches at the committed
revision, and builds the CLI with the upstream Gradle wrapper. Once a clean
pinned checkout and non-empty CLI exist, repeated runs are local no-ops.

Run the complete evidence suite end to end with:

```text
java tools/harness/Verify.java --smoke
```

The command prepares the frozen b1.7.3 RetroMCP workspace and verifies both
levels of evidence. The original server smoke proves an eight-tick
`World.tick()` fixture. The completion smoke decompiles and recompiles the
client when necessary, calls `bootHeadless()`, `loadWorld(...)`, and `tick(1)`,
and verifies the compiled path reaches mapped `Minecraft.runTick()`. A separate
oracle compiled directly against the hash-verified official client JAR reaches
`net.minecraft.client.Minecraft.k()`. Two fresh JVMs per side must be
internally deterministic, match across the differential boundary, and equal
the committed SHA-256 signature. See
`smokes/controlled-client-tick/MAP.md` for the exact symbol map, external
boundary inventory, headless substitutions, and pass conditions.

The gate next runs `smokes/m3-domain-api`. Two fresh JVMs exercise the stable,
neutral Worldline API while two independent JVMs perform the equivalent
operations directly against the official obfuscated client JAR. All four must
produce the frozen M3 trace and signature documented in
`smokes/m3-domain-api/MAP.md`.

`smokes/m4-durable-snapshot` then captures the same logical state in two fresh
JVMs, requires byte-identical snapshot artifacts, restores each in new JVMs,
matches the direct official-client state at tick 4, and proves checksum failure
on a corrupted artifact plus explicit rejection of unknown versions and runtime
identities. Its scope is defined in
`smokes/m4-durable-snapshot/MAP.md`.

`smokes/m5-reproduction-bundle` next produces byte-identical bundles in two
fresh pack JVMs, replays the original and a copied artifact through the public
CLI, matches the direct official-client state, and rejects corruption plus
incompatible client and toolchain declarations. Its exact boundary is in
`smokes/m5-reproduction-bundle/MAP.md`.

`smokes/m6-trace-explorer` independently reruns the mapped and official clients,
views their equal 17-record traces, injects one `tick9.slot` change, and requires
the CLI to locate that exact first divergence in both comparison directions.
It also rejects a malformed schema; details are in
`smokes/m6-trace-explorer/MAP.md`.

`smokes/m7-mod-loading` then builds two descriptor-selected mods, executes their
distinct glass and gold effects through the real controlled client, and rejects
wrong runtime, API, entrypoint type, malformed, and missing descriptor cases.
Its exact evidence boundary is in `smokes/m7-mod-loading/MAP.md`.

`smokes/m8-mod-version-diff` executes a no-mod baseline and versions `1.0.0`
and `1.1.0` of the same mod twice each, freezes deterministic JAR/trace/result
hashes, and requires exact baseline/version and version/version divergences.
Its scope is in `smokes/m8-mod-version-diff/MAP.md`.

The gate then runs `smokes/lab-cycle`, restores deterministic checkpoints in
fresh clients, compares hypotheses, exercises GUI selectors, and compiles and
loads `probe-mod.jar`; its scope is defined in `smokes/lab-cycle/MAP.md`.

The client, M3, M4, M5, M6, M7, M8, and lab runners deliberately raised the tooling budget to 2,250
code lines while retaining the 300-line per-file ceiling. Product
code remains capped at 1,350 lines and 250 lines per file. Smoke drivers and
oracles have their own enforced 1,250-line total and 150-line per-file budget, so
integration behavior cannot hide outside the product and tooling counts.

See `ARCHITECTURE.md` for module boundaries and `AGENTS.md` for the behavioral
and engineering constitution. `FIRST_CYCLE.md` is the v0.0.1 GO audit;
`LAB_CYCLE.md` is the seven-step laboratory GO audit.
`M3_CYCLE.md` is the v0.1.0 stable domain-API GO audit.
`M4_CYCLE.md` is the v0.2.0 durable-snapshot GO audit.
`M5_CYCLE.md` is the v0.3.0 reproduction-bundle and replay-CLI GO audit.
`M6_CYCLE.md` is the v0.4.0 trace-viewer and first-divergence GO audit.
`M7_CYCLE.md` is the v0.5.0 general-mod-loading and compatibility GO audit.
`M8_CYCLE.md` is the v0.6.0 differential-mod-testing GO audit.

After preparing the local runtime with the canonical smoke gate, replay a
bundle with:

```text
java tools/replay/Replay.java replay path/to/reproduction.wlrb
```

Inspect or compare canonical state traces without a Minecraft runtime:

```text
java tools/replay/Replay.java trace show run.wltrace
java tools/replay/Replay.java trace diff baseline.wltrace candidate.wltrace
```

Inspect a local Worldline mod package without executing it:

```text
java tools/replay/Replay.java mod inspect path/to/mod.jar
```

Record and compare provenance-bound mod test results:

```text
java tools/replay/Replay.java mod test record mod.jar run.wltrace run.wlmtest
java tools/replay/Replay.java mod test diff baseline.wlmtest candidate.wlmtest
```

Version and frozen evidence are authoritative in
`release/worldline.properties`. See `CHANGELOG.md` for stable scope and
`docs/ROADMAP.md` for the distinction between official and experimental stages.

## Legal boundary

Do not place the official JAR, original assets, or decompiled Minecraft source
in Git. Local artifacts and experiments belong under the ignored `local/`
directory. Public work should consist of original code, mappings, patches, and
transforms.
