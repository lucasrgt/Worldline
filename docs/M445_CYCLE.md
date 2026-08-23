# M445 qualification cycle

`SkeletonRangedAiSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Skeleton`, and reloads the actor onto
the fence-closed platform with full diamond armor. Only an arena-contained
spawn enters the bounded observation. After `time set 14000` the headless protocol-14 client
observes Packet24 type `51` and two Packet23 type `60` arrows whose
thrower is that skeleton. One official EOF is retried after a 5 second
sleep.

Run directly with:

```text
java tools/smoke/SkeletonRangedAiSetCycle.java m445-skeleton-ranged-ai-set
```

The frozen semantic SHA-256 is
`c397640bf9dddee3c3b93081c4816f82f93289ba759f499d2865fad69fb5d888`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
