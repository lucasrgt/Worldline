# M452 knockback cooldown set

M452 opens the official compound zombie melee knockback-plus-cooldown
SET. A saved mob spawner is retargeted from `Pig` to `Zombie`. After
midnight the headless protocol-14 client observes Packet24 type `54`,
takes one Packet7-equivalent melee on the actor, and records Packet38
status `2` then Packet8 health `20 -> 18`. The same hit must emit Packet28
velocity directed upward and away from the Packet31 mob position. A second
contact inside vanilla hurt-time must not emit a second Packet8 drop.

This family is knockback and cooldown together. It is distinct from env
damage (M307), PvP Packet7 (M66), and sword-hurt on mobs (M463).

Frozen semantic SHA-256:
`096b8ab01152b6efc7574f50c63e4f48562ea96d7a3a095b2daaa2faecde5e48`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
