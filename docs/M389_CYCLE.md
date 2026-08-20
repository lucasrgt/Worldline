# M389 qualification cycle

`AnimalDropsSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Cow` and `Chicken`, and kills
Packet24 types `92` and `93` with diamond sword `276`. Packet21 must include
leather `334` and feather `288`. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no GUI
and no Aero path.

Run directly with:

```text
java tools/smoke/AnimalDropsSetCycle.java m389-animal-drops-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`761e3177132b22cd98c5dd6a4fa802903098e923c7e0c31115dfae512c06213b`.
