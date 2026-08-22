# Semantic Audit — Player Lifecycle and Physics

Domain inventory for `origin/main` `1755fa8` (v1.55.0 / M67). Confirmed through
`mappings.tiny`, M24-M67 docs/smokes, and `adapters/b173-server`.

## Already catalogued

`LOCAL_PLAYER`, `PLAYER_TYPE`, `LIVING_TYPE`, `PLAYER_HEALTH`,
`ENTITY_POS_*`, `ENTITY_DEAD`, `ENTITY_SET_POSITION`,
`ENTITY_SET_LOCATION`, `MOVEMENT` (input, not physics), `TELEPORT`,
`SPAWN_SET` (world spawn, not respawn).

## Promoted

| Role | MCP | Official | Kind | Descriptor | Status | Consumer |
| --- | --- | --- | --- | --- | --- | --- |
| `PACKET10_FLYING` | `Packet10Flying` | `ig` | class | `-` | ORACLED | M32 heartbeat |
| `PACKET12_PLAYER_LOOK` | `Packet12PlayerLook` | `vh` | class | `-` | ORACLED | `B173PlayChannel#look` |
| `PACKET13_PLAYER_LOOK_MOVE` | `Packet13PlayerLookMove` | `ev` | class | `-` | ORACLED | `B173PlayChannel#synchronize` |
| `PACKET_STANCE` | `Packet10Flying.stance` | `d` | field | `D` | ORACLED | `B173PlayInbound#position` |
| `PACKET8_UPDATE_HEALTH` | `Packet8UpdateHealth` | `eu` | class | `-` | ORACLED | M66 `B173CombatTracker#health` |
| `PACKET7_USE_ENTITY` | `Packet7UseEntity` | `a` | class | `-` | ORACLED | M66 `B173CombatChannel#attack` |
| `PACKET38_ENTITY_STATUS` | `Packet38EntityStatus` | `jf` | class | `-` | ORACLED | M66 `B173CombatTracker#status` |

Packet13 has no field rows in `mappings.tiny`; pose scalars live on
Packet10Flying. Server payload order is stance then feet; client ack
inverts them. Packet7 official `a` is the class row, not
`attackEntityFrom`.

## Not promoted

`AxisAlignedBB`, `Entity.moveEntity`, `motionY`, `onGround`,
`respawnPlayer`, `Minecraft.respawn`, `EntityPlayer.dimension`,
`yOffset` as an alias of stance. M34/M35 explicitly do not claim
collision or gravity. `PACKET_STANCE` must not be merged with
`ENTITY_Y_OFFSET`.

## Test still required

Official-JAR collision/gravity differential; death-to-respawn;
dimension transition; stance versus `Entity.yOffset`.
