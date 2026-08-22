# M388 hostile drops set

M388 opens the official compound hostile loot boundary. Two saved mob
spawners are retargeted from `Pig` to `Zombie` and `Skeleton`. After
midnight (`time set 14000`) the headless protocol-14 client observes
Packet24 type `54` and type `51`, kills both with Packet7 diamond sword
`276`, and collects Packet21 feather `288` plus arrow `262` as one drop
set. Before combat it equips leather armor `298-301` through acknowledged
personal-window transactions so unrelated player death cannot erase the drop
oracle. This is distinct from M363's identity-only Packet24 observation.

Frozen semantic SHA-256:
`2e16ed6ddaaf65b63a82ad4de3734ab19f25ce46bb46154ef8ecd10ce680415c`.

This milestone does not re-qualify armor equipment and does not claim XP,
breeding, despawn, loot-table counts, or other hostile types. Headless
`B173WireClient` only. No GUI. No Aero.
