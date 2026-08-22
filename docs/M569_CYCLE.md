# M569 qualification cycle

`SpawnerDelaySetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Zombie`, rewrites `Delay` to `1`, sets
night, requires Packet24 type `54` to stay absent while the actor is 24
blocks above the spawner, then requires type `54` after the actor stations
in range. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SpawnerDelaySetCycle.java m569-spawner-delay-set
```

The frozen semantic SHA-256 is
`f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
