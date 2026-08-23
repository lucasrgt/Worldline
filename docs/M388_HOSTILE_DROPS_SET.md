# M388 hostile drops set

M388 opens the official compound hostile loot boundary. Two saved mob
spawners are retargeted from `Pig` to `Zombie` and `Skeleton`. After
midnight (`time set 14000`) the headless protocol-14 client observes
Packet24 type `54` and type `51`, kills both with Packet7 diamond sword
`276`, and collects Packet21 feather `288` plus arrow `262` as one drop
set. A player-NBT diamond armor set `310`–`313` makes the bounded combat loop
survivable without changing mob identity, attacks, or loot. This is distinct
from M363's identity-only Packet24 observation.

Frozen semantic SHA-256:
`c8953605d41925e26176881751113247054635ee807d6cd4f5f76ca9e830cd19`.

This milestone does not claim XP, breeding, despawn, loot-table counts, or
other hostile types. Headless `B173WireClient` only. No GUI. No Aero.
