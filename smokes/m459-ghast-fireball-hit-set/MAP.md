# M459 behavior map

Official server symbols:

- `net.minecraft.src.EntityGhast` is Packet24 type `56`. It spawns only in
  dimension `-1` when `spawn-monsters=true` and a 4-high air column exists.
- `net.minecraft.src.EntityFireball` is Packet23 type `63`. The thrower is
  the ghast entity ID. Impact emits Packet60 at strength `1`.
- Packet8 health below `20` is player hurt from that blast. Destroyed
  netherrack `87` or cobble `4` cells are the crater. Either observation
  freezes the hit. Lava `10`/`11` is not a fireball hit.

The cavern scan is restricted to the frozen support chunk `2,-1`. This makes
the existing support coordinate independent of loaded-chunk iteration order;
the hit trace and signature remain unchanged.

This map does not re-qualify M410 spawn-only type `63`, M411 pigman pork
`320`, TNT strength `4`, creeper strength `3`, or Nether-bed strength `5`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item87+item4+item52|fixture=nether-netherrack87-platform+cobble4-pad+spawner52-ghast|cause=nbt-entityid-ghast+ghast-los+fireball-impact|wire=packet24-type56+packet23-type63-thrower-ghast+packet60-strength1+packet8-or-crater|oracle=nether-ghast-fireball-hit-not-m410-spawn-only-not-m411-pork|dimension=-1,support=36:57:-14:87,pads=0,cobble-pads=1,spawner=36:58:-15:52:0,entityid=Ghast,ghast=type56,fireball=type63,thrower=ghast,packet60=strength1,hit=packet8-or-crater,not-m410-spawn-only,not-m411-pork,packet23-known=absent,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`491a34451873fea634086ff4a8c83a68e25ff5a8ed43d75033d4ed22b63f5042`.
