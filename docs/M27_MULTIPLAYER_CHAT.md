# M27 Two-Client Multiplayer Chat

M27 adds `ChatMultiplayerSession` above the playable session boundary. The
b1.7.3 adapter writes native `Packet3Chat` payloads and exposes one bounded
blocking receive operation.

The new inbound codec skips only qualified protocol-14 server packet payloads,
using their original field lengths and explicit bounds. It supports the time,
position, chunk, block, inventory, entity, metadata, weather, window, sign,
map, and related packets observed around login. Chat and disconnect remain
semantic results; unknown IDs and invalid counts fail closed.

Two fresh scenarios each connect `WorldlineA` and `WorldlineB` simultaneously.
After both synchronize, A sends `worldline-m27` and B must receive exactly
`<WorldlineA> worldline-m27`. Both clients then close and the official server
shuts down cleanly.

## Non-claims

M27 does not decode chunk contents into a world, maintain an asynchronous
event loop, reconnect sessions, render remote players, claim ordering beyond
the bounded scenario, or externally control server ticks.
