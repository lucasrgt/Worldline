# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## 0.3.0 - M5 Reproduction Bundle

Status: GO.

- Added canonical `ReproductionBundle`, `ReplayProvider`, and `ReplayReport`
  contracts in an independently compiled module.
- Added a neutral replay CLI and a repository launcher that verifies local
  runtime inputs before starting the controlled b1.7.3 provider.
- Bundles embed the durable M4 snapshot while declaring the exact Worldline,
  runtime, official-client hash, and RetroMCP revision required for replay.
- Added two-process deterministic packing, copied-path CLI replay, official-JAR
  state comparison, and negative corruption/runtime/dependency evidence.

The frozen M5 bundle SHA-256 is
`840dca117939412dbba24594a1091c44d4b312b1e9700cec7aab7f47e0cc0181`.

## 0.2.0 - M4 Durable Snapshot

Status: GO.

- Added neutral `SnapshotMinecraftRuntime` and immutable, bounded
  `RuntimeSnapshot` contracts.
- Promoted the b1.7.3 replay checkpoint to a versioned canonical UTF-8 format
  with a body checksum and a frozen full-document SHA-256.
- Added strict parsing for runtime/version identity, field order, event count,
  numeric ranges, relative logical world sources, UTF-8, checksum, and exact
  canonical round-trip.
- Added cross-process capture and restore evidence, direct official-client
  state comparison, and executable corruption rejection.

The frozen M4 snapshot SHA-256 is
`a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2`.

## 0.1.0 - M3 Domain API

Status: GO.

- Added the opt-in `AutomatedMinecraftRuntime` without changing the v0.0.1
  lifecycle contract.
- Added neutral immutable block and position values plus stable world, entity,
  and local-player interfaces.
- Added lifecycle-guarded b1.7.3 implementations for world time, block
  read/write, active-entity snapshots, player state, teleportation, and hotbar
  selection.
- Added machine-verified mappings for every M3 field and method.
- Added a four-process differential oracle compiled independently against the
  official client JAR.

The frozen M3 signature is
`d38186377edc68f8080e568ffaba6559c4b3980fcf2a5311aac1b6ec7ebcc13c`.

## 0.0.1 - Controlled Tick

Status: GO.

Stable milestone contract:

- freeze and hash the official Minecraft Beta 1.7.3 client artifact;
- pin and verify the RetroMCP toolchain;
- reconstruct and compile the mapped client locally;
- boot the real client object graph without a native window;
- load a deterministic in-memory world;
- advance exactly one externally requested `Minecraft.runTick()`;
- match an independent oracle compiled against the official client JAR in two
  fresh subject and two fresh oracle JVMs.

The frozen first-tick signature is
`ac13115a73408c85eb80b931dc3004b4fd66b26a5512e8d4fb036eebf70ae780`.

Release qualification includes two cold RetroMCP reconstructions. Decompiled
`World` source was not byte-stable, so v0.0.1 explicitly guarantees frozen
inputs and oracle-verified observable behavior, not byte-identical decompiler
output.

Experimental capabilities shipped alongside the milestone include a reusable
b1.7.3 adapter, 16-tick state traces, deterministic external boundaries,
replay-backed checkpoints, hypothesis branches, semantic inventory GUI
actions, and isolated mod-JAR loading. These do not enlarge the stable v0.0.1
contract.
