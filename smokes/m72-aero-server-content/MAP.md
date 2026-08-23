# M72 behavior map

M72 qualifies one exact server-authored StationAPI content boundary. A fresh
modded server loads the universal Worldline content mod without Aero, registers
`worldline-m72-content:server_probe`, and places one block with a plain block
entity after the sole player has settled.

The server-only nonce is written into that block entity and sent by its explicit
M72 `MessagePacket`. The client buffers the message until the matching remote
block exists, binds identifier, raw ID, coordinates, block-entity type, and
nonce, then invokes the pinned Aero renderer. Completion requires twenty later
renderer frames and a strict Aero row with positive visible chunks and
valid nonnegative counters. The content renderer-return marker is the direct
proof that this fixture traversed Aero; a later pulse need not contain at-rest
work from the same frame.

Two fresh server/client/worktree sets use distinct nonces. Each server loader
lists the content mod but not Aero; each real graphical client lists the same
content mod plus Aero 3.0.0. Both sides match exact coordinates, raw ID, and
nonce before clean disconnect and server stop.

Frozen trace:

```text
v1|server=stationapi-modded-without-aero|content=server-authored-custom-block-be|sync=explicit-m72-message-with-server-only-nonce|client=real-stationapi-aero3|identity=exact-identifier-coordinates-raw-nonce|render=aero-return+20-frames|aero=content-row-visible-chunks|runs=2-distinct-nonces|shutdown=clean|performance=not-claimed
```

SHA-256: `6dff186ed904bdce57466038dd32a9824888d6de7ddb1a20041663cb8cec0501`.

Nonclaims: automatic or generic block-entity NBT synchronization, arbitrary
registry compatibility, persistence/restart, multiple clients, dense scenes,
combat, pixels, visibility correctness, FPS, timing deltas, spike attribution,
causality, or reproduction of the historical Aero lag mechanism.
