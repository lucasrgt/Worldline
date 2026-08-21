# M435 remaining natural spawns

M435 opens the official compound remaining-natural Packet24 identity
boundary. With `spawn-monsters=true` and midnight (`time set 14000`) the
headless protocol-14 client observes at least two Packet24 types among
`50`, `51`, `52`, and `54` without placing a mob spawner or rewriting
MobSpawner `EntityId`. This is distinct from M141's single type-`90` pig,
from M363's spawner-seeded zombie type `54` plus skeleton type `51`, from
M388's spawner-seeded hostile drops, and from M390's spawner-seeded
creeper type `50` plus spider type `52`.

Frozen semantic SHA-256:
`a81852d5c2fb5cff300186da2b5d585a72f06f637dffb1942c4a8f1f2284d6d3`.

This milestone does not claim combat, drops, explosions, breeding,
despawn, or a specific pair of hostile types. Headless `B173WireClient`
only. No GUI. No Aero.
