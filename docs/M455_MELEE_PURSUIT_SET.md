# M455 melee pursuit set

M455 opens the official compound melee-pursuit boundary. Two saved mob
spawners are retargeted from `Pig` to `Zombie` and `Skeleton`. After
midnight (`time set 14000`) the headless protocol-14 client observes
Packet24 type `54` and type `51` both pursuing the actor: a Packet31,
Packet33, or Packet34 displacement whose horizontal vector points toward
the live player pose. Movement steps are capped at 9 blocks. Golden
apple `322` keeps the actor alive.

This is distinct from M363's identity-only Packet24 observation and from
M388/M422 death drops. It does not claim skeleton shooting (M445) or
zombie door break (M446). Headless `B173WireClient` only. No GUI. No Aero.

Frozen semantic SHA-256:
`36fea72b3152e1d8b6245cfd8731ba14fa83aa5818bef04bab2ab838441de935`.
