# M363 hostile identity set

M363 opens the official compound hostile Packet24 identity boundary. Two
saved mob spawners are retargeted from `Pig` to `Zombie` and `Skeleton`.
After midnight (`time set 14000`) the headless protocol-14 client observes
Packet24 type `54` and type `51` as one identity set. This is distinct from
M141's single type-`90` pig.

Frozen semantic SHA-256:
`e6df497cd2826b04e3930ffb08caa875bba470b29a8b5bad4ce5cc75d48db14d`.

This milestone does not claim combat, drops, breeding, despawn, or other
hostile types beyond the two observed Packet24 identities. Headless
`B173WireClient` only. No GUI. No Aero.
