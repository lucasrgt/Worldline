# M447 behavior map

The fixture raises an isolated `7×7` grass platform, closes its perimeter with
24 fence blocks `85:0`, Packet15-places a tall cobble `4` wall and a tall oak
plank `5` wall one cell from center, and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Spider`. Console time `14000` makes the platform dark enough
for spider pursuit. Only arena-contained Packet24 type `52` identities are
armed. A smoke-only non-blocking adapter read polls fixed five-tick windows and
must observe Packet31/33/34 positive-Y motion adjacent
to cobble `4` and again adjacent to oak plank `5` in one session.

This map does not claim spider string `287` (M409), spider leap/touch
(M457), or natural Packet24 identity without a spawner (M435). Headless
`B173WireClient` only.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-arena+cobble4-wall+oak-plank5-wall+spawner52|cause=nbt-entityid-spider+time-14000+bounded-mob-movement-poll|wire=packet24-type52+packet31-or33-or34-positive-y|oracle=spider-climb-cobble4-and-planks5-not-m409-string|column=17,platform=7x7-48grass,arena=fence85-24,spawner=4:72:4:52:0,entityid=Spider,mob=type52,night=14000,cobble=5:79:4:4:0,planks=3:79:4:5:0,climb=packet31|33|34+positive-y+cobble4+planks5,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`dd9ff5fff66e6f0e70d4f4dd873aa178184ec6e7b407e99cc861cec68916f588`.
