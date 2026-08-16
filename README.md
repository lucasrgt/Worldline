# Worldline

Current official milestone: **Worldline v0.2.0 - M4 Durable Snapshot (GO)**.

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
control, and an independently packaged benchmark mod JAR.

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

The gate then runs `smokes/lab-cycle`, restores deterministic checkpoints in
fresh clients, compares hypotheses, exercises GUI selectors, and compiles and
loads `probe-mod.jar`; its scope is defined in `smokes/lab-cycle/MAP.md`.

The client, M3, M4, and lab runners deliberately raised the tooling budget to 1,600
code lines while retaining the 300-line per-file ceiling. Product
code remains capped at 1,000 lines and 250 lines per file. Smoke drivers and
oracles have their own enforced 1,000-line total and 150-line per-file budget, so
integration behavior cannot hide outside the product and tooling counts.

See `ARCHITECTURE.md` for module boundaries and `AGENTS.md` for the behavioral
and engineering constitution. `FIRST_CYCLE.md` is the v0.0.1 GO audit;
`LAB_CYCLE.md` is the seven-step laboratory GO audit.
`M3_CYCLE.md` is the v0.1.0 stable domain-API GO audit.
`M4_CYCLE.md` is the v0.2.0 durable-snapshot GO audit.

Version and frozen evidence are authoritative in
`release/worldline.properties`. See `CHANGELOG.md` for stable scope and
`docs/ROADMAP.md` for the distinction between official and experimental stages.

## Legal boundary

Do not place the official JAR, original assets, or decompiled Minecraft source
in Git. Local artifacts and experiments belong under the ignored `local/`
directory. Public work should consist of original code, mappings, patches, and
transforms.
