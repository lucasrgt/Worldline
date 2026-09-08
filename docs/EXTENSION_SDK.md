# Extension SDK

Worldline publishes a Java 8 Extensions SDK so a mod can describe its own
content and tests without Worldline learning the mod's implementation types.
Runtime drivers remain Worldline-owned; extensions remain mod-owned.

## Manifest and discovery

Each external repository owns one directory per extension:

```text
worldline/extensions/<id>/manifest.properties
```

```properties
schema=worldline.extension.v1
id=example-mod
version=1.0.0
entrypoint=example.worldline.ExampleExtension
worldline.api=1
requires=testkit.v1,atlas.v1
provides=example-content.v1
```

`WorldlineExtensionDiscovery` scans the directories in stable order, validates
the schema, negotiates every required capability, and only then constructs the
entrypoint. Unknown keys, duplicate IDs, missing capabilities, mismatched
directory names, and invalid entrypoints fail closed.

## Entrypoint and registrations

Implement `WorldlineExtension` and register through the supplied registry:

```java
public final class ExampleExtension implements WorldlineExtension {
    @Override public void register(WorldlineExtensionRegistry registry) {
        registry.subject(ExtensionSubject.of("example-mod:glass",
                ExtensionSubjectKind.BLOCK, "Example glass"));
        registry.fixture("empty", context -> reset());
        registry.action("place", context -> placeGlass());
        registry.observation("state", context -> state());
        registry.oracle("equatable", ExtensionOracles.equatable());
        registry.contract(ExtensionContract.builder("glass-place", "example-mod:glass")
                .fixture("empty").action("place").observation("state")
                .oracle("equatable").mode(ExtensionMode.CUSTOM_CONTRACT)
                .custom(expectedSignature).build());
    }
}
```

Subjects are blocks, items, entities, or subsystems. Fixtures, actions,
observations, and oracles are independent providers with stable IDs. A
contract binds those IDs and explicitly declares its supported modes:

- `conformance` compares the result with a declared vanilla evidence pin;
- `differential` records whether the mod changed that vanilla pin;
- `custom-contract` compares the result with the mod's declared stable rule.

`ExtensionTestSpecs.create(plan, mode)` converts the selected contracts into
ordinary TestKit specs. Every attempt emits a canonical evidence properties
file. Differential attempts also emit a baseline/observed comparison.

## Custom contract shortcuts

Use `ExtensionContracts.custom` for contracts that compare observations with
declared expected values. It selects custom-contract mode, registers an equatable
oracle, and computes the expected signature using the existing evidence format:

```java
registry.subject(ExtensionSubject.of("example-mod:machine",
        ExtensionSubjectKind.SUBSYSTEM, "Processing machine"));
ExtensionContracts.custom("machine-pipeline", "example-mod:machine")
        .fixture("fresh-game", MachineProof::prepare)
        .action("exercise-machine", MachineProof::exercise)
        .expect("pipeline", MachineProof::status, "PASS")
        .registerInto(registry);
```

`MachineProof` is consumer-owned code; its methods receive `ExtensionContext`.
Fixture and action methods return `void`, observation methods return `String`,
and all may throw checked exceptions. Callbacks run when TestKit executes the
contract, never during registration. Chain `action` and `expect` for additional
steps and observations. Actions execute in declaration order.

Subject registration is separate so several contracts can refer to the same
block, item, entity, or subsystem without registering it twice. For other modes
or custom oracles, use `ExtensionContract.builder` and the registry directly.

## One entrypoint, multiple registration groups

The manifest's `entrypoint` is the extension's composition root. Worldline calls
its `register` method; no Java `main` method, annotation scanning, or additional
manifest per block is needed. A mod can split related registrations into ordinary
classes as it grows:

```java
public final class IndustrialModExtension implements WorldlineExtension {
    @Override public void register(WorldlineExtensionRegistry registry) {
        SharedFixtures.register(registry);
        RefineryContracts.register(registry);
        CrusherContracts.register(registry);
        FurnaceContracts.register(registry);
        PipeContracts.register(registry);
    }
}
```

The extension represents the whole mod, not a single machine. Point the manifest's
`entrypoint` to this class. For example, keep each machine's declarations and
execution together in a package:

```text
proof/IndustrialModExtension.java
proof/SharedFixtures.java
proof/machines/refinery/RefineryContracts.java
proof/machines/refinery/RefineryProof.java
proof/machines/crusher/CrusherContracts.java
proof/machines/crusher/CrusherProof.java
proof/machines/furnace/FurnaceContracts.java
proof/machines/furnace/FurnaceProof.java
proof/transport/PipeContracts.java
proof/transport/PipeProof.java
```

Each machine registers its subject once and can declare several contracts such
as formation, processing, and shutdown. Share fixtures or actions only when their
behavior is actually shared. If the entrypoint grows, a plain
`MachineContracts.register(registry)` method can compose the machine groups, while
`TransportContracts.register(registry)` composes pipes and other transport.
These are ordinary consumer-owned methods; no additional SDK interface is required.

Each group owns its subjects and contracts. Register shared providers once and
reference them using the ID-only overloads: `fixture(id)`, `action(id)`, and
`expect(observationId, expectedValue)`. For example, after `SharedFixtures`
registers `fresh-game`, a block group can declare:

```java
public final class PipeContracts {
    private PipeContracts() {}

    public static void register(WorldlineExtensionRegistry registry) {
        registry.subject(ExtensionSubject.of("example-mod:pipe",
                ExtensionSubjectKind.BLOCK, "Transfer pipe"));
        ExtensionContracts.custom("pipe-connect", "example-mod:pipe")
                .fixture("fresh-game")
                .action("connect-pipe", PipeProof::connect)
                .expect("pipe-connected", PipeProof::connected, "true")
                .registerInto(registry);
        ExtensionContracts.custom("pipe-disconnect", "example-mod:pipe")
                .fixture("fresh-game").action("connect-pipe")
                .action("disconnect-pipe", PipeProof::disconnect)
                .expect("pipe-connected", "false")
                .registerInto(registry);
    }
}
```

Group classes do not create namespaces: provider IDs remain extension-wide.
Use descriptive IDs such as `pipe-connected` for distinct observations and reuse
an ID only when referencing the same registered provider. Inline overloads
register a provider; they do not replace or silently deduplicate existing ones.
Each shortcut contract reserves its own contract ID in the oracle namespace.
Duplicate IDs and missing references remain errors under normal registry and
discovery validation. Registration errors must abort extension loading.

Proof-specific polling, file formats, and assertions remain in the consumer's
proof classes. A fixture name such as `fresh-game` does not reset the game by
itself: the registered fixture must perform the required setup. Runtime provider
qualification remains independent of these registration shortcuts.

## Runtime adapters

### Test progress HUD

For a prepared ModLoader or Forge TestKit client, enable `--overlay` on
`worldline test run`, set `overlay=true` in `worldline-test.properties`, or use
`new RunnerOptions().overlay(true)`. The runner projects the suite, test,
fixture/action/observation/oracle step, selected/completed counts, outcomes,
retry attempt, elapsed/timeout milliseconds, context ticks, seed, runtime ID, and latest
failure into native-font text at the bottom-right edge, without a container or
background. All rows remain visible independently of vanilla F3: status/count,
suite, test, step, runtime, seed, attempt, elapsed time, timeout, completed count,
passed/failed totals, skipped/flaky totals, ticks, and any failure detail.
Ticks, elapsed time, and timeout each have their own labeled row. Labels use a
colon followed by a space. Text uses the same font size and shadow as F3 and clips
long labels to the available GUI-scaled width. The bottom rows stay beside the hotbar.

Each runtime session owns its display. The text updates on runner events and
at most every 100ms during context ticks; it is a snapshot, not a wall-clock
heartbeat. Context ticks count `TestContext.tick`/`ExtensionContext.tick` calls.
The final attempt status is displayed before closing its client; console and
artifact reporters retain the complete batch result after the client exits.

The HUD is opt-in and uses `TestRuntimeSession.showProgress`; sessions without
a graphical implementation keep the default no-op. Prepared legacy clients
advertise `progress.protocol=worldline.test-progress.v1`. Re-prepare older clients
to enable the HUD. HUD messages update and present text without advancing a
game tick, and every acknowledgement checks unchanged controlled tick, world
time, player identity, health, selected slot, and position. Protocol failures
fail the attempt and still close the session. The HUD never replaces assertions,
evidence signatures, or the suite result.

`ExtensionRuntimeAdapter` names the loader, runtime ID, and public
`TestRuntimeProvider` class used by the extension. The descriptor does not
grant access to mapped or decompiled classes. The provider must be qualified
independently at the adapter boundary before runtime claims are made.

## Atlas contribution

Every validated plan produces a deterministic `WORLDLINE-EXTENSION-ATLAS/1`
projection. Atlas imports that projection into its canonical store: subjects
become `atlas.api.*` records, contracts become `atlas.scenario.*` records, and
runtime adapters become `atlas.loader.*` records. Extensions may add explicit
namespaced pages. Each record carries tags, validated relations, the extension
version, and `extension:<id>@<version>` provenance.

`AtlasStore.standard(repositoryRoot, extensionProjectRoot)` performs discovery
and import in one fail-closed operation. The equivalent CLI inspection route is:

```text
worldline atlas extensions <project-root>
```

The CLI reports the discovered extension count, canonical record count, IDs,
and the resulting stable Atlas SHA-256.

## Semantic bindings

Promoted catalog-role bindings are optional and use a separate sibling file:

```text
worldline/extensions/<id>/semantics.properties
```

That file retains schema `worldline.adapter.semantics.v1`. Older repositories
whose `manifest.properties` already uses the semantic schema remain readable.
The templates are `worldline/extensions/TEMPLATE.properties` and
`worldline/extensions/SEMANTICS_TEMPLATE.properties`.

## Qualification boundary

M785 compiles an external block, item, entity, and subsystem fixture using
only public packages, discovers it by manifest, runs all three comparison
modes, and verifies evidence plus canonical Atlas and CLI output. M786 also
requires strict public binding ledgers for every verified block and entity
Functional Census claim. Loader execution still requires a separately qualified
runtime provider; an SDK manifest alone is not a claim that arbitrary legacy
mods are executable.
