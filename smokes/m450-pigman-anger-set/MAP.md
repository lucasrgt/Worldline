<!-- worldline-map-schema=1 -->
<!-- boundary=m450-pigman-anger-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ae24558c960284894ed1577e583f5fbbdcfd65ebfd4ed48af6687179d2ccf098 -->

# M450 behavior map

Official server symbols:

- `ga` (`EntityList`) maps `pi` (`EntityPigZombie`) to name `PigZombie` and
  Packet24 type `57`.
- `pi.a(sn, int)` (`attackEntityFrom`) expands the AABB by `32` and calls
  `becomeAngryAt` on every nearby `EntityPigZombie`, then on the struck
  identity. `angerLevel` is `400 + rand.nextInt(400)`.
- `pi.d_()` (`findPlayerToAttack`) returns null while `angerLevel == 0`, so
  pigmen are peaceful until that Packet7 hit.

A dimension `-1` login places two default spawners `52` on netherrack `87`.
After save, DIM-1 `EntityId Pig` is rewritten twice (`unique=false` then
`unique=true`) to `PigZombie`. Packet7 diamond sword `276` strikes one live
type-`57` identity. Packet38 status `2` is required on that identity. The
neighbor type-`57` must then pursue or Packet38-hurt the actor. Death and
cooked-pork `320` are refused.

This map does not claim M411 pork, gold-sword rare drops, XP, M437 pig 90 /
lightning Packet71, or weather.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item52+item276|fixture=nether-netherrack87+two-spawner52|cause=nbt-entityid-pigzombie+diamond-sword-packet7|wire=packet24-type57+packet24-type57+packet38-status2|oracle=nether-pigzombie57-neighbor-anger-not-m411-pork-not-m437-lightning|dimension=-1,netherrack=positive,first=8:9:3:52:0,second=9:9:3:52:0,entityid=PigZombie,mobs=type57+type57,sword=276,hit=packet7,hurt=packet38-status2,aggro=neighbor,not-m411-pork,not-m437-lightning,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ae24558c960284894ed1577e583f5fbbdcfd65ebfd4ed48af6687179d2ccf098`.
