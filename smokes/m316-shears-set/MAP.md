# M316 behavior map

The fixture raises an isolated `7×7` grass platform, places oak log `17`
plus player-placed leaves `18:8`, and shears that leaf with item `359`.
Packet14 emits Packet21 stack id `18`. The same session then places mob
spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Sheep`. Packet7 interact while holding shears item `359`
drops Packet21 stack id `35` and does not complete Packet38 status 3.

This map does not claim dye colors, shears durability, cobweb harvest, or
sheep death drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+oak17+leaves18+sheep-spawner52|cause=packet14-shears359+packet7-interact-shears359|wire=packet53-air+packet21-id18+packet24-type91+packet21-id35+no-packet38-status3|oracle=shears-leaf-and-living-sheep-wool|column=17,platform=7x7-48grass,log=7:72:4:17:0,sheared=7:73:4:18:8->0:0,spawner=52:0-sheep,mob=type91,shears=359,drops=packet21-18+packet21-35,death=no-packet38-status3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`91eec7f3061f3c9cb956cd25ebcc7ece6a66055262a45538081a8ad72d79426e`.
