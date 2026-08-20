# M452 knockback cooldown set

M452 opens the official compound zombie melee knockback-plus-cooldown
SET. A saved mob spawner is retargeted from `Pig` to `Zombie`. After
midnight the headless protocol-14 client observes Packet24 type `54`,
takes one Packet7-equivalent melee on the actor, and records Packet38
status `2` then Packet8 health `20 -> 18`. The same hit must move the
actor's Packet13 pose away from the Packet31 mob position. A second
contact inside vanilla hurt-time must not emit a second Packet8 drop.

This family is knockback and cooldown together. It is distinct from env
damage (M307), PvP Packet7 (M66), and sword-hurt on mobs (M463).

Frozen semantic SHA-256:
`242841cb9a28e2404bdfba3f9787b624b4d14c6fdeb9e08665bc5522d4b5f441`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
