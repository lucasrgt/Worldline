# M409 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Spider`. Console time `14000` makes the platform dark enough
for `EntityMob.getCanSpawnHere`. Packet24 identity type `52` is killed with
a diamond sword. Packet21 must include string `287`. Packet15 then places
cobweb `30:0` on the platform. Drop count and cobweb harvest stay outside
the frozen hash.

This map does not claim zombie or skeleton loot (M388), pig identity (M141),
cobweb persistence (M195), cobweb-to-string harvest, XP, or other hostile
types.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52+cobweb30|cause=nbt-entityid-spider+time-14000+diamond-sword-packet7+packet15-item30|wire=packet24-type52+packet38-status3+packet29+packet21-287+packet53-cobweb30|oracle=spider-string-drop-and-cobweb-place|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Spider,mob=type52,night=14000,sword=276,drop=packet21-287,cobweb=3:72:4:30:0,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`625db8d839633b99daf0e73b098ad644d6f23e3d9ed4dda4f187687c1fe26fc4`.
