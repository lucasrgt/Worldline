# M455 qualification cycle

`MeleePursuitSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Zombie` and `Skeleton`, sets
night, and requires Packet24 types `54` and `51` both to emit a movement
vector toward the actor pose. Golden apple `322` keeps the actor alive.
One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/MeleePursuitSetCycle.java m455-melee-pursuit-set
```

The frozen semantic SHA-256 is
`36fea72b3152e1d8b6245cfd8731ba14fa83aa5818bef04bab2ab838441de935`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
