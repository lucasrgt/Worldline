<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c3830617b51785816a20934139bfc9588dafa1412ba4c9e70e16cc7972a50dd4 -->

# M502 Entity Collision Resolution Smoke Map

## Claim

Worldline can reproduce the vanilla Beta 1.7.3 horizontal push between two
living entities. A pair placed 0.05 blocks apart separates through the vanilla
entity update path, while a two-block-separated control pair stays unchanged.
The mapped Worldline path and an independently compiled official-JAR oracle
produce byte-identical canonical traces in two fresh processes each.

This claim is limited to the controlled ten-tick fixture. It does not claim
combat knockback, attack damage, AI targeting, packet behavior, persistence,
drops, or collision behavior for every entity class.

## Frozen inputs

| Input | Frozen evidence |
| --- | --- |
| Client JAR | SHA-1 `43db9b498cb67058d2e12d394e6507722e71bb45` |
| Server JAR | SHA-256 `033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d` |
| Version metadata | SHA-256 `be64dfdab54d85c986140b39345c2abc4fb05ad1dc567041a801e8af64f63944` |
| Tiny mappings | SHA-256 `c170d1fde77fccc36649fb2e09066e04670c835e00c0b46fcf6312c9560453a5` |
| Exceptions | SHA-256 `e02f5e01e0e0de6a4223958ce260ee4b4d5afa830a007a6a861afb55efda039d` |

## Behavioral spine

The mapping namespace order is `named`, `client`, `server`. Every referenced
vanilla owner, field, and method is checked against the frozen Tiny mapping by
the direct-world runner.

| Named symbol | Client | Server | Role |
| --- | --- | --- | --- |
| `World` | `fd` | `dj` | Vanilla state container |
| `World.tick()` | `l` | `h` | Advances world time and scheduled world behavior |
| `World.updateEntities()` | `g` | `e` | Executes the living-entity update and collision path |
| `World.entityJoinedWorld(Entity)` | `b` | `b` | Registers each fixture entity in the world and chunk |
| `World.loadedEntityList` | `b` | `b` | Observes the registered entity count |
| `World.getWorldTime()` | `t` | `m` | Observes the logical clock |
| `EntityLiving` | `ls` | `hl` | Vanilla living update and push behavior |
| `EntityLiving.health` | `Y` | `ab` | Keeps the neutral fixture entities alive |
| `EntityLiving.updatePlayerActionState()` | `f_` | `c_` | Disabled only to remove autonomous movement from the fixture |
| `Entity.posX` / `posZ` | `aM` / `aO` | `aP` / `aR` | Measures horizontal separation |
| `Entity.setPosition(DDD)` | `e` | `c` | Seeds the overlap and control positions |

`World.tick()` and `World.updateEntities()` are deliberately both invoked.
The official server loop treats world ticking and entity ticking as distinct
steps; calling only `World.tick()` does not exercise living-entity collision.

## Controlled fixture

- Seed: `50220240820`.
- Chunks: deterministic in-memory 5x5 area with stone through Y=64.
- Entity type: a minimal concrete `EntityLiving` subclass on each side.
- Health: 10, so the vanilla living update remains active.
- Autonomous movement: suppressed by a no-op action-state override.
- Positive case: `(8.0, 65.0, 8.0)` and `(8.05, 65.0, 8.0)`.
- Negative control: `(8.0, 65.0, 8.0)` and `(10.0, 65.0, 8.0)`.
- Duration: ten composed world/entity ticks.
- Persistence, players, networking, display, and audio: absent.

The fixture subclass does not implement collision behavior. Collision remains
the inherited vanilla `EntityLiving`/`Entity` behavior. Its only changes are a
live health value and removal of random autonomous movement, identically on the
mapped and official sides.

## Executable paths

The Worldline scenario calls the public `MinecraftRuntime` lifecycle. Its
`GameBackend.tick()` implementation invokes mapped `World.tick()` followed by
mapped `World.updateEntities()`. The runner inspects compiled bytecode and
fails unless the runtime-to-backend-to-`World.tick()` path is present.

The official oracle is separately compiled against the hash-verified official
server JAR. It has no dependency on mapped Minecraft classes or Worldline API
and uses the server names declared in `symbols.map`. Both paths share only the
neutral `CanonicalTrace` serializer.

## Pass condition

The runner starts four fresh JVMs: two mapped Worldline executions and two
official-JAR oracle executions. Qualification requires:

1. deterministic equality inside each pair;
2. byte-identical traces across the mapped/official boundary;
3. a strictly increased but bounded separation for the overlap pair;
4. stable separation within ten milliblocks for the negative control;
5. exactly two registered entities throughout the trace;
6. all declared mappings to resolve in the frozen mapping file; and
7. the frozen SHA-256 trace signature
   `c3830617b51785816a20934139bfc9588dafa1412ba4c9e70e16cc7972a50dd4`.

## Frozen semantic signal

`oracle=MATCH,fixture=m502-sw-entity-collision-resolution,ticks=10,controlled=true`
