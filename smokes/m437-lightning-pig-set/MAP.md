# M437 behavior map

Official server symbols:

- `ga` (`EntityList`) maps `cz` (`EntityPig`) to name `Pig` and Packet24
  type `90`.
- The same table maps `pi` (`EntityPigZombie`) to name `PigZombie` and
  Packet24 type `57`.
- `cz.a(lq)` (`EntityPig.onStruckByLightning`) constructs `pi`, copies pose,
  joins the world, and marks the pig dead. The live conversion is not a
  bounded headless path: weather lightning is `World.r.nextInt(100000) == 0`
  per loaded chunk while raining and thundering.
- Lightning itself is Packet71 weather (`entityId`, type byte `1`, three
  ints), not Packet23. This map does not invent a Packet23 tracker.

A raised Overworld `7×7` grass platform plus default spawner `52` emits
Packet24 type `90`. After a clean Nether save, DIM-1 `EntityId Pig` becomes
`PigZombie` and Packet24 type `57` is observed. That identity pair is the
lightning-conversion family without claiming a Packet71 strike and without
M411 cooked-pork `320`.

This map does not claim weather, thunder time, Packet71 decoding, gold-sword
rare drops, anger spread, XP, or M411 pork.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=overworld-animals+allow-nether-true+spawn-monsters-true|entry=overworld-item1+item2+item52+prelogin-player-nbt-dimension-minus-one+item87+item52|fixture=raised-7x7-grass-platform+spawner52+nether-netherrack87+spawner52|cause=official-spawner-pig+nbt-entityid-pigzombie|wire=packet24-type90+packet24-type57|oracle=overworld-pig90-nether-pigman57-contrast-not-weather-lightning-not-packet23-not-m411-pork|dimension=0+-1,overworld-spawner=4:72:4:52:0,nether-spawner=8:9:3:52:0,entityid=Pig+PigZombie,mobs=type90+type57,column=17,platform=7x7-48grass,packet23=absent,packet71=unclaimed,not-m411-pork,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`536016d5292cf2d747ea4a029011726719795579c19e8507ec912154e9bd77db`.
