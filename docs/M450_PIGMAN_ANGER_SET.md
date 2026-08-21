# M450 pigman anger set

M450 qualifies the official Beta 1.7.3 dedicated-server Nether zombie-pigman
anger family as one compound SET. A dimension `-1` player seed logs in through
the M130 Nether profile (`allow-nether=true`, `spawn-monsters=true`). Packet15
places two default mob spawners `52` on netherrack `87`. After a clean save
the DIM-1 region NBT `EntityId` is rewritten from `Pig` to `PigZombie` twice
(`unique=false` then `unique=true`, same pattern as `pigAndSheep`).

Official `EntityPigZombie.attackEntityFrom` spreads anger to every pigman
inside a 32-block AABB. Packet7 diamond sword `276` strikes one live Packet24
type `57` identity. Packet38 status `2` records that hurt. The second type
`57` identity must then pursue the actor or Packet38-hurt them. This SET does
not kill for cooked pork `320` (M411) and does not claim the pig/pigman
lightning pair (M437).

Frozen semantic SHA-256:
`ae24558c960284894ed1577e583f5fbbdcfd65ebfd4ed48af6687179d2ccf098`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
