# M457 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Spider`. Console time `14000` makes the platform dark enough
for `EntityMob.getCanSpawnHere`. Packet24 identity type `52` must leap toward
the standing actor (relative Packet31/33 with rising Y and closing XZ).
Touch Packet8/38 then hurts the actor. Golden apple `322` heals. The pad is
flat, so the Y rise is a leap, not a wall climb.

This map does not claim spider climb (M447), string drop (M409), cobweb
place, natural-spawn identity (M435), XP, or other hostile types.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52|cause=nbt-entityid-spider+time-14000+proximity-leap+touch|wire=packet24-type52+packet31-or33-leap-y+packet38-status2+packet8|oracle=spider-leap-toward-actor-and-touch-packet8-not-climb-not-string|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Spider,mob=type52,night=14000,leap=toward-actor,hurt=packet8,heal=322,cap=9,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`c1acc30fb89383a980963eda9ae54bd6fcc4a2c8eaff785ee3a10b3206e3153c`.
