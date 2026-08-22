# M564 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Creeper` and `Zombie`. Console time `14000` makes
the platform dark enough (`light <= 7`) for `EntityMob.getCanSpawnHere`.
Packet24 type `50` or type `54` must appear inside the official spawner
volume. A copied world then places forty-nine floor torches `50:5` on the
same pad (light 14) and the same night must keep nearby Packet24 types
`50` and `54` absent.

This map does not claim pig identity (M141), identity-only natural hostiles
(M435), remaining-spawner creeper+spider identity (M390), melee pursuit
(M455), combat, drops, or explosions.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-creeper+zombie+time-14000+torch-50-5-light-14|wire=packet24-type50-or-54-dark+packet24-type50-or-54-absent-torch|oracle=dark-spawn-torch-reject-not-m435-natural-not-m390-identity-not-m141-pig|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Creeper+Zombie,dark=type50-or-54,torch=50:5x49,torch-light=14,torch-arm=absent,night=14000,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`45458f0fd9d3a18ec9205472afef562dea56312353613004d3fe5c1b8374d503`.
