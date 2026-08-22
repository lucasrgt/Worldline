# M564 spawn light set

M564 opens the official hostile spawn-light boundary. Two saved mob
spawners are retargeted from `Pig` to `Creeper` and `Zombie`. After
midnight (`time set 14000`) the headless protocol-14 client observes
nearby Packet24 type `50` or type `54` on a dark pad (`light <= 7`).
Forty-nine floor torches `50:5` on the same pad raise block light to 14
and nearby types `50` and `54` are rejected.

This is distinct from M435's identity-only natural spawns without a
spawner rewrite, from M390's remaining-spawner creeper+spider identity
without a light contrast, and from M141's default pig type `90`. It does
not claim combat, drops, creeper explosion, or melee pursuit.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`45458f0fd9d3a18ec9205472afef562dea56312353613004d3fe5c1b8374d503`.
