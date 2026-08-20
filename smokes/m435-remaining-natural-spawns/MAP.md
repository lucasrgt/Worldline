# M435 behavior map

The fixture climbs a dry stone column from the seed water cell so the
actor is not drowning. Dedicated-server `spawn-monsters=true` stores
world difficulty above peaceful. Console time `14000` makes loaded
surface and cave cells dark enough for `EntityMob.getCanSpawnHere`.
No mob spawner `52` is placed and no region NBT `EntityId` is rewritten.
Packet24 must include at least two types among `50`, `51`, `52`, and `54`
with non-player entity IDs.

This map does not claim pig identity (M141), spawner-seeded zombie plus
skeleton (M363), spawner-seeded creeper plus spider (M390), hostile drops
(M388), combat, AI pathing, or exact spawn coordinates.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=spawn-monsters-true|entry=overworld-login+item1|fixture=dry-stone-column-no-spawner|cause=time-14000+natural-spawnercreature|wire=packet24-hostile-family-50-51-52-54|oracle=two-plus-natural-hostile-packet24-identities|column=11,spawn-monsters=true,night=14000,spawners=absent,entityid=unmodified,family=50+51+52+54,observed>=2,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`a81852d5c2fb5cff300186da2b5d585a72f06f637dffb1942c4a8f1f2284d6d3`.
