# M390 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Creeper` and `Spider`. Console time `14000` makes
the platform dark enough for `EntityMob.getCanSpawnHere`. Packet24 must
include type `50` and type `52` with distinct non-player entity IDs.

This map does not claim pig identity (M141), zombie+skeleton identity (M363),
combat, drops, explosions, AI pathing, or natural cave spawning.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-creeper+spider+time-14000|wire=packet24-type50+packet24-type52|oracle=two-remaining-spawner-packet24-identities|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Creeper+Spider,mobs=type50+type52,night=14000,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`543ebd4ab455f716f9f706ba3647dbe861dfbe4f81d65c002df093f92e401215`.
