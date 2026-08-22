# M468 qualification cycle

`TamedWolfAssistSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Sheep` then `Wolf`, waits for
Packet24 type `95`, and uses bone item `352` with Packet7 button 0 until
Packet38 status `7`. After the wolf sits, pending sheep are discarded so a
fresh Packet24 type `91` can spawn at full health. The owner unsits that
wolf and strikes the sheep once with diamond sword `276`. The tamed wolf
must produce target Packet38 status `2` and a death not caused only by
that single player hit. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and
no Aero path.

Run directly with:

```text
java tools/smoke/TamedWolfAssistSetCycle.java m468-tamed-wolf-assist-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`21920ae6ac95c99bc80e2adfef34dd66a8649b88c45836f5e61119f5fee019d6`.
