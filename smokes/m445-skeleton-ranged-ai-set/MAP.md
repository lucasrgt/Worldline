# M445 behavior map

The fixture raises an isolated `7×7` grass platform, closes its perimeter with
24 fence blocks `85:0`, and places one default mob spawner `52:0`. The client
selects only an arena-contained skeleton and wears official-format full diamond
armor while observing the bounded ranged window. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Skeleton`. Console time `14000` makes the platform dark enough
for `EntityMob.getCanSpawnHere`. Packet24 identity type `51` then fires two
Packet23 type `60` arrows whose `throwerId` is that skeleton entity.

This map does not claim player bow `261` (M157/M332/M436), skeleton bone
`352` (M422), loot-table arrows `262`, or arrow hit damage (M462).

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-arena+spawner52|cause=nbt-entityid-skeleton+time-14000+skeleton-los|safety=player-nbt-diamond-armor310+311+312+313|wire=packet24-type51+packet23-type60-thrower-skeleton+packet23-type60-thrower-skeleton|oracle=skeleton-ranged-ai-two-arrows|column=17,platform=7x7-48grass,arena=fence85-24,spawner=4:72:4:52:0,entityid=Skeleton,mob=type51,night=14000,arrows=2,arrow=type60,thrower=skeleton,armor=diamond310+311+312+313,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`c397640bf9dddee3c3b93081c4816f82f93289ba759f499d2865fad69fb5d888`.
