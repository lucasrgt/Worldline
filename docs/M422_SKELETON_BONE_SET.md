# M422 skeleton bone set

M422 opens the official skeleton-bone family. One saved mob spawner is
retargeted from `Pig` to `Skeleton`. The actor reloads onto the platform.
Twenty-four fence blocks `85:0` close the platform perimeter, and the client
accepts only an arena-contained spawn. A bounded movement attempt now fails
closed instead of silently waiting on a skeleton knocked into the void.
The personal 2x2 grid mills bone `352` to bone meal `351x3:15`, then
Packet15-applies that meal to wheat `59:0` and observes mature `59:7`
before midnight. After `time set 14000` the headless protocol-14 client
observes Packet24 type `51`, kills it with Packet7 diamond sword `276`,
and collects Packet21 bone `352`. Hoe and wheat stay off the skeleton
archery window. This is distinct from M388's skeleton arrow `262`.

Frozen semantic SHA-256:
`131ebd45e9b81d7f65d182b85fef0d213bda1f3d8521b4e4f403d5958aa1f0c0`.

This milestone does not claim dye-family milling (M328), wheat growth
families (M305), tree generation (M140), XP, loot-table counts, or other
hostile types. Headless `B173WireClient` only. No GUI. No Aero.
