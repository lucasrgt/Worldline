# M445 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Skeleton`. Console time `14000` makes the platform dark enough
for `EntityMob.getCanSpawnHere`. Packet24 identity type `51` then fires two
Packet23 type `60` arrows whose `throwerId` is that skeleton entity.

This map does not claim player bow `261` (M157/M332/M436), skeleton bone
`352` (M422), loot-table arrows `262`, or arrow hit damage (M462).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52|cause=nbt-entityid-skeleton+time-14000+skeleton-los|wire=packet24-type51+packet23-type60-thrower-skeleton+packet23-type60-thrower-skeleton|oracle=skeleton-ranged-ai-two-arrows|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Skeleton,mob=type51,night=14000,arrows=2,arrow=type60,thrower=skeleton,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`59d850eaeeb297f3879633c70a546d1aa4da2de0618852cb9f3e802a8ec6533b`.
