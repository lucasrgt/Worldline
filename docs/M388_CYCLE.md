# M388 qualification cycle

`HostileDropsSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Zombie` and `Skeleton`, sets
night, kills Packet24 types `54` and `51`, and requires Packet21 feather
`288` plus arrow `262`. The actor first equips leather armor `298-301` through
four acknowledged personal-window transactions to keep its survival separate
from the hostile drop claim. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/HostileDropsSetCycle.java m388-hostile-drops-set
```

The frozen semantic SHA-256 is
`2e16ed6ddaaf65b63a82ad4de3734ab19f25ce46bb46154ef8ecd10ce680415c`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
