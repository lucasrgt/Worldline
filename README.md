<h1 align="center">Worldline</h1>

<p align="center"><strong>A deterministic laboratory for Minecraft Beta 1.7.3.</strong></p>

<p align="center">
  <a href="#getting-started">Getting Started</a> |
  <a href="#mod-testing">Mod Testing</a> |
  <a href="#capabilities">Capabilities</a> |
  <a href="#documentation">Documentation</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/milestone-v0.7.0%20M9%20GO-2EA44F?style=flat-square" alt="Worldline v0.7.0 M9 GO">
  <img src="https://img.shields.io/badge/Minecraft-Beta%201.7.3-62B47A?style=flat-square" alt="Minecraft Beta 1.7.3">
  <img src="https://img.shields.io/badge/product-Java%208-5586A4?style=flat-square" alt="Java 8 product">
  <img src="https://img.shields.io/badge/harness-JDK%2021-6B5B95?style=flat-square" alt="JDK 21 harness">
  <img src="https://img.shields.io/badge/oracle-official%20JAR-CB8B2C?style=flat-square" alt="Official JAR behavioral oracle">
</p>

Worldline executes the real Minecraft Beta 1.7.3 runtime under external
control. It can freeze state, advance deterministic ticks, observe behavior,
restore checkpoints, replay failures, compare runs, and minimize a divergent
scenario.

The official Minecraft JAR is the behavioral oracle. Worldline does not treat
decompiled source as exact and does not distribute Mojang binaries, assets, or
decompiled game code.

<table>
<tr><td><b>Behavior before assumptions</b></td><td>Promoted behavior must match the hash-pinned official JAR or satisfy an explicit invariant.</td></tr>
<tr><td><b>Control at the boundary</b></td><td>Clock, input, RNG, filesystem, network, scheduling, and background threads are controlled without replacing game logic.</td></tr>
<tr><td><b>Canonical evidence</b></td><td>Traces, snapshots, bundles, mod-test results, and scenarios are strict, bounded, checksum-protected formats.</td></tr>
<tr><td><b>Fail closed</b></td><td>Unknown formats, mappings, dependencies, tools, runtime identities, and compatibility states are rejected.</td></tr>
<tr><td><b>Honest scope</b></td><td>A passing experiment does not silently become a stable public API.</td></tr>
</table>

---

## Current status

The official product line is **Worldline v0.7.0 / M9**. It includes the
controlled runtime, stable domain automation, durable reproduction artifacts,
trace analysis, descriptor-selected mod loading, differential mod testing, and
automatic scenario minimization.

| Stage | Contract | Status |
| --- | --- | --- |
| v0.0.1 | Headless boot, world load, controlled client tick, official-oracle match | GO |
| M2 | Virtual clock, input, RNG, filesystem, network, scheduler, thread control | GO |
| M3 | Stable world, player, entity, and block automation API | GO |
| M4 | Durable snapshots and fresh-process restore | GO |
| M5 | Portable reproduction bundles and replay CLI | GO |
| M6 | Canonical trace viewer and first-divergence analysis | GO |
| M7 | General mod packaging, inspection, compatibility, and loading | GO |
| M8 | Provenance-bound differential mod/version results | GO |
| M9 | Deterministic, budgeted scenario minimization | GO |
| GUI tree | Neutral inventory UI tree and Butter `HostUi` bridge | GO |
| Invariants | Item, block, entity, wear, health, and time rules | GO |
| Semantics | Closed role catalog, mappings, manifests, and coverage gate | GO |
| M10 | Native/offscreen rendering and Aero investigation | Not started |
| M11 | Mod API v2: lifecycle hooks, domain handles, scheduling, spawn/give | GO |
| M12 | One-command attested mod test runs | GO |
| M13 | Multi-mod dependency graphs with deterministic ordering | GO |
| M14 | Public scenario DSL with validated, runnable reproducers | GO |

Version and frozen signatures are authoritative in
[`release/worldline.properties`](release/worldline.properties). The promotion
rules and future direction are in [`docs/ROADMAP.md`](docs/ROADMAP.md).

---

## Getting started

### 1. Run the repository gate

Requirements:

- JDK 21 for the repository harness
- `tokei` 14 or newer for source ceilings
- Java 8 bytecode compatibility for product modules

```text
java tools/harness/Verify.java
```

This command validates source ceilings and module dependencies, compiles every
product module separately with warnings as errors, checks release metadata,
and runs the complete unit suite. Derived files stay under `.worldline/`.

### 2. Prepare runtime-bound work

Runtime verification requires a legitimate local copy of the official
Minecraft Beta 1.7.3 client JAR at:

```text
local/artifacts/minecraft-b1.7.3-client.jar
```

Bootstrap the pinned RetroMCP checkout and verify the exact artifact and
toolchain identities:

```text
java tools/toolchains/Bootstrap.java retromcp
java tools/harness/Verify.java --runtime
```

The JAR and generated workspace remain ignored. Worldline accepts only the
byte length and hashes frozen in the public artifact descriptor.

### 3. Run the complete evidence suite

```text
java tools/harness/Verify.java --smoke
```

The smoke profile builds the mapped adapters, runs the deterministic server
and client proofs, executes independent official-JAR oracles, and verifies all
frozen milestone signatures through M9.

### 4. Use the neutral lifecycle

The public API contains no Minecraft, RetroMCP, LWJGL, or mapped classes:

```java
try (AutomatedMinecraftRuntime runtime = B173Runtimes.create(1234L)) {
    runtime.bootHeadless();
    runtime.loadWorld(WorldSource.at(worldPath));

    runtime.world().setBlock(
        new BlockPosition(8, 65, 8),
        new BlockState(20, 0));
    runtime.player().selectHotbarSlot(2);
    runtime.tick(4);
}
```

`AutomatedMinecraftRuntime`, `GameWorld`, `GamePlayer`, `GameEntity`, and the
immutable coordinate and block values form the stable M3 domain contract.
Adapter factories remain runtime-specific.

---

## Mod testing

Worldline can load trusted, independently packaged Java mods and compare their
observable behavior. The package contract is stable; the external test-author
experience is a controlled-laboratory workflow with a one-command attested
run, while a published Maven/Gradle TestKit remains future work.

### Package a Worldline mod

A b1.7.3 test mod implements the tick entrypoint and may use lifecycle hooks,
the M3 domain handles, scheduling, entity spawn/removal, and inventory give:

```java
public final class GlassProbe implements B173Mod {
    @Override
    public void onLoad(B173ModContext context) {
        context.world().setBlock(new BlockPosition(8, 65, 8), new BlockState(20, 0));
        context.at(3, () -> context.setBlock(9, 65, 9, 20));
    }

    @Override
    public void onTick(B173ModContext context) {
        if (context.blockAt(8, 65, 8) == 0) {
            context.setBlock(8, 65, 8, 20);
        }
    }
}
```

Every JAR contains exactly one canonical descriptor at
`META-INF/worldline-mod.properties`. Format 2 adds dependency declarations:

```properties
format=2
id=example.glass-probe
version=1.0.0
entrypoint=example.GlassProbe
worldline.api=1
runtime=b1.7.3
requires=example.foundation>=1.0.0
```

Format 1 packages (without `requires=`) stay valid and dependency free.
Dependencies resolve deterministically: topological order, lexicographic
tie-breaking, fail-closed rejection of missing, unmet, self, and cyclic
requirements.

Inspect the package without executing mod code or loading Minecraft:

```text
java tools/replay/Replay.java mod inspect path/to/mod.jar
```

The inspector reports descriptor metadata, declared dependencies, whole-JAR
SHA-256, and exact runtime/API compatibility. Loading rejects incompatible
runtimes, API versions, entrypoint types, malformed descriptors, and changed
JARs.

### Run, record, and compare results

```text
java tools/replay/Replay.java mod test run mod.jar 17320110707 16 run.wlmtest
java tools/replay/Replay.java mod test record mod.jar run.wltrace run.wlmtest
java tools/replay/Replay.java mod test diff baseline.wlmtest candidate.wlmtest
```

`mod test run` executes the mod inside the controlled runtime and writes a
format 2 `.wlmtest` attesting `execution=controlled-runtime`, the seed, and
the tick count. `mod test record` binds a caller-supplied trace without
attesting execution. Comparison reports the earliest seed, schema, record, or
field divergence and names a known invariant when one applies.

### Create, validate, and run scenarios

```text
java tools/replay/Replay.java scenario create run.wlscenario observe:before block:8,65,8:20 tick observe:target
java tools/replay/Replay.java scenario validate run.wlscenario
java tools/replay/Replay.java scenario run run.wlscenario 4242 run.wltrace
```

The public DSL covers `tick[:n]`, `reseed:<long>`, `tap:<key>`,
`observe:<label>`, and `block:x,y,z:id[:meta]` steps with strict validation
and canonical rendering. Scenarios stay ordinary M9 artifacts, so the
minimizer applies unchanged.

### Testing flow

```text
mod source + descriptor
          |
          v
      mod JAR --inspect--> compatibility + artifact SHA-256
          |
          v
 controlled runtime --observe--> canonical .wltrace
          |                              |
          +---------- record ------------+
                         |
                         v
                    canonical .wlmtest
                         |
             +-----------+-----------+
             |                       |
             v                       v
        version diff          scenario minimizer
             |                       |
             v                       v
     first divergence          one-minimal case
```

Working examples live in
[`smokes/m7-mod-loading`](smokes/m7-mod-loading),
[`smokes/m8-mod-version-diff`](smokes/m8-mod-version-diff),
[`smokes/m9-scenario-minimization`](smokes/m9-scenario-minimization),
[`smokes/m11-mod-api`](smokes/m11-mod-api),
[`smokes/m12-mod-run`](smokes/m12-mod-run),
[`smokes/m13-mod-graph`](smokes/m13-mod-graph), and
[`smokes/m14-scenario-dsl`](smokes/m14-scenario-dsl).

---

## Capabilities

### Runtime control and automation

| Capability | What it provides |
| --- | --- |
| Lifecycle | Headless boot, world load, explicit tick, state validation, close |
| Domain API | World time, block read/write, entities, player state, teleport, hotbar selection |
| Boundary control | Virtual time, programmable input, RNG reseed, filesystem journal/failure injection, offline network |
| Scheduling | Externally requested ticks and timer-thread supervision |
| Semantic GUI | Screen, node, slot, open, close, and click through `GameUi` |
| Butter bridge | Reflective `HostUi` binding without importing Butter into `worldline-api` |

### Reproduction and analysis

| Capability | What it provides |
| --- | --- |
| Snapshot | Canonical replay-backed state with checksum and strict restore identity |
| Reproduction bundle | Snapshot plus exact Worldline, runtime, client, and toolchain provenance |
| Canonical trace | Versioned ordered observations shared by subject and official oracle |
| First divergence | Exact seed, schema, record, field, and ordered left/right values |
| Mod-test result | Mod descriptor and artifact provenance bound to one canonical trace |
| Scenario minimization | Deterministic delta debugging with evaluation budget and one-minimality proof |

### Evidence and governance

| Capability | What it provides |
| --- | --- |
| Official oracle | Independent execution against the hash-pinned Mojang artifact |
| Invariant engine | Six fail-closed conservation and monotonicity rule families |
| Semantic catalog | Closed 24-category role graph with mapping and adapter coverage |
| Optimization SDK | Source-only `OptimizationRef`, portable records, owner-controlled catalogs |
| Release gate | Frozen signatures, legal artifact scan, dependency order, source ceilings |

The optimization SDK records Worldline-owned changes under
`optimizations/catalog/`. Other projects keep their own optimization records
in their repositories; Worldline evidence may reference their stable IDs but
does not copy their implementation catalogs. See
[`docs/OPTIMIZATION_SDK.md`](docs/OPTIMIZATION_SDK.md).

---

## Architecture

```text
scenario author
      |
      v
worldline-api <----------- worldline-kernel ----------> b1.7.3 adapter
      |                         |                            |
      |                         v                            v
      |                   controlled policy          mapped real client
      |                                                      |
      +---------------- canonical observation ---------------+
                                  |
                                  v
                      trace / snapshot / bundle
                                  |
             +--------------------+--------------------+
             |                    |                    |
             v                    v                    v
          analysis             modtest             minimization
             |                    |                    |
             +--------------------+--------------------+
                                  |
                                  v
                                 CLI

official JAR oracle -------- same trace protocol -------- subject
```

Modules are physical source roots compiled separately in the dependency order
declared by `harness.properties`. The API has no product dependencies. The
kernel imports only the API. Trace analysis, mod packaging, result comparison,
and minimization stay independent of mapped Minecraft classes.

The full dependency map and evidence boundaries are in
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## CLI reference

After a successful verification, use the repository launcher:

```text
java tools/replay/Replay.java replay <bundle.wlrb>

java tools/replay/Replay.java trace show <run.wltrace>
java tools/replay/Replay.java trace diff <left.wltrace> <right.wltrace>

java tools/replay/Replay.java mod inspect <mod.jar>
java tools/replay/Replay.java mod test record <mod.jar> <trace.wltrace> <result.wlmtest>
java tools/replay/Replay.java mod test diff <left.wlmtest> <right.wlmtest>
java tools/replay/Replay.java mod test run <mod.jar> <seed> <ticks> <result.wlmtest>

java tools/replay/Replay.java scenario create <output.wlscenario> [step ...]
java tools/replay/Replay.java scenario inspect <scenario.wlscenario>
java tools/replay/Replay.java scenario validate <scenario.wlscenario>
java tools/replay/Replay.java scenario run <scenario.wlscenario> <seed> <trace.wltrace>
```

Neutral inspection and comparison commands do not require Minecraft, mapped
classes, RetroMCP, or native libraries on their product classpaths.

---

## Documentation

| Document | Use it for |
| --- | --- |
| [Vision](docs/VISION.md) | Project purpose, evidence philosophy, and determinism boundary |
| [Architecture](docs/ARCHITECTURE.md) | Module order, dependency direction, adapters, and smokes |
| [Roadmap](docs/ROADMAP.md) | Official milestones, promotion rule, and future direction |
| [M2 runtime control](docs/M2_RUNTIME.md) | Clock, input, RNG, filesystem, network, scheduling, threads |
| [M3 domain API](docs/M3_API.md) | Stable world, player, entity, and block contract |
| [M4 snapshots](docs/M4_SNAPSHOT.md) | Snapshot format, restoration, and portability limits |
| [M5 bundles](docs/M5_BUNDLE.md) | Reproduction envelope and replay identity |
| [M6 traces](docs/M6_TRACE.md) | Canonical trace grammar, viewer, and diff semantics |
| [M7 mods](docs/M7_MODS.md) | Descriptor, compatibility, loading, and trust boundary |
| [M8 results](docs/M8_RESULTS.md) | `.wlmtest` format, recording, and comparison |
| [M9 minimization](docs/M9_MINIMIZATION.md) | `.wlscenario`, evaluator contract, and guarantees |
| [M11 mod API v2](docs/M11_MOD_API.md) | Lifecycle hooks, domain handles, scheduling, spawn/give |
| [M12 mod test run](docs/M12_MOD_RUN.md) | Attested one-command execution and result format 2 |
| [M13 mod graph](docs/M13_MOD_GRAPH.md) | Format 2 dependencies and deterministic ordering |
| [M14 scenario DSL](docs/M14_SCENARIO_DSL.md) | Public step grammar, validation, and execution |
| [GUI tree](docs/GUI_TREE.md) | Semantic inventory UI and Butter bridge |
| [Invariants](docs/INVARIANTS.md) | Observation model and fail-closed rules |
| [Semantics](docs/SEMANTICS.md) | Roles, mappings, manifests, confidence, and coverage |
| [Optimization SDK](docs/OPTIMIZATION_SDK.md) | Stable optimization IDs and catalog ownership |
| [Changelog](CHANGELOG.md) | Stable scope and release history |
| [Engineering guide](AGENTS.md) | Behavioral constitution and canonical gates |

Every promoted milestone also has a `*_CYCLE.md` completion audit and a smoke
`MAP.md` defining its exact oracle, mappings, exclusions, and pass conditions.

---

## Compatibility and scope

Worldline currently targets:

- Minecraft Beta 1.7.3
- Java 8 bytecode for product modules
- JDK 21 for the repository harness
- a hash-pinned official client JAR kept only under ignored local storage
- a pinned RetroMCP revision for mapped adapter construction
- trusted local Worldline mod JARs using runtime `b1.7.3` and mod API `1`

Worldline is not a general Minecraft launcher, a security sandbox, or a
drop-in loader for legacy ModLoader/Forge mods. The current public mod boundary
does not provide permissions, hot reload, arbitrary private-state capture,
classloader namespacing between mods, or a published Maven/Gradle TestKit.
Native/offscreen framebuffer evidence and Aero integration remain M10 work.
Entity spawning uses a closed semantic registry; unregistered types fail
closed.

Never commit or distribute the official Minecraft JAR, original assets, or
decompiled Minecraft sources. Public artifacts contain original Worldline
code, metadata, mappings, patches, transforms, and reproducible evidence only.

---

## Build and contribute

### Canonical gate

```text
java tools/harness/Verify.java
```

### Runtime identity and mapped adapter

```text
java tools/harness/Verify.java --runtime
```

### Complete official-oracle evidence

```text
java tools/harness/Verify.java --smoke
```

There is no total line budget. Tests are unlimited, but maintained source files
must stay within their per-file ceilings:

| Kind | Maximum `tokei` code lines |
| --- | ---: |
| Product | 250 |
| Harness | 300 |
| Smoke | 150 |
| Adapter | 150 |

Contributions must preserve module dependency order, fail closed on missing
tools and unknown contracts, keep game types outside neutral product modules,
and add differential or invariant evidence before claiming a controlled
boundary is implemented.

---

## Project transparency

Worldline is developed with substantial AI assistance. Architecture, product
decisions, review, and release responsibility remain with the maintainer. The
repository keeps source, mappings, tests, frozen signatures, and non-claims
public so status can be inspected rather than inferred from the development
process.

Maintainer: [lucasrgt](https://github.com/lucasrgt)
