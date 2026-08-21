# M464 explosion player hurt set

M464 opens the official compound explosion-player-hurt SET. A retargeted
creeper emits Packet24 type `50` and protocol-14 Packet60 at strength
`3`, which drops the actor's Packet8 health. After a golden-apple `322`
restore, TNT `46` flint-and-steel prime emits Packet60 at strength `4`
and drops Packet8 again. The actor stands far enough to survive both
blasts. The family is player-hurt-from-explosions, not a block crater.

This is distinct from M137 TNT destroyed cells, M391 creeper wool/dirt
crater, and M359 Nether-bed Packet60 strength `5`. The frozen signal
names both strengths, Packet8, type `50`, and survival.

Frozen semantic SHA-256:
`57b40f0692f328b32fc02a568a0f47854d1d21fda0e2ed383262bf03c2b8c078`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
