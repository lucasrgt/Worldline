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
java -jar worldline-test-runner-0.3.1.jar init
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
javac --release 8 -Xlint:all,-options -Werror -classpath worldline-test-api-0.3.1.jar -d build/test-classes src/test/java/example/GlassProbeWorldlineTest.java
```

Use `:` instead of `;` in the classpath on Linux and macOS.

Run the canonical repository Gate before packaging or testing a checkout:

```text
java tools/harness/Gate.java
```

Then use the packaged runner, which owns runtime validation and the exclusive
official-runtime lease:

```text
java tools/replay/Replay.java test
java tools/replay/Replay.java test example.GlassProbeWorldlineTest
```

List the public behavior identities without starting Minecraft:

```text
java -jar worldline-test-runner-0.3.1.jar behaviors list
```

The catalog supplies `WorldlineBehavior` tokens and Atlas IDs. A mod can emit
`WorldlineEvidence` and compare it with a frozen vanilla signal/signature via
`expect(observed).toMatchVanilla(...)`; see
[`VanillaBehaviorSpec.java`](../examples/testkit/src/test/java/example/VanillaBehaviorSpec.java).

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

### Persistent entity lifecycle matrices

`EntityConformancePlan` expands persistent entity profiles over reusable dimensions and routes
each case through `UNIVERSAL`, `ARCHETYPE`, or `SINGULAR`. `EntityLifecycleFixture` executes a
selected coherent lifecycle rather than counting each operation as a milestone:

```java
EntityLifecycleScenario scenario = new MobObservationEntityScenario(session,
        (current, entityId) -> current.attackMob(entityId));
EntityLifecycleEvidence evidence = EntityLifecycleFixture.execute(
        plan, "b1.7.3:entity/090", 90, new RemoteItemStack(319, 1, 0),
        new LinkedHashSet<String>(Arrays.asList("spawn-materialization",
                "movement-policy", "damage-death", "drop-matrix")), scenario);
```

The caller prepares causal runtime preconditions such as position, selected weapon, and spawned
fixture. The TestKit then requires a positive Packet24 identity of the expected type, an
entity-consistent movement transition when selected, Packet38 death status 3 plus Packet29
removal, and a matching distinct Packet21 drop when selected. Drop cannot be claimed without death.
Canonical evidence deliberately normalizes fresh entity IDs and poses, which makes equivalent
runs equatable without weakening the asserted semantics.

Probabilistic historical drops use the bounded overload. Each unsuccessful attempt still requires
an expected Packet24 spawn followed by the caller's causal kill, Packet38 death status 3, and
Packet29 removal before the fixture materializes another entity. The drop expectation can accept a
count interval, while canonical evidence records the declared bound rather than the random winning
attempt:

```java
EntityLifecycleEvidence squid = EntityLifecycleFixture.execute(
        plan, "b1.7.3:entity/094", 94,
        new EntityDropExpectation(351, 1, 3, 0), 8,
        new LinkedHashSet<String>(Arrays.asList("spawn-materialization",
                "damage-death", "drop-matrix")), scenario);
```

Packet23 objects use a separate universal materialization fixture so projectiles, vehicles,
primed TNT, and falling blocks are not forced through Packet24 mob semantics. The caller performs
the gameplay cause, then the fixture verifies type, positive entity identity, and any qualified
thrower or zero-velocity constraint:

```java
ObjectMaterializationScenario objectScenario =
        new ObjectObservationMaterializationScenario(objectSession);
ObjectMaterializationEvidence arrow = ObjectMaterializationFixture.execute(
        plan, "b1.7.3:entity/010",
        ObjectSpawnExpectation.withThrower(60, actorEntityId), objectScenario);
ObjectMaterializationEvidence cart = ObjectMaterializationFixture.execute(
        plan, "b1.7.3:entity/040",
        ObjectSpawnExpectation.stationary(10, 0), objectScenario);
```

The resulting evidence records Packet23 type and relational thrower policy, never ephemeral entity
IDs or poses. Arrow, snowball, primed TNT, falling sand, minecart, and boat therefore remain six
cases of one coherent materialization subsystem rather than six milestone-count atoms.

Paintings use a complete lifecycle fixture because their Packet25 identity, anchored facing, and
support dependency are one user-visible capability. Two expectations retain the qualified west
and east anchors while normalizing random motives and fresh IDs. The singular support-loss step
must destroy the selected painting through Packet29 and yield exactly item 321 through Packet21:

```java
List<PaintingSpawnExpectation> poses = Arrays.asList(
        new PaintingSpawnExpectation(5, 72, 4, 1),
        new PaintingSpawnExpectation(3, 72, 3, 3));
PaintingLifecycleScenario paintings = new PaintingObservationLifecycleScenario(session,
        (current, painting) -> removePaintingSupport(current, painting));
PaintingLifecycleEvidence evidence = PaintingLifecycleFixture.execute(
        plan, "b1.7.3:entity/009", poses, paintings);
```

This promotes the universal spawn claim and both singular orientation/support-loss claims without
claiming motive selection or native rendering. Wrong anchors, reused identities, repeated facings,
wrong destroy IDs, and missing or mismatched drops fail closed.

Controlled entity motion uses one singular matrix for every scene qualified by the historical
`m504-m508-sw-entity-dynamics` differential. The driver observes eight bounded scenes: open and
roofed ghast travel, open and low-roof slime travel, open and wall-bounded boat travel, and short
and long powered-rail minecart travel. The fixture retains only the authoritative thresholds:

```java
EntityDynamicsScenario dynamics = new EntityObservationDynamicsScenario(session,
        motionDriver::observe);
EntityDynamicsEvidence motion = EntityDynamicsFixture.execute(plan, dynamics);
```

The evidence binds the singular movement claims for minecart, boat, slime, and ghast as one
mini-subsystem. Exact positions, velocities, and vertical spans are normalized after the fixture
has checked collision, jump/landing, roof, and rail/brake envelopes. A missing scene, wrong axis,
boundary-equal sample, or non-singular route fails closed.

The slime fixture composes two different authoritative surfaces without conflating them. A
shared controlled-dynamics callback supplies the open jump/landing and low-roof vertical
envelopes; bounded protocol callbacks supply a killed parent plus nearby Packet24 children and a killed size-one
slime plus its Packet21 slimeball:

```java
SlimeLifecycleScenario slimes = new SlimeObservationLifecycleScenario(session,
        motionDriver::observe, splitDriver::attempt, dropDriver::attempt);
SlimeLifecycleEvidence evidence = SlimeLifecycleFixture.execute(
        plan, "b1.7.3:entity/055", 8, 12, slimes);
```

Canonical evidence retains the type-55 and below-y16 spawn law, both motion thresholds, causal
Packet38/Packet29 death, child locality, size-one drop policy, item 341 count range, and both
attempt bounds. It normalizes exact spans, attempt numbers, positions, entity IDs, and child count.
This promotes spawn, split, and drop as one complete lifecycle mini-subsystem while delegating the
motion law to the shared dynamics matrix.

Sheep use one complete fixture for all five qualified claims. A general lethal scenario supplies
bounded Packet24 materialization, causal Packet38/Packet29 death, and the white-wool death drop.
Two additional driver-owned scenes execute the paired dye/shear interaction and the complete
three-restart sheared-state sequence:

```java
SheepLifecycleScenario sheep = new SheepLifecycleScenario() {
    public EntityLifecycleScenario lethal() { return lethalDriver; }
    public SheepDyeShearObservation observeDyeAndShear() { return dyeDriver.observe(); }
    public SheepPersistenceObservation observePersistence() { return saveDriver.observe(); }
};
SheepLifecycleEvidence evidence = SheepLifecycleFixture.execute(plan, 8, sheep);
```

The interaction scene requires two distinct living type-91 sheep, dye inputs `351:1` and
`351:11`, and first Packet21 wool drops `35:14` and `35:4`. The persistence scene retains the
red metadata transition `14 -> 30`, persisted `30`, untouched white `0`, no repeated wool from an
already sheared sheep, the exact `Sheared=true -> false` NBT mutation, one changed sheep, and a
new `35:14` drop after metadata returns `14 -> 30`. Runtime identities and coordinates are
normalized; every semantic boundary fails closed.

The chicken-and-egg family fixture keeps the historical proof boundary deliberately asymmetric.
It requires a distinct Packet24 type-93 chicken and a player-thrown Packet23 type-62 egg within
the qualified platform radius. A Packet21 egg observed during the bounded 40-tick probe is checked
as item `344`, but its absence is also valid because the official M407 run did not wait through
the vanilla `6000+`-tick laying interval:

```java
ChickenEggFamilyEvidence evidence = ChickenEggFamilyFixture.execute(plan,
        chickenDriver::observeFamily);
```

Canonical evidence normalizes whether the optional bounded probe happened to see an egg. This
promotes only `chicken-spawn`; `chicken-egg-tick` remains explicit `UNKNOWN` until a future
qualified fixture actually observes natural laying. Wrong identities, item/type values, thrower
relations, or platform bounds fail closed.

### Block lifecycle matrices

`worldline.testkit.BlockConformancePlan` and `BlockLifecyclePlan` are part of
the Java 8 authoring JAR. They let an external mod describe a block matrix as
data while the runner registers one isolated test per scenario:

```java
BlockConformancePlan claims = new BlockConformancePlan(profiles, templates);
BlockLifecycleScenario cobblestone = BlockLifecycleScenario.from(
        "cobblestone-pickaxe", claims, "b1.7.3:block/004",
        support, supportState, BlockFace.UP, placedState,
        placementSlot, pickaxeSlot, expectedDrops, 8, 4);

new BlockLifecyclePlan("b1.7.3-server-lifecycle",
        Arrays.asList(cobblestone)).register("block lifecycle");
```

Every executable row binds `gameplay-placement`, `save-reload`,
`break-transition`, and `drop-matrix` claims. It requires an observed support
state, verifies placement and inventory change, reconnects through the
provider's declared reload boundary, breaks with the selected tool, compares
only newly observed drops, reconnects again, and verifies that removal
persisted. The result is attached as canonical
`block-lifecycle.properties` evidence.

The selected runtime provider must expose a
`worldline.api.BlockLifecycleDriver` through
`TestRuntimeSession.capability(Class)`. Unsupported capabilities fail closed;
a provider does not need to pretend that a protocol-only session is an
`AutomatedMinecraftRuntime`.

The release also contains the optional
`worldline-test-provider-b173-server-lifecycle-0.3.1.jar`. It does not contain
Minecraft. Point it at the caller-owned, hash-pinned official server and add
the extension JAR to the runner classpath:

```text
java -Dworldline.b173.lifecycle.serverJar=/path/to/b1.7.3-server.jar \
  -jar worldline-test-runner-0.3.1.jar test run my-specs.jar \
  --classpath=worldline-test-provider-b173-server-lifecycle-0.3.1.jar \
  --provider=b1.7.3-server-lifecycle --seed=17320110707
```

`B173ServerLifecycleFixtures.scenarios()` returns twenty-six currently provisioned rows:
cobblestone, dirt, empty chest, stone, planks, sandstone, brick, four ores, four mineral-storage
blocks, obsidian, rail, powered rail, detector rail, stone pressure plate, and wooden pressure
plate, plus empty dispenser, note block, crafting table, idle furnace, and empty jukebox. The
runner supplies the qualified test path to the provider, which selects
the test identity, plus immutable placement/break slot options emitted by `BlockLifecyclePlan`.
The rail rows cover flat, unpowered metadata-zero lifecycle only; slopes, power propagation,
detector activation, and minecart motion remain separate contracts.
The pressure-plate rows cover the unpowered metadata-zero lifecycle on flat stone support;
activation, release timing, redstone propagation, and support-loss behavior remain separate.
The workstation rows prove only their empty block lifecycle. Dispenser and furnace preserve their
directional metadata; GUIs, inventories, recipes, activation, tuning, smelting, and records remain
separate contracts.
For each attempt the provider seeds only that row's placement item and break tool, so external
lifecycle rows are not registered in a
provider-owned catalog and the matrix is not limited by hotbar capacity. Qualified paths retain
the collected `suite > case` identity across every retry. Each
attempt owns a new official-server workspace. The server JAR must be exactly 503100 bytes with
SHA-256 `033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d`.
The provider rejects mod paths, other seeds, missing test identity, and absent or malformed slot
options rather than guessing a fixture. Empty chest does not claim loaded-container spill.
Provider discovery chooses the runtime implementation; scenario selection remains per attempt.

For incremental coverage, `B173LifecycleScenarioFactory` builds caller-owned rows and
`BlockLifecycleFamilyCycle` qualifies a coherent list under its own stable family signature. The
first independent family covers sponge, white wool, TNT, fence, and netherrack. Adding another
family does not modify the frozen 26-row aggregate or rewrite its 104 claim signatures.
Family rows carry an exact post-break tool stack, including valid no-damage outcomes such as
shears used to break placed white wool.
The scenario factory ships with the provider extension; the family executor ships with the
runner, so the provider never depends on runner implementation classes.
Runner compatibility is requalified through `testkit-cycle`, the M7-M9 mod/testing chain, and the
M620 StationAPI driver before the provider-discovery transport is released.

### Hooks

- `beforeAll` and `afterAll` are neutral preparation hooks. They receive no
  runtime and must not share an official session.
- `beforeEach` and `afterEach` run around every fresh attempt.
- `onTestFailed` and `onTestFinished` can be declared at suite scope or added
  dynamically through `TestContext`.
- cleanup hooks run even when the body or an assertion fails.

These hooks are not interchangeable with project-global setup or dependency
substitution. `beforeAll` runs once for its collected suite in one spec file;
it does not run once across every discovered file. A future project-global
contract must be configured once by the runner, execute before all selected
plans, and guarantee teardown after partial collection or execution failure.

Mocks and fakes must also be explicit inputs, not hidden hook side effects.
Neutral unit tests may use ordinary Java test doubles. Behavioral tests may
substitute a declared mod dependency only through a visible test-runtime
configuration that records the replacement in evidence. The official
Minecraft client/server, mappings, Butter, and Aero cannot be silently mocked
for a test that claims oracle-backed behavior.

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
id, role, name, label, text, and slot are lazy and fail when a single-node action
is absent or ambiguous. Optional input, layout, and native visual operations
are capability-gated and validated by `GameUiContract`.

`getByRole(role, name)` is the concise two-token selector. `type` appends while
`fill` replaces and therefore requires the stronger `TEXT_REPLACE` capability.
Typed assertions cover values, checked/selected/expanded/read-only state, exact
item stacks, and positive item containment without exposing mapped classes.

Use `getById` for a stable author-supplied automation identity, `getByName` for
the semantic node name, `getByLabel` for a control's visible label, and
`getByText` for rendered content. Existing nodes without an explicit `id`
remain addressable because their name is the compatibility id.

`context.awaitUi(locator, maximumTicks, assertion)` retries ordinary assertion
failures against the live tree and advances exactly one controlled game tick
between attempts. It never uses wall-clock sleeps and never retries capability,
adapter, or runtime failures.

`context.screenshot("name")` requires `SCREENSHOT`, writes a bounded binary PPM
artifact, and returns an immutable ARGB `GameUiImage`. Its normal
`toMatchSnapshot` representation freezes width, height, and SHA-256. Exact and
tolerant diagnostics are available through `GameUiImage.difference`; accepting
a changed baseline still requires the ordinary explicit snapshot-update flag.

Keyboard order is semantic evidence rather than pixel inference. Focusable
nodes expose a unique zero-based `tabIndex`; tests assert it with
`shouldHaveTabIndex`, press `GameUiKey.TAB`, and confirm the live destination
with `shouldBeFocused`.

The default `--template=gui` project is executable against the vanilla
inventory adapter. Butter is optional: screens implementing its `HostUi`
contract are bridged reflectively. Extended `HostUi` methods are negotiated as
separate text-input, focus, value-input, and secondary-click capabilities;
their absence never disables the basic tree. Aero and Butter native
layout/capture capabilities remain consumer-owned and must pass their own
locked runtime gate.

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
through `TestRuntimeProvider`. A provider may be selected by its implementation
class for compatibility or by its stable `runtimeId()` through the standard
Java `ServiceLoader` descriptor
`META-INF/services/worldline.test.TestRuntimeProvider`. Missing and duplicate
runtime IDs fail before a session opens. Provider extension JARs may be passed
through `--classpath`; discovery and spec loading share that bounded class
loader for the duration of the run.

The `stationapi-b1.7.3` adapter is the second real provider family. It runs a
fresh official server and a fresh Fabric/StationAPI client per TestKit attempt,
then gates the client game thread one tick at a time over a loopback control
channel. Its M620 surface is deliberately narrow: lifecycle state, world time,
player identity, health, selected slot, and pose. It does not claim headless
boot, block or inventory mutation, GUI automation, or broad StationAPI parity.

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
- `failure.gui.txt` — canonical semantic screen and node tree when UI is available;
- `failure.gui.ppm` — native ARGB frame when the adapter declares `SCREENSHOT`;
- `failure.gui-capture.txt` — non-masking diagnostic when GUI evidence capture itself fails;
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
