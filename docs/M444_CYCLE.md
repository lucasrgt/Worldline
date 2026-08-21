# M444 qualification cycle

`RemainingMobDropsRestCycle` rebuilds the raised grass platform in two
fresh official server JVMs. Each run places two default spawners `52`,
retargets the first saved MobSpawner `EntityId` to `Sheep`, leaves the
second as `Pig`, and kills Packet24 types `90` and `91` with diamond
sword `276`. Packet21 must include pork `319` and undyed wool `35:0`.
One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingMobDropsRestCycle.java m444-remaining-mob-drops-rest
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`4f0cf6fc97f045251947014072b407aae095b6419fb3c3ab94c50722f7db8f66`.
