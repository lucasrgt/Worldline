# M500-SW rain transition

M500-SW qualifies the official Beta 1.7.3 dedicated-server dry-to-rain
weather transition as one bounded SET. A fresh vanilla world is created and
stopped cleanly, then its `level.dat` is patched with the adapter-owned
gzip-NBT helper to a dry state with a bounded positive `rainTime` and
thunder disabled with a long `thunderTime`.

After restart, a protocol-14 client proves dimension `0` and synchronized dry
state, arms the rain tracker, and awaits exactly one fresh `Packet70Bed(1)`.
`WorldServer.updateWeather` emits that reason only on a live dry-to-rain
transition, and a pre-arm reason `1` (the already-raining login bootstrap) is
rejected, so the observation is not bootstrap.

The official save path is a dual-snapshot oracle, not a single canonical
persistence claim. The server saves the Overworld then the secondary dimension
through one save handler, so the later secondary write becomes
`world/level.dat` and the Overworld write moves to `world/level.dat_old`.
After a clean disconnect/save/stop, `level.dat_old` reads back `raining=true`,
`thundering=false`, a freshly seeded positive `rainTime` in `12000..23999`,
and preserved seed/spawn identity, while the canonical `level.dat` keeps the
original dry state with the patched `rainTime` and preserved identity.
Canonical restart rain persistence is NOT claimed.

The packet-71 lightning body is corrected to 17 bytes and covered by a focused
decoder regression; lightning and thunder are non-claims.

Frozen semantic SHA-256:
`3a90b1745c24f4ba910f209f1f4939d631063e67acfcee4f610933d55b69eb7d`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
