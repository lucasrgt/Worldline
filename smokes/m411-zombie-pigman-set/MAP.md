<!-- worldline-map-schema=1 -->
<!-- boundary=m411-zombie-pigman-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c448868efb22d1c2a50bab9554f6c30de3f184d9cc2eb129103068be9868ae84 -->

# M411 behavior map

Official server symbols:

- `ga` (`EntityList`) maps `pi` (`EntityPigZombie`) to name `PigZombie` and
  Packet24 type `57`.
- `pi.j` returns `ej.ap.bf`, the shifted index of `porkchopCooked`. Item
  constructor `bipush 64` is legacy id `320`. This is cooked porkchop, not
  later gold nuggets `371`.
- `pi.d` (`getCanSpawnHere`) requires difficulty `> 0`, a clear AABB, and no
  liquid. It does not require darkness, so a Nether spawner can emit type
  `57` without midnight.
- Region rewrite targets `world/DIM-1/region`, not Overworld `world/region`.

A dimension `-1` login places default spawner `52` on netherrack `87`. After
save, `EntityId Pig` becomes `PigZombie`. Packet7 diamond sword `276` kills
the Packet24 identity; Packet21 must include cooked pork `320`.

This map does not claim Overworld pigmen, gold-sword rare drops, anger
spread radius, XP, or M388 zombie/skeleton loot.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|entry=prelogin-player-nbt-dimension-minus-one+item52+item276|fixture=nether-netherrack87+spawner52|cause=nbt-entityid-pigzombie+diamond-sword-packet7|wire=packet24-type57+packet38-status3+packet29+packet21-320|oracle=nether-pigzombie57-cooked-pork-320-not-m388-zombie-feather|dimension=-1,netherrack=positive,spawner=8:9:3:52:0,entityid=PigZombie,mob=type57,sword=276,drop=packet21-320,kills<=8,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c448868efb22d1c2a50bab9554f6c30de3f184d9cc2eb129103068be9868ae84`.
