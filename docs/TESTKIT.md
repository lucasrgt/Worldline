# Worldline TestKit 0.x

Worldline TestKit is an experimental Java test runner for trusted Minecraft
Beta 1.7.3 mods. It combines a Vitest-like authoring surface, Playwright-like
failure artifacts, and Worldline's deterministic runtime and official-oracle
rules. The `0.x` label is deliberate: real external specs may still refine the
API before it is frozen.

External projects that also need domain helpers, runtime providers, or
performance records should follow the [extension authoring guide](EXTENSIONS.md).

The tests are ordinary Java. "DSL" means a typed internal Java API; Worldline
does not parse a custom language, run JavaScript, or introduce implicit globals.
Consequently Java formatters, syntax highlighting, completion, navigation,
refactoring, and debugging work normally in IntelliJ IDEA, VS Code, and Eclipse.

## Compatibility boundary

| Layer | Bytecode | Purpose |
| --- | --- | --- |
| `api` | Java 8 | Neutral controlled-runtime contract |
| `testmodel` | Java 8 | Plans, contexts, results, diagnostics, reporter events |
| `testapi` | Java 8 | Types imported by external specs |
| external specs | Java 8 recommended | Maximum compatibility with legacy mod builds |
| `testkit` | Java 21 | Runner, isolation, artifacts, snapshots, reporters |
| `cli` | Java 21 | Discovery, filtering, watch shell, output |

Mods do not depend on `testkit` or the b1.7.3 adapter. Mapped Minecraft,
RetroMCP, LWJGL, and obfuscated names remain outside the test API.

## First spec

```java
package example;

import worldline.test.WorldlineSpec;

import static worldline.test.Expect.expect;
import static worldline.test.Worldline.block;
import static worldline.test.Worldline.describe;
import static worldline.test.Worldline.pos;
import static worldline.test.Worldline.test;

public final class GlassProbeWorldlineTest extends WorldlineSpec {
    @Override
    protected void define() {
        describe("GlassProbe", () -> {
            test("places glass on the first tick", worldline()
                    .runtime("b1.7.3")
                    .seed(173L)
                    .mod("build/libs/glass-probe.jar")
                    .run(context -> {
                context.setBlock(pos(8, 65, 8), block("b1.7.3:glass"));
                context.tick();

                expect(context.block(pos(8, 65, 8)))
                        .toEqual(block("b1.7.3:glass"));
            })).tag("block").timeout(5_000);
        });
    }
}
```

`test` and `it` are exact aliases. `describe` and `suite` are exact aliases.
The methods collect an immutable plan before any test executes.

## Gradle project configuration

The canonical external-mod layout and command surface are documented in the
[Gradle adoption guide](GRADLE_TESTKIT.md). In short:

```text
java -jar worldline-test-runner-0.2.1.jar init
tests/worldline/gradlew.bat worldlineDoctor worldlineTest
```

Use ordinary `*WorldlineTest.java` files in
`tests/worldline/src/test/java`. The older direct-runner configuration below
remains supported as a low-level compatibility surface, not the recommended
adoption path.

### Legacy direct-runner configuration

Place `worldline-test.properties` in the project root:

```properties
format=1
source=build/test-classes
spec=*
classpath=build/classes/java/main;build/resources/main
mod=build/libs/glass-probe.jar
world=.worldline/worlds/glass-probe
provider=worldline.b173.B173TestRuntimeProvider
reporter=default
artifacts=.worldline/test-results
snapshots=src/test/snapshots
runtime.lock=.worldline/official-runtime.lock
```

The format and keys are strict. `spec=*` or an omitted `spec` discovers every
top-level concrete `WorldlineSpec` in the bounded source. `classpath` contains
the mod's own class directories or dependency JARs, using the platform path
separator. Unknown keys, missing sources, invalid classes, classpath entries
over 64 MiB, and unavailable providers fail before runtime execution.

Compile a spec against only Java 8 API modules:

```text
javac --release 8 -Xlint:all,-options -Werror -classpath worldline-test-api-0.2.1.jar -d build/test-classes src/test/java/example/GlassProbeWorldlineTest.java
```

Use `:` instead of `;` in the classpath on Linux and macOS.

After `java tools/harness/Verify.java --smoke` has prepared the local adapter:

```text
java tools/replay/Replay.java test
java tools/replay/Replay.java test example.GlassProbeWorldlineTest
```

The explicit form does not require a project file. Omit the class to discover
all specs and pass the mod's product output separately:

```text
java tools/replay/Replay.java test run build/test-classes --classpath=build/classes/java/main --mod=build/libs/glass-probe.jar
```

Collection-only commands do not execute Minecraft:

```text
java tools/replay/Replay.java test list build/test-classes example.GlassProbeWorldlineTest
java tools/replay/Replay.java test inspect build/test-classes example.GlassProbeWorldlineTest
java tools/replay/Replay.java test minimize build/test-classes example.GlassProbeWorldlineTest
```

Build ignored deterministic API and runner JARs plus PowerShell/CMD launchers:

```text
java tools/testkit/TestKitPackage.java
```

The generated distribution stays under `.worldline/dist/testkit`. Butter and
AeroModelLib each compile their consumer specs against only the packaged API
JAR and execute them with the packaged runner JAR.

## Authoring API

### Tests and suites

```java
describe("inventory", () -> {
    test("selects a slot", context -> { /* ... */ });
    it("keeps the selection", context -> { /* ... */ });
});
```

Modifiers are chain methods because Java does not have callable properties:

```java
test("known gap", context -> {}).skip();
test("future contract", context -> {}).todo();
test("focused locally", context -> {}).only();
test("bounded retry", context -> {}).retry(1);
test("tagged", context -> {}).tag("inventory").timeout(10_000);
```

Per-test runtime settings use a typed Java builder. Reconfiguring the same
runtime, seed, mod, or world twice fails during collection:

```java
test("fixed seed", worldline().runtime("b1.7.3").seed(173L)
        .run(context -> expect(context.seed()).toEqual(173L)));
```

`.only()` is an error under `--ci` unless `--allow-only` is explicit. Retry is
zero by default. A test that passes only after retry is `FLAKY`, never ordinary
green. Runtime tests marked `.concurrent()` fail closed; official runtimes are
always sequential.

### Table tests

```java
each(Arrays.asList("a", "b", "c"))
        .test("row %# is %s", (context, value) -> expect(value).notToBeNull());
```

`%#` is the zero-based row and `%s` is the value. Values are copied during
collection so the plan does not retain a mutable source iterable.

### Hooks

- `beforeAll` and `afterAll` are neutral preparation hooks. They receive no
  runtime and must not share an official session.
- `beforeEach` and `afterEach` run around every fresh attempt.
- `onTestFailed` and `onTestFinished` can be declared at suite scope or added
  dynamically through `TestContext`.
- cleanup hooks run even when the body or an assertion fails.

### Expectations

The initial typed matchers include equality, inequality, booleans, null,
substring, numeric greater-than, change assertions, and external snapshots:

```java
expect(context.player().health()).toEqual(20);
expect(() -> context.player().health()).toChange(() -> context.tick());
expect(context.player().items().toString()).toMatchSnapshot("inventory");
```

Snapshots are external `.wlsnap` files. A missing or changed snapshot fails by
default. `-u` or `--update-snapshots` is required to create or update one. The
runner never writes inline Java, release metadata, smoke evidence, or official
oracle signatures.

### Semantic GUI testing

`context.ui()` exposes a Cypress-style semantic surface without browser DOM,
JavaScript, mapped Minecraft classes, or a mandatory GUI library. Locators by
role, name, label, text, and slot are lazy and fail when a single-node action
is absent or ambiguous. Optional input, layout, and native visual operations
are capability-gated and validated by `GameUiContract`.

`context.screenshot("name")` requires `SCREENSHOT`, writes a bounded binary PPM
artifact, and returns an immutable ARGB `GameUiImage`. Its normal
`toMatchSnapshot` representation freezes width, height, and SHA-256. Exact and
tolerant diagnostics are available through `GameUiImage.difference`; accepting
a changed baseline still requires the ordinary explicit snapshot-update flag.

The default `--template=gui` project is executable against the vanilla
inventory adapter. Butter is optional: screens implementing its `HostUi`
contract are bridged reflectively. Aero and Butter native input/layout/capture
capabilities remain consumer-owned and must pass their own locked runtime gate.

### Steps and minimization

```java
context.step("place glass", step ->
        step.setBlock(pos(8, 65, 8), block("b1.7.3:glass")));
context.step("advance one tick", step -> step.tick());
```

Named steps produce `failure.wlscenario`. With `--minimize`, the runner reruns
the complete spec in fresh sessions while omitting candidate steps and uses the
existing deterministic delta debugger. `minimized.wlscenario` is emitted only
when the same failure type and message are preserved within the evaluation
budget.

Use the context parameter passed to each step. A closure that captures a prior
attempt's context cannot be safely replayed and is outside the contract.

## Evidence-backed selectors

Public specs use versioned semantic names instead of RetroMCP names:

```java
block("b1.7.3:glass")
entity("b1.7.3:pig")
item("b1.7.3:diamond_sword")
packet("b1.7.3:health")
```

The initial catalog is intentionally small and cites promoted milestones,
access (`READ_ONLY`/`READ_WRITE`), and stability. An
unknown selector fails with `WLTEST E2101`. Attempting a write through a
read-only mapping fails with `WLTEST E2104` and identifies the available
evidence. Milestones feed reusable selectors, actions, fixtures, matchers, and
diagnostics; they do not become hundreds of unrelated public methods.

## Isolation and runtime ownership

Every attempt, including every retry and minimizer evaluation, requests a new
`TestRuntimeSession`. It is closed after cleanup regardless of outcome. The
runner holds both a fair process-local lock and a cross-process file lock for
the entire official session. Collection and neutral tests may be parallelized
in a future release; Minecraft runtimes may not.

The default b1.7.3 provider:

1. creates the mapped controlled client;
2. boots it headlessly;
3. loads a fresh caller-owned world fixture;
4. validates and loads at most one descriptor-selected mod JAR;
5. closes the runtime and isolated mod class loader together.

The provider is runtime-specific. The test API and runner discover it only
through `TestRuntimeProvider`.

The targeted repository acceptance gate first proves the controlled client
against the hash-pinned official JAR, then executes all ten Java 8 example
specs as 30 fresh, serial TestKit sessions:

```text
java tools/smoke/ClientCycle.java controlled-client-tick
java tools/smoke/TestKitCycle.java testkit-cycle
```

Both commands are runtime-bound. Coordinated environments must place them
behind the same exclusive runtime lock used for every other official smoke.

## Results and artifacts

Result states are `queued`, `running`, `passed`, `failed`, `skipped`, `todo`,
`interrupted`, and `flaky`. Results are immutable before reporters receive
them. A failure can produce:

- `failure.txt` — exception type and message;
- `failure.wltrace` — canonical deterministic observations;
- `failure.wlsnapshot` — adapter snapshot when supported;
- `failure.wlmtest` — exact mod provenance bound to the trace;
- `failure.wlscenario` — ordered named steps;
- `minimized.wlscenario` — reduced failure when requested;
- `timeout-inventory.txt` — bounded Java 21 thread and process inventory.
- `timeout.wltrace` or `failure.wltrace` — partial trace retained on timeout.

Attachments are bounded and path-safe. Artifact names cannot escape their
per-test directory.

## Reporters

The executor never prints. It emits lifecycle events to reporters:

`RUN_STARTED`, `FILE_COLLECTED`, `TEST_QUEUED`, `TEST_STARTED`,
`ARTIFACT_RECORDED`, `TEST_FINISHED`, and `RUN_FINISHED` are represented by
typed reporter callbacks. Multi-file discovery owns one reporter lifecycle,
so JSON and JUnit outputs are written once rather than overwritten per spec.

- `default` — compact tree with `✓`, `×`, `↓`, and `□`;
- `verbose` — source locations, attempts, durations, and artifacts;
- `dot` — dense status stream;
- `json` — canonical machine-readable result;
- `junit` — JUnit XML for CI;
- `agent` — failures and artifact paths with minimal noise.

Combine reporters with commas:

```text
--reporter=default,json,junit --json=build/worldline.json --junit=build/worldline.xml
```

Use `--no-unicode` for `PASS`, `FAIL`, `SKIP`, and `TODO` output.

## Filters and watch shell

Filters are applied to the collected immutable plan:

```text
--name=GlassProbe
--file=GlassProbeSpec
--tag=block
--line=24
--seed=173
--bail=1
```

`test watch` provides line-oriented shortcuts that also work in plain CI
terminals:

| Key | Action |
| --- | --- |
| `a` | run all |
| `r` | rerun current selection |
| `f` | rerun failures |
| `u` | update snapshots for the next run |
| `p` | filter by spec path text |
| `t` | filter by test name |
| `m` | minimize the last failure |
| `o` | open the first artifact directory (or print it when desktop open is unavailable) |
| `v` | show the first assertion divergence |
| `q` | quit |
| `h` | help |

## Fail-closed defaults

- zero matched tests fail unless `--pass-with-no-tests` is explicit;
- retry defaults to zero and flaky remains visible;
- `.only` fails in CI;
- shuffle is opt-in and uses the printed deterministic seed;
- runtime concurrency is forbidden;
- every runtime is protected by an exclusive lock;
- unknown mappings and unsupported snapshot values fail;
- timeouts are failures and capture a partial trace plus inventory;
- interrupted and todo are not silently normalized to failed or passed;
- no arbitrary Minecraft mocking, automatic evidence updates, or hidden retry.

## Inspiration and license

The collection/execution/result/reporter split and console vocabulary were
inspired by Vitest. Vitest is MIT licensed. Worldline's implementation is
original Java code and does not vendor Vitest, Vite, Node.js, or copied source.
Worldline and TestKit are distributed under the repository's
[MIT License](../LICENSE), so external mods may use, modify, and redistribute
the authoring and runner code subject to that license.
See the [Vitest repository](https://github.com/vitest-dev/vitest), its
[MIT license](https://github.com/vitest-dev/vitest/blob/main/LICENSE), and its
[reporter documentation](https://vitest.dev/guide/reporters).

## 0.x non-claims

TestKit 0.x is not a security sandbox, a Forge/ModLoader compatibility layer,
a multi-mod dependency resolver, or an API for every promoted milestone. It
does not make the official runtime concurrent and does not convert decompiled
source into an oracle. Promotion to 1.0 requires sustained use by at least two
external mods without repository-private access.
