# M564 qualification cycle

`SpawnLightSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Creeper` and `Zombie`, sets
night `14000`, and requires nearby Packet24 type `50` or `54`. The same
pad then receives forty-nine floor torches `50:5` (light 14) and nearby
types `50` and `54` must stay absent. One official EOF is retried after
a 5 second sleep.

Run directly with:

```text
java tools/smoke/SpawnLightSetCycle.java m564-spawn-light-set
```

The frozen semantic SHA-256 is
`45458f0fd9d3a18ec9205472afef562dea56312353613004d3fe5c1b8374d503`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
