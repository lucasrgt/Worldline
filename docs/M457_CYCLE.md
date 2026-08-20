# M457 qualification cycle

`SpiderLeapSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Spider`, sets night, observes Packet24
type `52` leap toward the actor, and requires touch Packet8 hurt. Golden
apple `322` heals after the hit. Actor Packet13 steps stay at or under 9
blocks. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SpiderLeapSetCycle.java m457-spider-leap-set
```

The frozen semantic SHA-256 is
`c1acc30fb89383a980963eda9ae54bd6fcc4a2c8eaff785ee3a10b3206e3153c`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
