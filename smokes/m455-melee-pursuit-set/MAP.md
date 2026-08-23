<!-- worldline-map-schema=1 -->
<!-- boundary=m455-melee-pursuit-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=36fea72b3152e1d8b6245cfd8731ba14fa83aa5818bef04bab2ab838441de935 -->

# M455 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Zombie` and `Skeleton` with `unique=false` on the
first rewrite. Console time `14000` makes the platform dark enough for
`EntityMob.getCanSpawnHere`. Packet24 identities type `54` and type `51`
must each emit a Packet31/33/34 step whose horizontal vector points toward
the actor pose. Golden apple `322` heals; the actor must not die.

This map does not claim pig identity (M141), identity-only hostiles (M363),
zombie feather / skeleton arrow drops (M388/M422), skeleton shooting (M445),
or zombie door break (M446).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-zombie+skeleton+time-14000|wire=packet24-type54+packet24-type51+packet31-or33-or34-toward-pose|oracle=zombie-and-skeleton-melee-pursuit-toward-actor-pose|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Zombie+Skeleton,mobs=type54+type51,night=14000,apple=322,pursuit=toward-pose,move-cap=9,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`36fea72b3152e1d8b6245cfd8731ba14fa83aa5818bef04bab2ab838441de935`.
