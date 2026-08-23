<!-- worldline-map-schema=1 -->
<!-- boundary=durable-snapshot -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2 -->

# M4 Durable Snapshot Evidence Map

## Claim

Worldline v0.2.0 captures one deterministic b1.7.3 logical state into a
canonical durable artifact and restores it in another process without changing
the observed vanilla state.

## Scenario

The scenario uses the already oracle-qualified controlled-client fixture. It
loads the same seed and logical world, reseeds the controlled RNG, injects a
hotbar event at tick 2, advances to tick 4, and captures a snapshot through
`SnapshotMinecraftRuntime`.

Two fresh capture JVMs must write byte-identical files. Two additional JVMs
read those bytes, restore clean runtimes, reproduce the stored internal
fingerprint, and serialize back to the exact original artifact. A fifth fresh
official-JAR JVM executes the uninterrupted scenario; its tick-4 state must
match capture and restore. Three additional processes receive a modified state
payload, unknown format version, and wrong runtime identity; each must fail on
its specific validation boundary.

Frozen full-document SHA-256:

```text
a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2
```

## Exact boundary

The stable API exposes only snapshot capture and immutable bounded bytes. The
b1.7.3 adapter owns the event grammar and clean-runtime reconstruction. The
official oracle does not parse Worldline's format; it independently establishes
the vanilla state that the restored runtime must reproduce.

## Non-claims

The artifact covers the deterministic in-memory fixture and realized keyboard,
mouse, and RNG controls. It is not a heap dump, Minecraft save, mod-state
serializer, arbitrary callback serializer, cross-version migration format, or
self-contained reproduction bundle. Bundle manifests, dependency packaging,
and a user-facing replay CLI remain M5.
