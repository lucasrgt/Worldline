# M388 qualification cycle

`HostileDropsSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Zombie` and `Skeleton`, sets
night, kills Packet24 types `54` and `51`, and requires Packet21 feather
`288` plus arrow `262`. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/HostileDropsSetCycle.java m388-hostile-drops-set
```

The frozen semantic SHA-256 is
`af71fe63de80f6405617a61a51d16c2027b51a4b9d198ef70cc5286faa026b45`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
