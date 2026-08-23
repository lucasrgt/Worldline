<!-- worldline-map-schema=1 -->
<!-- boundary=runtime-lab -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3d8f8d72d954019cc8b3c1e3b41740fa277202edeacdad53fc3720a03202967f -->

# Worldline Laboratory Cycle Map

## Claim

The reusable b1.7.3 adapter can capture a replay-backed logical checkpoint,
restore it in a fresh client, replay its realized boundary events, compare two
isolated hypotheses, select and act on an inventory GUI semantically, and load
an independently compiled benchmark mod JAR through a narrow public contract.

This is not a byte-for-byte JVM heap snapshot. A checkpoint freezes the world
source, initial seed and clock, completed tick, realized input/RNG events, and
an exact observable-state fingerprint. Restore rebuilds a clean runtime and
fails if replay does not reproduce that fingerprint. Snapshots require a
drained scheduler; pending arbitrary `Runnable` instances are deliberately not
serialized.

## Executable scenario

The lab scenario creates a checkpoint at tick 4 after a fixed RNG reseed and a
scheduled hotbar event. It then proves:

| Capability | Executable evidence |
| --- | --- |
| Snapshot / restore / replay | Two independent restorations reproduce the stored fingerprint and selected slot 2. |
| Branching | Baseline and intervention start from the same checkpoint; after one tick they end at slots 2 and 4. |
| Semantic GUI | `GuiInventory` is selected by type, its 45 slots by index, slot 0 is clicked without caller-supplied coordinates, and the screen is closed semantically. |
| Mod boundary | `ProbeMod` is compiled into `probe-mod.jar`, loaded from that JAR, installed through `B173Mod`, and changes fixture block 65 from air (`0`) to glass (`20`). |

Two fresh lab JVMs must emit the same frozen signature:

```text
3d8f8d72d954019cc8b3c1e3b41740fa277202edeacdad53fc3720a03202967f
```

## Aero seam

`B173Mod` and `B173ModContext` are the initial compatibility seam for an Aero
experiment. A candidate Aero adapter can be packaged independently and receive
only controlled tick, block-read, and block-write capabilities. The probe does
not claim Aero compatibility yet; it proves isolated JAR provenance, lifecycle
installation, deterministic invocation, and observable branch comparison
before Aero-specific code is introduced.

## Non-claims

The checkpoint format is currently in-process and not a durable cross-version
file format. It covers realized keyboard, mouse, and RNG controls over the
deterministic in-memory fixture. Arbitrary saved worlds, pending callbacks,
network sessions, arbitrary mod state, rendering, and every Minecraft GUI are
outside this cycle.
