# Worldline extension authoring

> **Current layout:** new integrations use the isolated Gradle project at
> `tests/worldline`; see the [Gradle adoption guide](GRADLE_TESTKIT.md).
> The manual commands later in this document remain useful for provider
> development and low-level debugging.

This guide explains how an external project can add Worldline tests,
project-specific test helpers, runtime integration, and performance evidence
without copying its implementation into Worldline.

## Status and terminology

In Worldline 0.x, an **extension** is a repository convention, not a loadable
plugin artifact. An extension can contain:

- ordinary Java 8 TestKit specs;
- typed fixtures, actions, and assertions owned by the external project;
- an optional runtime provider implemented at the correct adapter boundary;
- project-owned optimization records;
- frozen test and benchmark evidence.

There is currently no `worldline-extension.properties` format and no public
mapping-registration API. Do not invent either in an external repository.
Stable formats will be introduced only after multiple real integrations prove
the common contract.

## Choose an integration lane

| Lane | Use it when | Runtime support |
| --- | --- | --- |
| Host logic | The code is independent of Minecraft classes | `--no-runtime` |
| Worldline mod | The artifact implements `B173Mod` and has the canonical descriptor | Default b1.7.3 provider |
| Runtime provider | A different controlled runtime already implements the neutral API | Custom `TestRuntimeProvider` |
| Legacy loader | The project requires ModLoader, StationAPI, Aero, or another loader | New Worldline-owned adapter qualification |

Start with host logic. Add an official runtime only for behavior that cannot
be proved outside Minecraft.

## Recommended external repository layout

```text
project/
|-- src/                              product source
|-- tests/worldline/
|   |-- build.gradle.kts              isolated `io.github.lucasrgt.worldline.test` build
|   |-- worldline.toml                runtime/profile selection
|   |-- .local/oracles/b1.7.3/        ignored official-JAR drop zone
|   `-- src/test/java/                Java 8 `*WorldlineTest.java` specs
|-- worldline-extension/
|   |-- src/main/java/                typed test helpers, if needed
|   `-- README.md                     boundary and non-claims
|-- optimizations/
|   |-- catalog/                      project-owned optimization records
|   `-- evidence/                     bounded summaries or pointers
`-- .github/workflows/                `worldlineTest` CI invocation
```

Generated classes, reports, snapshots, worlds, and downloaded TestKit JARs
must stay in ignored build directories.

Initialize this layout with `worldline init`; Gradle compilation, runner
provisioning, reports, locking, and IDE import are then owned by the plugin.
The remaining direct-runner commands are the low-level provider-development
surface.

## 1. Build a low-level TestKit distribution

From a verified Worldline checkout:

```text
java tools/harness/Gate.java
java tools/testkit/TestKitPackage.java
```

The ignored distribution is written to `.worldline/dist/testkit` and contains:

- `worldline-test-api-0.3.1.jar` for Java 8 authoring;
- `worldline-test-runner-0.3.1.jar` for the Java 21 CLI;
- deterministic SHA-256 checksums and launchers.

Pin the version and checksum used by the external project. Do not commit a
locally built Minecraft JAR or mapped game classes.

## 2. Write an external spec

Specs are ordinary Java and may import the external project's separately
compiled product classes:

```java
package example.tests;

import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;
import worldline.test.WorldlineSpec;
import example.storage.StorageIndex;

public final class StorageIndexWorldlineTest extends WorldlineSpec {
    @Override protected void define() {
        test("preserves the indexed total", context -> {
            StorageIndex index = new StorageIndex();
            index.insert("iron", 64);
            expect(index.count("iron")).toEqual(64);
        });
    }
}
```

Compile the product and spec separately. Pass product outputs to the runner
through the bounded classpath:

```text
javac --release 8 -classpath worldline-test-api-0.3.1.jar;build/classes -d build/worldline-tests tests/worldline/src/test/java/example/tests/StorageIndexWorldlineTest.java
java -jar worldline-test-runner-0.3.1.jar test run build/worldline-tests --classpath=build/classes --no-runtime
```

Use the platform classpath separator (`;` on Windows, `:` on Linux/macOS).

## 3. Add strict project configuration

```properties
format=1
source=build/worldline-tests
spec=*
classpath=build/classes
provider=none
reporter=default,agent
artifacts=build/worldline-results
snapshots=tests/worldline/snapshots
runtime.lock=build/worldline-runtime.lock
```

The complete key reference, filters, reporters, and watch commands are in
[TESTKIT.md](TESTKIT.md).

## 4. Add project-owned helpers

Keep domain vocabulary beside the external project. A storage mod may expose
helpers such as `networkFixture()`, `insert(...)`, `extract(...)`, and
`expectStored(...)`. These helpers may depend on the project's public types and
the Java 8 TestKit API. They must not depend on mapped Minecraft classes or
reach into Worldline's private adapter implementation.

Helpers are not permission to bypass provenance. A custom block cannot be
passed to `block("namespace:name")` until that semantic mapping has promoted
evidence. Unknown and read-only mappings intentionally fail closed.

## 5. Use the default b1.7.3 provider

The default provider accepts one descriptor-packaged `B173Mod` per isolated
attempt:

```text
java -jar worldline-test-runner-0.3.1.jar test run build/worldline-tests \
  --mod=build/example-mod.jar \
  --provider=worldline.b173.B173TestRuntimeProvider
```

The JAR must satisfy [M7_MODS.md](M7_MODS.md). This path does not load legacy
ModLoader, Forge, StationAPI, or Aero artifacts.

## 6. Implement a runtime provider only at an adapter boundary

`TestRuntimeProvider` is the neutral factory contract:

```java
public interface TestRuntimeProvider {
    String runtimeId();
    TestRuntimeSession open(TestRuntimeRequest request) throws Exception;
}
```

`TestRuntimeRequest` carries the seed, world path, optional mod path, and the runner's qualified
test path. Providers that provision scenario-specific fixtures can route on `testPath()` while
preserving the three-argument request constructor for direct callers.

Every `open` call must return a fresh session whose runtime implements
`AutomatedMinecraftRuntime`. Closing the session must close the runtime,
loader, files, and background resources together.

Storage and automation providers may additionally implement neutral optional
capabilities: `ChunkLifecycleRuntime` for explicit load/unload,
`TileObservableRuntime` for immutable tile observations, and
`RuntimeWorkObservable` for deterministic named work counters. Specs must
check a capability explicitly and fail or skip clearly when it is absent.

Inventory adapters can publish a fail-closed `MutationCoverage` manifest.
Every `MutationBoundary` names a promoted mapping, evidence, and one precision
level: exact `PUSH`, provider-local `DIRTY_NOTIFY`, or bounded `POLL`.
`requireAll` rejects missing paths. A guessed method name is not coverage.

A provider that reads, transforms, instruments, or executes Minecraft belongs
in a Worldline adapter or in an equally strict runtime repository. It requires:

1. exact input and toolchain identities;
2. mapped symbol verification;
3. module dependency enforcement;
4. an exclusive official-runtime lock;
5. differential or invariant evidence;
6. `Verify --runtime` and a direct serialized smoke.

Do not place Minecraft, loader, RetroMCP, or LWJGL types in `testmodel`,
`testapi`, or an external spec helper API.

## 7. Integrate a legacy mod loader

Legacy-loader support is an adapter project, not a test-spec shortcut. The
safe sequence is:

1. identify and pin the loader, mod, and dependency artifacts;
2. reproduce the loader boot in a controlled b1.7.3 client;
3. prove a no-mod control against the official JAR;
4. prove one minimal loaded-mod lifecycle;
5. expose only neutral actions and observations;
6. add save/restart and unload/reload evidence;
7. add the TestKit provider after those lower boundaries pass.

Project-specific helpers may then use the provider, but loader mechanics stay
inside the adapter. A test must never access decompiled sources or guessed
obfuscated members.

## 8. Record performance work

The project that owns an optimization owns its record:

```text
optimizations/catalog/example.storage.cached-index.properties
```

```properties
schema=worldline.optimization.v1
id=example.storage.cached-index
summary=Reuse a versioned aggregate item index.
subsystem=storage.index
status=candidate
default.enabled=false
behavior.delta=Changes aggregate lookup implementation without changing results.
risks=Stale totals after insertion, extraction, or network topology changes.
rollback=Disable the cached index and rebuild totals from storage providers.
tracking=symbol
source.symbols=example.storage.StorageNetwork#getAll
evidence=example:storage-index-scale-matrix
```

Validate it with Worldline's portable checker:

```text
java path/to/worldline/tools/harness/OptimizationCatalogCheck.java path/to/project
```

Only an `active` optimization may default on. A candidate needs behavioral
equivalence, performance evidence, risk coverage, and a tested rollback before
promotion. See [OPTIMIZATION_SDK.md](OPTIMIZATION_SDK.md).

## 9. Build a qualification matrix

A performance extension should freeze inputs and report more than elapsed
time. At minimum capture:

- exact project, dependency, runtime, and fixture revisions;
- deterministic seed and warmup policy;
- operation counts and result signatures;
- per-tick latency distribution rather than one average;
- allocations, GC pauses, packet bytes, rebuilds, and cache events when relevant;
- fresh-process repetitions and a retained baseline;
- functional invariants before and after each measurement;
- timeout, interruption, and artifact paths.

Do not call a result faster when it changes observable behavior, loses data,
skips required work, or compares different fixtures.

## Beta Energistics blueprint

Beta Energistics should use all four extension surfaces:

1. **Host specs** for item keys, storage aggregation, crafting planning, and
   BetaVault cell identity.
2. **A generic ModLoader/Aero provider** qualified in Worldline, because the
   default `B173Mod` provider cannot load the legacy artifact.
3. **Project-owned helpers and runtime specs** for networks, disks, buses,
   terminals, crafting, persistence, chunk lifecycle, and multiplayer.
4. **Project-owned optimization records** for every cache, index, batching,
   scheduling, or packet change.

The recommended qualification order is:

```text
host correctness
-> loader boot
-> minimal storage network
-> insertion/extraction conservation
-> save and chunk reload
-> crafting and automation
-> telemetry baseline
-> scale matrix
-> candidate optimizations
-> soak and multiplayer qualification
```

Do not wait for unrelated vanilla milestones. The missing work is the
Beta-Energistics-specific loader, action, observation, persistence, and
performance boundary.

## Promotion checklist

An extension is ready for a claimed capability only when:

- its repository gate passes from a clean checkout;
- every external dependency is revision-pinned;
- specs compile as Java 8 and tooling runs on Java 21;
- official runtimes are serialized behind the exclusive lock;
- behavior has a differential or invariant oracle;
- mappings cite promoted evidence;
- generated and official artifacts remain ignored;
- performance candidates retain correctness and rollback coverage;
- evidence and non-claims are documented;
- repeated direct smokes pass before integration or promotion.

Worldline TestKit remains experimental 0.x. Extension experience from real
projects will determine which parts deserve a stable 1.0 contract.
