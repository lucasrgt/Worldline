# M406 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values
are rewritten from `Pig` to `Sheep`. Packet7 with dye `351:1` then dye
`351:11` colors two living Packet24 type-`91` sheep. Packet7 interact
while holding shears item `359` drops Packet21 stacks `35:14` and `35:4`
and does not complete Packet38 status 3.

This map does not re-qualify M316 undyed shears wool or wool crafts
M315/M368/M396. It does not claim breeding, wool growth, cobweb harvest,
or sheep death drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-sheep-spawner52|cause=packet7-dye351:1+packet7-dye351:11+packet7-interact-shears359|wire=packet24-type91+packet21-35:14+packet21-35:4+no-packet38-status3|oracle=dyed-sheep-wool-set|column=17,platform=7x7-48grass,red=4:72:4:52:0,yellow=5:72:4:52:0,mobs=type91+type91,dyes=351:1+351:11,shears=359,drops=packet21-35:14+packet21-35:4,death=no-packet38-status3,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`0c2857eb2e2bf4aaa39c631eced8f47d470862396ae8e7981d41c6c0a0775cb7`.
