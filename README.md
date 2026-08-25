<h1 align="center">Worldline</h1>

<p align="center"><strong>Deterministic testing, mapping, and behavioral evidence for Minecraft Beta 1.7.3.</strong></p>

<p align="center">
  <a href="#getting-started">Getting Started</a> |
  <a href="#testkit">TestKit</a> |
  <a href="#capabilities">Capabilities</a> |
  <a href="#architecture">Architecture</a> |
  <a href="#documentation">Documentation</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/release-v1.463.0%20GO-2EA44F?style=flat-square" alt="Worldline v1.463.0 GO">
  <img src="https://img.shields.io/badge/Minecraft-Beta%201.7.3-62B47A?style=flat-square" alt="Minecraft Beta 1.7.3">
  <img src="https://img.shields.io/badge/product-Java%208-5586A4?style=flat-square" alt="Java 8 product">
  <img src="https://img.shields.io/badge/harness-JDK%2021-6B5B95?style=flat-square" alt="JDK 21 harness">
  <img src="https://img.shields.io/badge/oracle-official%20JAR-CB8B2C?style=flat-square" alt="Official JAR oracle">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-4C566A?style=flat-square" alt="MIT license"></a>
</p>

Worldline is a laboratory for executing the real Minecraft Beta 1.7.3 client
and dedicated server under controlled conditions. It freezes inputs, advances
deterministic ticks, records canonical evidence, compares implementations, and
turns failures into replayable scenarios.

The official client and server JARs are the behavioral oracles. Worldline does
not redistribute those JARs, Mojang assets, or decompiled game source.

<table>
<tr><td><b>Behavior before assumptions</b></td><td>Claims must match a hash-pinned official runtime or an explicit invariant.</td></tr>
<tr><td><b>Controlled boundaries</b></td><td>Clock, input, RNG, filesystem, network, scheduling, and threads are controlled without replacing game logic.</td></tr>
<tr><td><b>Canonical evidence</b></td><td>Traces, snapshots, mod results, mapping records, and smoke signatures are deterministic and checksum protected.</td></tr>
<tr><td><b>Fail closed</b></td><td>Unknown formats, mappings, dependencies, runtime identities, and tools are rejected.</td></tr>
<tr><td><b>Honest scope</b></td><td>An experiment does not silently become a stable API or a universal gameplay claim.</td></tr>
</table>

---

## Runtime matrix

| Lane | Runtime | Purpose |
| --- | --- | --- |
| Host verification | No Minecraft process | Source ceilings, module isolation, compilation, unit tests, Atlas validation |
| Controlled client | Official b1.7.3 client | Input, GUI, rendering, resources, TestKit, Aero, and client lifecycle |
| Dedicated server | Official b1.7.3 server | World behavior, protocol, multiplayer, entities, blocks, saves, and dimensions |
| External mod | Isolated TestKit runtime | Java specs, fixtures, retries, snapshots, failure evidence, and minimization |

<!-- worldline-status:start -->
| Release | Milestone | Behavior contracts | Portable smoke proofs |
| --- | --- | --- | --- |
| v1.463.0 GO | `m625-multiplayer-edge-set` | 596/596 complete | 596/596 pinned |
<!-- worldline-status:end -->

The current release is declared in
[`release/worldline.properties`](release/worldline.properties). Milestone
history, retractions, and active goals live in
[`docs/ROADMAP.md`](docs/ROADMAP.md).

---

## Getting started

### 1. Run the canonical gate

Requirements:

- JDK 21 for the repository harness
- `tokei` 14 or newer
- Java 8 compatibility for product and mod-facing APIs

```text
java tools/harness/Gate.java
```

The gate enforces per-file source ceilings, module dependencies, warnings as
errors, release metadata, artifact boundaries, and the complete host test
suite. Generated output stays under `.worldline/`.

Enable the versioned pre-push hook once per clone:

```text
git config core.hooksPath tools/hooks
```

### 2. Prepare runtime verification

Place a legitimate local copy of the official client JAR at:

```text
local/artifacts/minecraft-b1.7.3-client.jar
```

Then bootstrap the pinned mapping workspace and validate runtime inputs:

```text
java tools/toolchains/Bootstrap.java retromcp
java tools/harness/Gate.java --runtime
```

Runtime artifacts and generated workspaces remain ignored. Their accepted
lengths and hashes are frozen in public descriptors.

### 3. Run the complete evidence suite

```text
java tools/harness/Gate.java --smoke
```

This is the final promotion gate. It compiles mapped adapters, executes the
registered client and server scenarios, compares official-JAR oracles, and
checks every frozen signature on one immutable commit. Successful scenarios
are stored as content-addressed PASS proofs. A later run reuses unchanged
proofs, executes only new or behaviorally affected milestones, and writes an
aggregate receipt for the current clean commit. Set `WORLDLINE_SMOKE_CACHE=off`
to deliberately execute every scenario again. Approved fingerprints can be
made portable between clones through `smokes/qualification.lock`, which records
whether evidence was executed or migrated from the reviewed pre-cache baseline.

### 4. Use the neutral runtime API

```java
try (AutomatedMinecraftRuntime runtime = B173Runtimes.create(173L)) {
    runtime.bootHeadless();
    runtime.loadWorld(WorldSource.at(worldPath));
    runtime.world().setBlock(
            new BlockPosition(8, 65, 8),
            new BlockState(20, 0));
    runtime.player().selectHotbarSlot(2);
    runtime.tick(4);
}
```

The public API contains no Minecraft, RetroMCP, LWJGL, Butter, or Aero types.
Runtime-specific factories remain behind adapter boundaries.

---

## TestKit

Worldline TestKit 0.x is a Java 8 testing experience for external mods. Specs
work with ordinary IDE completion, formatting, refactoring, and debuggers.

```java
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

public final class GlassProbeWorldlineTest extends WorldlineSpec {
    @Override protected void define() {
        describe("GlassProbe", () -> {
            beforeAll(TestDependency::start);
            afterAll(TestDependency::stop);
            beforeEach(context -> context.setBlock(
                    pos(8, 64, 8), block("b1.7.3:stone")));
            afterEach(context -> context.attach(
                    "final-block.txt", context.block(8, 65, 8).toString()));

            test("places glass", worldline()
                    .runtime("b1.7.3")
                    .seed(173L)
                    .mod("build/glass-probe.jar")
                    .run(context -> {
                context.setBlock(pos(8, 65, 8), block("b1.7.3:glass"));
                context.tick();
                expect(context.block(pos(8, 65, 8)))
                        .toEqual(block("b1.7.3:glass"));
            })).tag("block").timeout(5_000);
        });
    }

    private static final class TestDependency {
        static void start() { }
        static void stop() { }
    }
}
```

`beforeAll` and `afterAll` own suite resources. `beforeEach` and `afterEach`
wrap every fresh runtime attempt. The runner also supports table tests,
`.skip()`, `.todo()`, `.only()`, retries, snapshots, filters, watch mode,
named minimizable steps, JUnit XML, JSON, dot, verbose, and agent reporters.

### Semantic GUI tests

The GUI surface follows the same model as browser testing: query semantic
nodes, perform user actions, and capture deterministic failure artifacts.

```java
context.ui().getById("craft").click();
context.ui().getByLabel("Amount").fill("8");
expect(context.ui().getByName("Craft result")).toBeVisible();
expect(context.ui().getById("output").value()).toEqual("8");
context.ui().screenshot("craft-screen");
```

Stable automation IDs, semantic names, labels, text, focus, tab order,
geometry, clipping, overlap, pointer actions, drag, keyboard input,
screenshots, and image diffs are distinct contracts. Butter can expose its
semantic tree through the reflection boundary; vanilla and Aero remain
independently qualified consumers.

### Visual GUI builder

[`tools/gui-builder`](tools/gui-builder) is the standalone visual authoring
surface for the same semantic GUI contract. It builds a Flutter-inspired
widget tree, previews the vanilla-sized panel, and exports `GameUiSpec` JSON
or `Ui.screen(...)` Java without generating containers or runtime hooks.

```text
cd tools/gui-builder
npm install
npm test
npm run dev
```

### Create a TestKit project

```text
java -jar worldline-test-runner-0.3.1.jar init
tests/worldline/gradlew.bat worldlineDoctor worldlineTest
```

The generated project pins Gradle, isolates specs under
`tests/worldline/src/test/java`, and never modifies the mod's legacy build.
Useful tasks include:

```text
worldlineTest worldlineTestList worldlineTestWatch worldlineTestInspect
worldlineTestUpdateSnapshots worldlineTestMinimize worldlineDoctor
```

Discover the stable public behavior catalog before authoring an evidence
comparison:

```text
java -jar worldline-test-runner-0.3.1.jar behaviors list
```

`WorldlineBehavior`, `WorldlineEvidence`, and
`expect(observed).toMatchVanilla(...)` bind a mod observation to a frozen
vanilla pin. The complete Java 8 example is
[`VanillaBehaviorSpec.java`](examples/testkit/src/test/java/example/VanillaBehaviorSpec.java).

See the short [`consumer guide`](docs/CONSUMING_WORLDLINE_TESTKIT.md),
[`docs/TESTKIT.md`](docs/TESTKIT.md),
[`docs/GRADLE_TESTKIT.md`](docs/GRADLE_TESTKIT.md), and the
[`examples/testkit`](examples/testkit/README.md) project.

---

## Mod and extension testing

A test mod implements `B173Mod` and carries exactly one descriptor at
`META-INF/worldline-mod.properties`:

```properties
format=2
id=example.glass-probe
version=1.0.0
entrypoint=example.GlassProbe
worldline.api=1
runtime=b1.7.3
requires=example.foundation>=1.0.0
```

Dependencies use deterministic topological ordering with lexicographic
tie-breaking. Missing, incompatible, self-referential, and cyclic requirements
fail closed.

```text
java tools/replay/Replay.java mod inspect path/to/mod.jar
java tools/replay/Replay.java mod test run mod.jar 17320110707 16 run.wlmtest
java tools/replay/Replay.java mod test diff baseline.wlmtest candidate.wlmtest
```

An extension may add Java specs, typed fixtures, assertions, a separately
qualified runtime provider, or project-owned optimization evidence. It may not
move the mod implementation into Worldline, guess mappings, or leak Minecraft
types into the neutral API. See [`docs/EXTENSIONS.md`](docs/EXTENSIONS.md) and
[`docs/EXTENSION_SDK.md`](docs/EXTENSION_SDK.md).

---

## Capabilities

### Runtime control

- Virtual clock, deterministic input, RNG, filesystem, network, scheduler,
  and supervised background threads
- World, player, entity, block, inventory, protocol, and lifecycle handles
- Durable snapshots, reproduction bundles, replay, and exact reverse jumps
- Dedicated-server and controlled-client adapters against official b1.7.3

### Test and analysis

- Canonical traces and first-divergence analysis
- Invariant engine for item, block, entity, wear, health, and time rules
- Scenario DSL, deterministic fuzzing, automatic minimization, and profiling
- Runtime coverage, census, semantic GUI export, and self-contained HTML
- Serialized official-runtime execution and bounded Runtime Fabric lanes

### Semantics and mappings

- Complete client/server bytecode inventory as the mapping target
- Cross-namespace graph for official, intermediary, Nostalgia, and RetroMCP
- Evidence-promoted semantic roles with adapter manifests and coverage gates
- Generated Atlas knowledge store with provenance, gaps, graph, and matrix
- Seed terrain atlas through the official dedicated-server wire path

### Ecosystem boundaries

- Descriptor-packaged mods and deterministic dependency graphs
- Experimental Gradle TestKit plugin and isolated runner
- Reflection-only Butter semantic GUI bridge
- Qualified AeroModelLib adapter and performance evidence
- Project-owned optimization records with stable IDs

---

## Architecture

| Layer | Responsibility | Minecraft types allowed? |
| --- | --- | --- |
| `api` | Neutral runtime, domain, GUI, values, and semantic mapping contracts | No |
| `testmodel` / `testapi` | Java 8 spec DSL and assertions | No |
| `testkit` | Discovery, execution, retries, reporters, artifacts, and minimization | No |
| `kernel` | Controlled boundary composition | No |
| `trace` / `reproduction` | Canonical evidence formats and replay | No |
| `semantics` / `symbolgraph` / `atlas` | Mappings, roles, provenance, and coverage | No |
| `analysis` / `fuzz` / `profiling` | Derived diagnostics and campaigns | No |
| `b173-client` / `b173-server` / `stationapi` | Mapped runtime integration | Yes, isolated |
| `aero-model-lib` adapter | External renderer qualification | Yes, isolated |

Modules compile separately in the order declared by
[`harness.properties`](harness.properties). Undeclared dependencies and cycles
fail the canonical gate.

---

## CLI highlights

```text
worldline test run [filters]
worldline mod inspect <mod.jar>
worldline scenario run <scenario.wlscenario> <seed> <trace.wltrace>
worldline trace diff <left.wltrace> <right.wltrace>
worldline fuzz <out-dir> <seed> <cases> <max-steps> [left.jar] [right.jar]
worldline atlas status
worldline atlas show <id>
worldline atlas coverage
worldline atlas <seed> <radius-1..4> <output.html>
worldline mappings report <runtime and mapping inputs>
worldline semantics role <ROLE>
```

Commands are strict: unknown options, missing providers, invalid formats, and
incompatible artifacts return non-zero status instead of selecting defaults.

---

## Documentation

| Topic | Guide |
| --- | --- |
| Architecture and runtime boundaries | [`ARCHITECTURE.md`](docs/ARCHITECTURE.md), [`RUNTIME_FABRIC.md`](docs/RUNTIME_FABRIC.md) |
| TestKit and Gradle adoption | [`CONSUMING_WORLDLINE_TESTKIT.md`](docs/CONSUMING_WORLDLINE_TESTKIT.md), [`TESTKIT.md`](docs/TESTKIT.md), [`GRADLE_TESTKIT.md`](docs/GRADLE_TESTKIT.md) |
| Mods and extensions | [`MOD_API_V2.md`](docs/MOD_API_V2.md), [`MOD_GRAPH.md`](docs/MOD_GRAPH.md), [`EXTENSIONS.md`](docs/EXTENSIONS.md) |
| Semantics and complete mappings | [`SEMANTICS.md`](docs/SEMANTICS.md), [`SEMANTICS_AUDIT.md`](docs/SEMANTICS_AUDIT.md), [`ECOSYSTEM_MAPPINGS.md`](docs/ECOSYSTEM_MAPPINGS.md) |
| Atlas knowledge and seed maps | [`ATLAS.md`](docs/ATLAS.md), [`SEED_ATLAS.md`](docs/SEED_ATLAS.md) |
| GUI contracts and builder | [`GUI_TREE.md`](docs/GUI_TREE.md), [`GUI_SPEC.md`](docs/GUI_SPEC.md), [`tools/gui-builder`](tools/gui-builder) |
| Reproduction and analysis | [`SCENARIO_DSL.md`](docs/SCENARIO_DSL.md), [`M18_COVERAGE.md`](docs/M18_COVERAGE.md), [`M17_PROFILE.md`](docs/M17_PROFILE.md) |
| Optimization evidence | [`OPTIMIZATION_SDK.md`](docs/OPTIMIZATION_SDK.md) |
| Release history and direction | [`ROADMAP.md`](docs/ROADMAP.md), [`CHANGELOG.md`](CHANGELOG.md) |

Each promoted smoke owns a local `MAP.md`, `smoke.properties`, executable
scenario, and frozen signature. Those local files are the precise evidence;
the README is an entry point, not a duplicate catalog.

---

## Compatibility and scope

Worldline currently targets Minecraft Beta 1.7.3. Product and mod-facing code
uses Java 8; the repository harness and TestKit runner use JDK 21. RetroMCP,
Nostalgia, Butter, AeroModelLib, and loader-specific integrations are external
inputs or adapters, not replacements for the official behavioral oracle.

Supported does not mean bundled. Official JARs, original assets, decompiled
sources, credentials, worlds, and runtime output must remain outside Git.

---

## Build and contribute

Before reporting a change complete:

```text
java tools/harness/Gate.java
```

Changes that inspect, transform, or execute Minecraft also require:

```text
java tools/harness/Gate.java --runtime
```

Release candidates require the integral smoke gate:

```text
java tools/harness/Gate.java --smoke
```

Keep product sources at or below 250 `tokei` code lines, verification sources
at or below 300, smoke runners/scenarios at or below 300/150 executable
statements, and adapters at or below 150 code lines. Reviewed legacy smoke
debt can only decrease. Tests are unlimited. Add cohesive files rather than
hiding product behavior in tests, generated output, or harness code.

---

## Project transparency

Worldline is a local research and testing platform, not an official Mojang or
Microsoft project. Minecraft trademarks and game assets belong to their
respective owners. See [`LICENSE`](LICENSE) for Worldline's original code.
