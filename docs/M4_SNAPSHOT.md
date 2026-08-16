# M4 Durable Snapshot Contract

Worldline v0.2.0 promotes replay-backed logical checkpoints to durable,
cross-process artifacts. `SnapshotMinecraftRuntime.snapshot()` returns an
immutable `RuntimeSnapshot`; callers may persist its defensive byte copy and
later pass reconstructed bytes to a compatible runtime factory.

Snapshot capture is valid only in `WORLD_LOADED` and requires a drained
deterministic scheduler. Capture before loading, after closing, or while an
arbitrary callback remains pending fails closed. The returned bytes remain
valid and immutable after the originating runtime closes.

```java
SnapshotMinecraftRuntime runtime = B173Runtimes.create(seed);
// boot, load, perform controlled actions, and drain the scheduler
RuntimeSnapshot captured = runtime.snapshot();
Files.write(path, captured.bytes());

RuntimeSnapshot loaded = RuntimeSnapshot.of(Files.readAllBytes(path));
SnapshotMinecraftRuntime restored = B173Runtimes.restore(loaded);
```

## Restore semantics

The b1.7.3 adapter records the initial game seed and virtual clock, normalized
relative logical world source, completed client tick, realized controlled
events, and exact adapter observation fingerprint. Restore boots a clean
runtime, loads the logical source, replays every event at its original tick,
and refuses to return unless the resulting fingerprint equals the stored one.

This is reconstruction plus deterministic replay, not Java serialization,
heap capture, or a Minecraft save archive.

## Canonical document

The artifact is strict UTF-8 and ends every line with LF:

```text
WORLDLINE-SNAPSHOT/1
runtime=minecraft-b1.7.3-client
seed=<signed decimal long>
initialMillis=<signed decimal long>
world=<unpadded base64url UTF-8 relative path>
tick=<non-negative decimal int>
events=<non-negative decimal int>
event=<tick>,<kind>,<a>,<b>,<c>,<d>,<signed long value>
state=<unpadded base64url UTF-8 fingerprint>
sha256=<lowercase SHA-256 of every preceding byte, including the prior LF>
```

Event kind `1` is keyboard, `2` is mouse, and `3` is RNG reseed. Field order,
event count, numeric ranges, boolean flags, base64 spelling, checksum, and a
decode/re-encode byte comparison are all validated. Unknown versions and
runtimes fail closed. `RuntimeSnapshot` bounds artifacts to 1 MiB and never
exposes its internal byte array.

## Portability boundary

M4 snapshots are durable across processes using the same Worldline release,
b1.7.3 artifact, mapped runtime, and deterministic logical world source. The
world source must be a normalized relative identifier; host-absolute paths are
rejected. The format itself contains no Mojang code or game binary.

M4 does not embed arbitrary saved worlds, mods, assets, pending scheduler
callbacks, filesystem contents, network sessions, native rendering state, or
external dependencies. Packaging those inputs and a replay CLI is M5. Format
version 1 makes no cross-Worldline-version compatibility promise beyond exact
runtime identification and fail-closed parsing.
