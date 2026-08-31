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

## Runtime adapters

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
