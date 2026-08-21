<h1 align="center">Worldline</h1>

<p align="center"><strong>A deterministic laboratory for Minecraft Beta 1.7.3.</strong></p>

<p align="center">
  <a href="#getting-started">Getting Started</a> |
  <a href="#worldline-testkit">TestKit</a> |
  <a href="#extensions">Extensions</a> |
  <a href="#mod-testing">Mod Testing</a> |
  <a href="#capabilities">Capabilities</a> |
  <a href="#documentation">Documentation</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/milestone-v1.446.0%20M469%20GO-2EA44F?style=flat-square" alt="Worldline v1.446.0 M469 GO">
  <img src="https://img.shields.io/badge/Minecraft-Beta%201.7.3-62B47A?style=flat-square" alt="Minecraft Beta 1.7.3">
  <img src="https://img.shields.io/badge/product-Java%208-5586A4?style=flat-square" alt="Java 8 product">
  <img src="https://img.shields.io/badge/harness-JDK%2021-6B5B95?style=flat-square" alt="JDK 21 harness">
  <img src="https://img.shields.io/badge/oracle-official%20JAR-CB8B2C?style=flat-square" alt="Official JAR behavioral oracle">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-4C566A?style=flat-square" alt="MIT license"></a>
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

The official main line is **Worldline v1.446.0 / M469**. Its stable foundation
includes the controlled runtime, domain automation, durable reproduction
artifacts, trace analysis, descriptor-selected mod loading, differential mod
testing, and automatic scenario minimization. Later milestones add bounded
native-render, Aero, dedicated-server, protocol, multiplayer, and vanilla
behavior evidence without silently widening those early public APIs.

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
| GUI tree | Neutral inventory UI tree | GO |
| Invariants | Item, block, entity, wear, health, and time rules | GO |
| Semantics | Closed role catalog, mappings, manifests, and coverage gate | GO |
| M10-M19 | Native rendering, Aero qualification, attribution, and bounded performance policies | GO |
| Mod API v2 | Lifecycle hooks, domain handles, scheduling, spawn/give surface | GO |
| Attested runs | One-command `mod test run` binding execution provenance | GO |
| Mod graphs | Format 2 dependencies with deterministic topological ordering | GO |
| Scenario DSL | Public step grammar with validation and controlled execution | GO |
| M20-M67 | Official server lifecycle, protocol-14 control, multiplayer, inventory, crafting, and combat | GO |
| M68-M110 | Real Aero client composition, renderer census, cell pages, cache pressure, and controls | GO |
| M111-M469 | Official vanilla world, block, item, entity, crafting, AI, damage, and death behavior sets | GO |
| M469 | Current release: official void walk-off death and respawn set | GO |
| TestKit 0.x | Experimental Java specs, isolated runner, reporters, artifacts, and CLI | EXPERIMENTAL |

Version and frozen signatures are authoritative in
[`release/worldline.properties`](release/worldline.properties). The promotion
rules and future direction are in [`docs/ROADMAP.md`](docs/ROADMAP.md).

---

## Getting started

### 1. Run the repository gate

Requirements:

- JDK 21 for the repository harness
- `tokei` 14 or newer for source ceilings
- Java 8 bytecode compatibility for `api`, `testmodel`, `testapi`, adapters, and mod-facing contracts
- Java 21 bytecode for the TestKit runner, CLI, and repository tooling

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
and client proofs, executes independent official-JAR oracles, and verifies the
frozen signatures registered by the current release.

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

## Worldline TestKit

Worldline TestKit 0.x turns the controlled runtime into an external-mod test
experience without freezing an untested 1.0 API. Specs are ordinary Java 8,
so existing Java formatters, syntax highlighting, completion, refactoring, and
debuggers work without a custom editor plugin:

```java
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

public final class GlassProbeWorldlineTest extends WorldlineSpec {
    @Override protected void define() {
        describe("GlassProbe", () -> {
            test("places glass", worldline().runtime("b1.7.3").seed(173L)
                    .mod("build/glass-probe.jar").run(context -> {
                context.setBlock(pos(8, 65, 8), block("b1.7.3:glass"));
                context.tick();
                expect(context.block(pos(8, 65, 8)))
                        .toEqual(block("b1.7.3:glass"));
            })).tag("block").timeout(5_000);
        });
    }
}
```

`test` and `it` are exact aliases; `describe` and `suite` are exact aliases.
Hooks, table tests, `.skip()`, `.todo()`, `.only()`, explicit retries,
snapshots, filters, watch mode, and named minimizable steps are included.
Top-level specs are discovered automatically, while `--classpath` exposes the
mod's separately compiled product classes without weakening source isolation.

Initialize the isolated Gradle project in a mod repository:

```text
java -jar worldline-test-runner-0.2.1.jar init
tests/worldline/gradlew.bat worldlineDoctor worldlineTest
```

The generated project pins Gradle, keeps every spec under
`tests/worldline/src/test/java`, creates an ignored empty official-JAR drop
zone, and emits JUnit XML plus Worldline evidence. The binary plugin is
`io.github.lucasrgt.worldline.test`; it never changes the mod's legacy build.

Useful tasks mirror the TestKit CLI:

```text
worldlineTest worldlineTestList worldlineTestWatch worldlineTestInspect
worldlineTestUpdateSnapshots worldlineTestMinimize worldlineDoctor
```

Build deterministic ignored JARs for external projects:

```text
java tools/testkit/TestKitPackage.java
```

The runner creates a fresh runtime for every attempt and retry, holds an
exclusive cross-process runtime lock, never executes official runtimes
concurrently, and marks retry-only passes as `FLAKY`. Failures can emit a
canonical trace, durable runtime snapshot, provenance-bound `.wlmtest`, named
scenario, minimized scenario, and timeout inventory. Reporters include
default, verbose, dot, JSON, JUnit, and agent output.

Only promoted semantic mappings enter the friendly selector catalog. Unknown
or read-only mappings fail with a stable `WLTEST` diagnostic instead of
guessing an ID or obfuscated field.

See the [Gradle adoption guide](docs/GRADLE_TESTKIT.md), the complete
[TestKit guide](docs/TESTKIT.md), and the
[ten-spec, 30-test example project](examples/testkit/README.md).

---

## Extensions

A Worldline 0.x extension is an external project-owned integration layer. It
combines ordinary Java TestKit specs, optional typed fixtures and assertions,
an optional runtime provider, and optimization records without moving the
mod's implementation into Worldline.

Choose the smallest integration lane that fits the project:

| Need | Extension surface |
| --- | --- |
| Test dependency-free mod logic | TestKit specs plus `--classpath` |
| Test a descriptor-packaged `B173Mod` | TestKit specs plus `--mod` |
| Support another controlled runtime | `TestRuntimeProvider` and a Worldline adapter |
| Qualify performance work | Project-owned `optimizations/catalog/` records plus evidence |
| Load legacy ModLoader, StationAPI, or Aero mods | A separately qualified loader adapter; not provided by the default provider |

In 0.x, an extension is a repository convention rather than an executable
plugin format. It must not bypass mapped-runtime ownership, register guessed
semantic mappings, or place Minecraft classes in the neutral TestKit API.

The complete [extension authoring guide](docs/EXTENSIONS.md) includes a
recommended repository layout, compile and run flow, provider boundary,
optimization workflow, promotion checklist, and a concrete Beta Energistics
blueprint.

---

## Mod testing

Worldline can load trusted, independently packaged Java mods and compare their
observable behavior. The package contract is stable. TestKit 0.x is the new
experimental external authoring layer; the lower-level commands remain useful
for inspecting and comparing canonical artifacts directly.

### Package a Worldline mod

A b1.7.3 test mod implements the tick entrypoint and may use lifecycle hooks,
the M3 domain handles, scheduling, entity spawn/removal, and inventory give:

```java
public final class GlassProbe implements B173Mod {
    @Override
    public void onLoad(B173ModContext context) {
        context.world().setBlock(new BlockPosition(8, 65, 8), new BlockState(20, 0));
        context.player().give(265, 5);
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
SHA-256, and exact runtime/API compatibility. Loading rejects incompatible runtimes, API
versions, entrypoint types, malformed descriptors, and changed JARs.

### Record and compare results

```text
java tools/replay/Replay.java mod test record mod.jar run.wltrace run.wlmtest
java tools/replay/Replay.java mod test diff baseline.wlmtest candidate.wlmtest
java tools/replay/Replay.java mod test run mod.jar 17320110707 16 run.wlmtest
```

`mod test run` executes the mod inside the controlled runtime and writes a
format 2 `.wlmtest` attesting `execution=controlled-runtime`, the seed, and
the tick count. `mod test record` binds a caller-supplied trace without
attesting execution.

A `.wlmtest` binds the exact mod ID, version, entrypoint, artifact hash,
runtime, Worldline API, and canonical trace. Comparison reports the earliest
seed, schema, record, or field divergence and names a known invariant when one
applies.

`mod test record` binds caller-supplied inputs; it does **not** attest that the
trace came from executing that JAR. The M8 smoke owns the stronger execution
boundary. TestKit's runtime runner owns the stronger one-command execution and
automatically records a `.wlmtest` on failure when a mod JAR is configured.

### Create and minimize scenarios

```text
java tools/replay/Replay.java scenario create run.wlscenario tick observe:target
java tools/replay/Replay.java scenario inspect run.wlscenario
```

The public DSL covers `tick[:n]`, `reseed:<long>`, `tap:<key>`,
`observe:<label>`, and `block:x,y,z:id[:meta]` steps with strict validation
and canonical rendering (`scenario validate`). Scenarios stay ordinary M9
artifacts, so the minimizer applies unchanged; `scenario run <scenario>
<seed> <trace>` executes one against the controlled runtime.

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

external Java 8 spec -> testapi -> testmodel -> testkit (Java 21) -> provider SPI
                                      |                    |
                                      v                    v
                              reporters/artifacts      b1.7.3 adapter

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

java tools/replay/Replay.java test
java tools/replay/Replay.java test run <spec.jar|classes> [spec.class] [options]
java tools/replay/Replay.java test list <spec.jar|classes> [spec.class]
java tools/replay/Replay.java test watch <spec.jar|classes> [spec.class] [options]
java tools/replay/Replay.java test minimize <spec.jar|classes> [spec.class] [options]
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
| [Mod API v2](docs/MOD_API_V2.md) | Lifecycle hooks, domain handles, scheduling, spawn/give |
| [Attested runs](docs/MOD_RUN.md) | One-command execution and result format 2 |
| [Mod graphs](docs/MOD_GRAPH.md) | Format 2 dependencies and deterministic ordering |
| [Scenario DSL](docs/SCENARIO_DSL.md) | Public step grammar, validation, and execution |
| [M10 native render](docs/M10_RENDER.md) | Offscreen framebuffer oracle and Aero artifact boundary |
| [M11 attribution](docs/M11_ATTRIBUTION.md) | Aero qualification and render-work attribution |
| [GUI tree](docs/GUI_TREE.md) | Semantic inventory UI and Butter bridge |
| [Invariants](docs/INVARIANTS.md) | Observation model and fail-closed rules |
| [Semantics](docs/SEMANTICS.md) | Roles, mappings, manifests, confidence, and coverage |
| [Optimization SDK](docs/OPTIMIZATION_SDK.md) | Stable optimization IDs and catalog ownership |
| [TestKit 0.x](docs/TESTKIT.md) | Java specs, runner isolation, reporters, snapshots, artifacts, and CLI |
| [Gradle adoption](docs/GRADLE_TESTKIT.md) | Isolated build, plugin tasks, oracle profiles, migration, IDE, and CI |
| [Extension authoring](docs/EXTENSIONS.md) | External test layers, provider boundary, optimization evidence, and legacy-mod limits |
| [Changelog](CHANGELOG.md) | Stable scope and release history |
| [Engineering guide](AGENTS.md) | Behavioral constitution and canonical gates |

Every promoted milestone also has a `*_CYCLE.md` completion audit and a smoke
`MAP.md` defining its exact oracle, mappings, exclusions, and pass conditions.

---

## Compatibility and scope

Worldline currently targets:

- Minecraft Beta 1.7.3
- Java 8 bytecode for stable APIs, TestKit authoring API, adapters, and mods
- Java 21 bytecode for TestKit runner, CLI, tests, and repository harness
- a hash-pinned official client JAR kept only under ignored local storage
- a pinned RetroMCP revision for mapped adapter construction
- trusted local Worldline mod JARs using runtime `b1.7.3` and mod API `1`

Worldline is not a general Minecraft launcher, a security sandbox, or a
drop-in loader for legacy ModLoader/Forge mods. The current public mod boundary
provides dependency resolution and deterministic multiple-mod ordering
through format 2 descriptors, but does not provide per-mod permissions, hot
reload, arbitrary private-state capture, or a stable published Maven/Gradle
TestKit 1.0. Entity spawning uses a closed semantic registry; unregistered
types fail closed. Native rendering, Aero integration, official-server control, and the
vanilla behavior suites are bounded evidence with milestone-specific
non-claims; [`docs/ROADMAP.md`](docs/ROADMAP.md) is the complete ledger.

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

Worldline is available under the [MIT License](LICENSE).
