# M3 Domain API Differential Map

## Claim

Worldline v0.1.0 exposes a neutral stable API for world, block, entity, and
local-player automation. A subject using those contracts produces the same
observable state as an independent oracle using official obfuscated b1.7.3
fields and methods.

## Scenario

Both sides construct the same headless world and local player, fix RNG, and
record loaded state. They then place glass ID 20 with metadata 3 at `(8,65,8)`,
teleport the player to `(10.5,66,10.5)`, select hotbar slot 4, advance three
client ticks, and record final state.

The trace contains world time, two block states, semantic entity count,
runtime-local player ID, alive state, exact position bits, health, and selected
slot. Four fresh JVMs - two per side - must agree and equal:

```text
d38186377edc68f8080e568ffaba6559c4b3980fcf2a5311aac1b6ec7ebcc13c
```

## Exact boundary

The subject imports b1.7.3 code only to obtain the runtime factory; every game
interaction is through `AutomatedMinecraftRuntime` and neutral `worldline-api`
types. The oracle is compiled directly against the hash-verified official JAR
and uses no M3 implementation classes. Named/client/server mappings for block
metadata, notified mutation, entity identity/liveness/position, player
username/health, and inventory selection are checked before execution.

## Non-claims

The fixture covers one local player, one in-memory world, one block mutation,
and three ticks. It does not establish every entity type, arbitrary worlds,
inventory contents, multiplayer behavior, or durable identity semantics. The
normative stable scope is in `docs/M3_API.md`.
