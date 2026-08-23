# M459 ghast fireball hit set

M459 qualifies the official Beta 1.7.3 dedicated-server Nether ghast fireball
hit family as one compound SET. The 8-arg Nether profile logs a dimension
`-1` player onto a seeded netherrack platform with a cobble pad.
`spawn-monsters=true` lets `EntityGhast` emit Packet24 type `56`. The same
session observes Packet23 type `63` whose thrower is that ghast, then the
live Packet60 strength `1` impact.

The netherrack cavern search is restricted to frozen support chunk `2,-1`,
eliminating loaded-chunk iteration order as a fixture choice. The selected
support, fireball hit contract, and signature are unchanged.

The frozen hit is Packet8 hurt and/or destroyed netherrack `87` or cobble
`4` cells from that fireball. This SET does not re-qualify M410 spawn-only
type `63`, M411 pigman pork, gunpowder, TNT, creeper, or Nether-bed blasts.

Frozen semantic SHA-256:
`491a34451873fea634086ff4a8c83a68e25ff5a8ed43d75033d4ed22b63f5042`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
