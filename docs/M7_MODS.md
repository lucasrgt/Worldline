# M7 General Mod Loading Contract

## Scope

M7 promotes the laboratory's hard-coded probe into a game-independent package
inspection and loading boundary. A Worldline mod JAR selects its own entrypoint
and declares the exact runtime and Worldline mod API it targets. Inspection does
not require Minecraft, RetroMCP, mapped classes, or native libraries.

The stable package module is `worldline-mods`. The b1.7.3 adapter continues to
own the runtime-specific `B173Mod` and `B173ModContext` tick contract. This
separation lets later adapters reuse package validation without making the
neutral product module depend on a game or mapping set.

## Canonical descriptor

Every package contains exactly one regular entry named
`META-INF/worldline-mod.properties`. It is strict UTF-8, at most 4,096 bytes,
uses LF only, ends in LF, and has exactly these ordered fields:

```text
format=1
id=worldline.example
version=1.0.0
entrypoint=example.ExampleMod
worldline.api=1
runtime=b1.7.3
```

Unknown, missing, reordered, duplicated, blank, malformed, or non-canonical
fields are rejected. IDs and runtime tokens are lowercase bounded identifiers;
versions use the supported semantic-version spelling; entrypoints are qualified
Java binary names. Format 1 accepts positive decimal API versions.

## Inspection and compatibility

`ModLoader.inspect(path, runtime, api)` resolves the real local path, requires a
regular non-empty JAR no larger than 64 MiB, strictly parses the descriptor, and
computes the whole JAR's lowercase SHA-256. It returns immutable metadata and
one exact compatibility result:

- `COMPATIBLE`;
- `RUNTIME_MISMATCH`;
- `WORLDLINE_API_MISMATCH`;
- `RUNTIME_AND_API_MISMATCH`.

Compatibility is exact string equality. Inspection never initializes the mod
entrypoint. Invalid package structure is an input error, not an incompatibility.

## Loading and lifetime

`ModLoader.load` repeats inspection and refuses every non-compatible result
before class loading. It creates a dedicated parent-first `URLClassLoader`,
loads the descriptor-selected class, requires that class to implement the
adapter-supplied entrypoint type, verifies its code source is the inspected JAR,
and invokes its public no-argument constructor. `LoadedMod` owns both the typed
instance and class loader and must be closed after the runtime stops using it.

For b1.7.3, installing a `B173Mod` explicitly enables its `onTick` callback
before each controlled `Minecraft.runTick()`. The context exposes only tick
number and integer block read/write operations. Mod exceptions propagate to the
caller; M7 makes no rollback or recovery guarantee.

## CLI

```text
worldline mod inspect <mod.jar>
```

The command prints the descriptor, artifact SHA-256, and compatibility against
runtime `b1.7.3` and Worldline mod API `1`. Exit status is 0 for compatible, 3
for well-formed but incompatible, 1 for invalid input, and 2 for usage errors.
The command inspects metadata only and never executes the entrypoint.

## Non-claims

M7 loads local code trusted by the caller; it is not a security sandbox or a
permission system. It does not load legacy ModLoader/Forge mods, resolve
dependencies, order multiple simultaneously installed mods, isolate native or
process state, serialize arbitrary mod state, promise hot reload, or claim Aero
compatibility. Those require later milestones and their own evidence.
