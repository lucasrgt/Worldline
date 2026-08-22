# Semantic Audit — Multiplayer World State

Domain inventory for `origin/main` `1755fa8` (v1.55.0 / M67). Native `NetClientHandler`
and `WorldClient` appear only in `mappings.tiny`. Executable evidence
is the `b173-server` protocol-14 adapter against the official server.

## Already catalogued (singleplayer, do not reuse)

`NETWORK_DISABLED`, `OFFLINE_SESSION`, `CHUNK_LOAD`, `CHUNK_PROVIDER`,
`SAVE_CHUNKS`, `WORLD_TIME`, `NATIVE_WORLD_SAVE`, `CLIENT_WORLD`
(typed `World`, not `WorldClient`), `BLOCK_WRITE`, `SPAWN_SET`.

## Promoted

Packet12/13/stance, Packet3 chat, Packet14 dig, Packet50-53 class roles,
plus M48-M67 inventory/place/combat/window packet classes and unique
`b173-server` sites. Packet50 is not `CHUNK_LOAD`. Packet52/53 are not
`BLOCK_WRITE`. Packet14 is not `PlayerController.clickBlock`. Packet15
is not `sendUseItem`. Packet50 `yPosition` is not catalogued.
Packet200 is `Packet200Statistic` (`of`).

## Inventory, not catalog

| Candidate | MCP / adapter | Status | Why wait |
| --- | --- | --- | --- |
| Handshake / login classes | Packet2/1 inside `#connect` | shared site | one Type#method cannot own two roles |
| Packet0 keep-alive | inbound 0 writes Packet10 | alias trap | would steal `PACKET10_FLYING` |
| Packet255 kick | `#disconnect` | no kick oracle | smokes assert clean close only |
| Packet4 time | skipped 8 bytes | skip-only | not `WORLD_TIME` |
| Packet20 named spawn | `B173PeerEquipmentTracker#spawn` | identity only | not a unique catalog target this pass |
| `NET_CLIENT_HANDLER` / `WORLD_CLIENT` | `nb` / `mm` | OBSERVED | no native MP client smoke |
| weather / entity metadata packets | various | unresolved | adapter skips them |

## Conflicts

Packet50 is not `CHUNK_LOAD`. Packet4 is not `WORLD_TIME`.
`SERVER_SAVE_ALL` is not `NATIVE_WORLD_SAVE`. Adapter Packet51 decode
is not `NetClientHandler.handleMapChunk`. Packet5 is not Packet50.

## Next

Split `B173WireClient#connect` before cataloguing handshake/login. Add a
Packet255 kick oracle. Leave NetClientHandler until a client MP gate exists.
