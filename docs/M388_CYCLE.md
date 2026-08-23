# M388 qualification cycle

`HostileDropsSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Zombie` and `Skeleton`, sets
night, kills Packet24 types `54` and `51`, and requires Packet21 feather
`288` plus arrow `262`. Full diamond armor is seeded only as a bounded-fixture
safety control. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/HostileDropsSetCycle.java m388-hostile-drops-set
```

The frozen semantic SHA-256 is
`c8953605d41925e26176881751113247054635ee807d6cd4f5f76ca9e830cd19`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
