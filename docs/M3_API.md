# M3 Stable Domain API

Worldline v0.1.0 promotes a small neutral automation surface for Minecraft
Beta 1.7.3. Public domain types live in `worldline-api` and contain no mapped,
RetroMCP, LWJGL, or Minecraft classes.

## Entry point

`AutomatedMinecraftRuntime` extends the v0.0.1 `MinecraftRuntime` lifecycle and
adds `world()` and `player()`. These methods are valid only in
`WORLD_LOADED`. Access before load or after close fails without reaching the
backend.

Returned world, player, and entity objects are live handles. They become
invalid when their runtime closes and every operation then fails closed.

## Stable contracts

| Type | Stable M3 behavior |
| --- | --- |
| `GameWorld` | Logical world time, block read/write, immutable active-entity snapshot |
| `BlockPosition` | Exact immutable integer coordinate with value equality |
| `BlockState` | Immutable legacy ID `0..255` and metadata `0..15` |
| `GameEntity` | Runtime-local ID, semantic type, position, alive state, teleport |
| `GamePlayer` | Entity behavior plus username, health, hotbar read/select `0..8` |
| `GamePosition` | Exact finite double coordinates with value equality |

`GameWorld.entities()` includes the controlled local player even though the
headless fixture intentionally does not register it in vanilla's internal
loaded-entity list. The returned list is a caller-immutable snapshot; its
entity handles remain live.

`minecraft:player` is the only promoted semantic entity type in M3. Other
entities return `worldline:unknown` until their mappings receive independent
oracle coverage. Entity IDs are unique only within the live runtime and are not
durable identities across restore, replay, saves, or processes.

## Concurrency and mutation

The API is designed for the single externally controlled runtime thread.
Concurrent calls are outside M3. `teleport` delegates to vanilla
`Entity.setPosition`; `setBlock` delegates to vanilla notified block+metadata
mutation; hotbar selection writes the vanilla selected-slot state. Subsequent
controlled ticks execute normal vanilla behavior.

## Non-claims

M3 does not include entity spawning/removal, a complete entity-type registry,
inventory item manipulation, dimensions, chunks, tile entities, arbitrary save
loading, multiplayer/server automation, thread safety, durable entity IDs, or
a version-independent block registry. Those require later contracts and their
own official-JAR evidence. The experimental `GamePlayer.items()`,
`GameWorld.items()`, and `GameWorld.blocks()` censuses are the Invariant
Engine observation surface; they are not M3 inventory or world mutation.
