<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d5e39c681248baa95e697c21d1db30d004ed3e6e090fa5dd8feb4fc0b6e34e8c -->

# M501 Entity Item Grounding Smoke Map

## Claim

Worldline reproduces the vanilla Beta 1.7.3 gravity, ground collision, vertical
stop, and age progression of a neutral `EntityItem`. An item seeded three blocks
above a stone surface descends and reaches the ground, while an immediately
supported control stays on that surface. The mapped Worldline path and a
separately compiled official-JAR oracle must emit identical canonical traces.

The claim is limited to this deterministic thirty-tick fixture. It does not
claim item pickup, despawn at age 6000, lava response, damage, serialization,
network packets, horizontal friction, or arbitrary terrain behavior.

## Frozen inputs

| Input | Frozen evidence |
| --- | --- |
| Client JAR | SHA-1 `43db9b498cb67058d2e12d394e6507722e71bb45` |
| Server JAR | SHA-256 `033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d` |
| Version metadata | SHA-256 `be64dfdab54d85c986140b39345c2abc4fb05ad1dc567041a801e8af64f63944` |
| Tiny mappings | SHA-256 `c170d1fde77fccc36649fb2e09066e04670c835e00c0b46fcf6312c9560453a5` |
| Exceptions | SHA-256 `e02f5e01e0e0de6a4223958ce260ee4b4d5afa830a007a6a861afb55efda039d` |

## Behavioral spine

The mapping namespace order is `named`, `client`, `server`. The direct-world
runner checks every row in `symbols.map` against the frozen Tiny mapping.

| Named symbol | Client | Server | Role |
| --- | --- | --- | --- |
| `World` | `fd` | `dj` | Vanilla state container |
| `World.tick()` | `l` | `h` | Advances world time |
| `World.updateEntities()` | `g` | `e` | Executes `EntityItem.onUpdate()` |
| `World.entityJoinedWorld(Entity)` | `b` | `b` | Registers the fixture item in its chunk and world |
| `World.loadedEntityList` | `b` | `b` | Observes entity-count stability |
| `EntityItem` | `hl` | `ez` | Owns item gravity, ground collision, and age progression |
| `EntityItem.age` | `b` | `b` | Proves exactly thirty entity updates occurred |
| `Entity.posY` | `aN` | `aQ` | Observes descent and settlement |
| `Entity.motionY` | `aQ` | `aT` | Observes vertical motion and bounce decay |
| `Entity.onGround` | `aX` | `ba` | Establishes contact with the stone surface |
| `Entity.setPosition(DDD)` | `e` | `c` | Seeds airborne and supported positions |

`World.tick()` does not update entities by itself. Both the mapped backend and
official oracle therefore reproduce the server loop's distinct world tick and
entity-update steps.

## Controlled fixture

- Seed: `50120240820`.
- Chunks: deterministic in-memory 5x5 area with stone through Y=64.
- Airborne item: `(8.0, 68.125, 8.0)` with zero initial velocity.
- Supported control: `(8.0, 65.125, 8.0)` with zero initial velocity.
- Duration: thirty composed world/entity ticks.
- Observation: world time, entity count, quantized Y, quantized vertical
  velocity, `onGround`, and age after every tick.
- Item stack, persistence, players, networking, display, and audio: absent.

Using the empty `EntityItem(World)` constructor avoids constructor-owned random
horizontal velocity. The fixture sets all motion components to zero on both
sides. No Worldline implementation replaces item physics.

## Executable paths

The Worldline scenario drives the public `MinecraftRuntime` lifecycle and a
`GameBackend` that invokes mapped `World.tick()` and `World.updateEntities()`.
The runner inspects the compiled driver/backend bytecode for both calls.

The official oracle compiles directly against the hash-verified obfuscated
server JAR, with no mapped Minecraft or Worldline API classes. The two paths
share only `CanonicalTrace` serialization.

## Pass condition

The runner starts two fresh mapped processes and two fresh official-oracle
processes. Qualification requires deterministic equality inside both pairs,
byte-identical cross-boundary traces, exactly one entity throughout, age 30,
ground contact with zero final vertical velocity, at least 2.5 blocks of
airborne descent, and no more than 0.125 blocks of supported-control drift. The
frozen trace signature is
`d5e39c681248baa95e697c21d1db30d004ed3e6e090fa5dd8feb4fc0b6e34e8c`.

## Frozen semantic signal

`oracle=MATCH,fixture=m501-sw-entity-item-grounding,ticks=30,controlled=true`
