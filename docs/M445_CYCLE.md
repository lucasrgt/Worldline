# M445 qualification cycle

`SkeletonRangedAiSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Skeleton`, and reloads the actor onto
the platform. After `time set 14000` the headless protocol-14 client
observes Packet24 type `51` and two Packet23 type `60` arrows whose
thrower is that skeleton. One official EOF is retried after a 5 second
sleep.

Run directly with:

```text
java tools/smoke/SkeletonRangedAiSetCycle.java m445-skeleton-ranged-ai-set
```

The frozen semantic SHA-256 is
`59d850eaeeb297f3879633c70a546d1aa4da2de0618852cb9f3e802a8ec6533b`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
