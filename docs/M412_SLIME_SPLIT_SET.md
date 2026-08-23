# M412 slime split set

M412 opens the official compound slime size-split boundary. A headless
protocol-14 client places one default spawner in seed `17320110707` slime
chunk `-2,-2` below `y=16`, rewrites MobSpawner `EntityId` to `Slime`,
observes Packet24 type `55`, and kills a larger slime with Packet7 diamond
sword `276`. Child Packet24 type `55` identities must appear after the
parent Packet38 status 3 and Packet29 destroy. That parent-plus-children
family is the SET, distinct from one spawn and from M363/M388 generic
hostile lists. A player-NBT diamond armor set `310`–`313` keeps the actor alive
through the bounded repeated-kill loop without changing the split oracle.

Tiny size-1 slimes may drop slimeball `341`. Drop count stays outside the
frozen hash.

Frozen semantic SHA-256:
`c35f4196ac8b8633b7ffa097ac87eca1828f89b9ab9d2be20f0802c924191929`.

This milestone does not claim XP, breeding, despawn, loot-table counts, or
other hostile types. Headless `B173WireClient` only. No GUI. No Aero.
