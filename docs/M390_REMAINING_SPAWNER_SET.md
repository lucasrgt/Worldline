# M390 remaining spawner set

M390 opens the official compound remaining-spawner Packet24 identity
boundary. Two saved mob spawners are retargeted from `Pig` to `Creeper`
and `Spider`. After midnight (`time set 14000`) the headless protocol-14
client observes Packet24 type `50` and type `52` as one identity set.
This is distinct from M141's single type-`90` pig and from M363's
zombie type `54` plus skeleton type `51`.

Frozen semantic SHA-256:
`543ebd4ab455f716f9f706ba3647dbe861dfbe4f81d65c002df093f92e401215`.

This milestone does not claim combat, drops, creeper explosion, breeding,
despawn, or other hostile types beyond the two observed Packet24
identities. Headless `B173WireClient` only. No GUI. No Aero.
