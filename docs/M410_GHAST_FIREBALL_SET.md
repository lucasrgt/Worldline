# M410 ghast fireball set

M410 qualifies the official Beta 1.7.3 dedicated-server Nether ghast family as
one compound SET. The 8-arg Nether profile logs a dimension `-1` player onto a
seeded netherrack platform. `spawn-monsters=true` lets `EntityGhast` emit
Packet24 type `56`. The same session then observes the live Packet23 fireball
object whose type is discovered from the official server and whose thrower is
the ghast identity.

The live official Packet23 type is `63`. Frozen object types
`1,10,11,12,60,61,62,70,71,90` are excluded. This SET does not claim pigmen,
gunpowder, or fireball crater geometry.

Frozen semantic SHA-256:
`4a77f0136d56574b37e6aca69072e884a92ea9240a1904aca9aaaa8170e08b76`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
