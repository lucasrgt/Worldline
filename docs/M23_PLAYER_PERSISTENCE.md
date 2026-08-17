# M23 Multiplayer Player Persistence

M23 extends the multiplayer server boundary with
`PersistentMultiplayerServerRuntime.player(username)` and immutable
`ServerPlayerState`. The neutral value exposes username, dimension, position,
health, and persisted inventory item count.

The b1.7.3 adapter validates the username before resolving a path and reads the
official server's gzip/NBT `world/players/<username>.dat` with original
Worldline code. It does not load server classes or decompiled sources.

Two fresh protocol-14 sessions each login, appear in the server list,
disconnect, disappear, force a server save, and read the resulting player file.
Both observations require dimension 0, health 20, empty inventory, and finite
position above the void. Exact spawn coordinates remain observational.

## Non-claims

M23 does not move the player, compare spawn coordinates, parse the full play
protocol, load the official graphical client, or externally step ticks.
