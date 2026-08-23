<!-- worldline-map-schema=1 -->
<!-- boundary=m454-peaceful-despawn-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8a4c4acadf23008e8fed2fdbc1d9c05c903c65c527c3489dabee48e7d2183abe -->

# M454 behavior map

Official server symbols:

- `PropertyManager` reads `spawn-monsters`. True stores world difficulty
  Easy `1`; false stores Peaceful `0`. `EntityMob` despawns when
  difficulty is `0`. `B173ServerProperties.difficulty` writes both keys.
- Easy still sets `spawn-monsters=true`. Peaceful is a second boot of
  the same world with `difficulty=0` because live difficulty is not a
  b1.7.3 console command.
- Default mob spawner `52` writes `EntityId=Pig`. After a clean Easy
  save, `B173SpawnerSeed.entity` rewrites Creeper and Zombie.
- Night (`time set 14000`) lets `EntityMob.getCanSpawnHere` succeed on
  Easy. Packet24 types `50` and `54` persist. Peaceful keeps types among
  `50`, `51`, and `54` absent or despawned.

This map does not claim M435 natural identity without difficulty, loot,
combat, spider type `52`, M363 skeleton pairing, or M390 spider pairing.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profiles=peaceful-difficulty-0+easy-difficulty-1-spawn-monsters-true|entry=overworld-login+item1+item2+item52|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-creeper+zombie+time-14000+server-properties-difficulty|wire=packet24-type50+packet24-type54|oracle=easy-persist-peaceful-absent-or-despawn-not-m435-natural|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Creeper+Zombie,family=50+51+54,easy=type50+type54+persist,peaceful=absent,peaceful-difficulty=0,easy-difficulty=1,spawn-monsters=true,night=14000,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`8a4c4acadf23008e8fed2fdbc1d9c05c903c65c527c3489dabee48e7d2183abe`.
