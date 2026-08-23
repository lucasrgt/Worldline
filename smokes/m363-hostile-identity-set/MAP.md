<!-- worldline-map-schema=1 -->
<!-- boundary=m363-hostile-identity-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e6df497cd2826b04e3930ffb08caa875bba470b29a8b5bad4ce5cc75d48db14d -->

# M363 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Zombie` and `Skeleton`. Console time `14000` makes
the platform dark enough for `EntityMob.getCanSpawnHere`. Packet24 must
include type `54` and type `51` with distinct non-player entity IDs.

This map does not claim pig identity (M141), combat, drops, AI pathing, or
natural cave spawning.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-zombie+skeleton+time-14000|wire=packet24-type54+packet24-type51|oracle=two-hostile-packet24-identities|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Zombie+Skeleton,mobs=type54+type51,night=14000,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`e6df497cd2826b04e3930ffb08caa875bba470b29a8b5bad4ce5cc75d48db14d`.
