# M390 qualification cycle

`RemainingSpawnerSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Creeper` and `Spider`, sets
night, and requires Packet24 types `50` and `52`. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingSpawnerSetCycle.java m390-remaining-spawner-set
```

The frozen semantic SHA-256 is
`543ebd4ab455f716f9f706ba3647dbe861dfbe4f81d65c002df093f92e401215`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
