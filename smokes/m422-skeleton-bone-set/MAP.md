<!-- worldline-map-schema=1 -->
<!-- boundary=m422-skeleton-bone-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=131ebd45e9b81d7f65d182b85fef0d213bda1f3d8521b4e4f403d5958aa1f0c0 -->

# M422 behavior map

The fixture raises an isolated `7×7` grass platform, closes its perimeter with
24 fence blocks `85:0`, and places one default mob spawner `52:0`. The client
selects only a skeleton spawned inside that arena, so knockback cannot turn a
bounded combat attempt into an off-platform target wait. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Skeleton`. The personal 2x2 grid mills seeded bone `352` to
bone meal `351x3:15`. Console time `14000` makes the platform dark enough
for `EntityMob.getCanSpawnHere`. Packet24 identity type `51` is killed with
a diamond sword. Packet21 must include bone `352`. Packet15 then applies
the crafted bone meal to wheat `59:0` and observes mature `59:7`. Drop count
and loot-table arrows stay outside the frozen hash.

This map does not claim skeleton arrow `262` (M388), dye-family milling
(M328), wheat growth families (M305), XP, or other hostile types.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-arena+spawner52+wheat59|cause=nbt-entityid-skeleton+time-14000+diamond-sword-packet7+packet102-bone352-to-351x3:15+packet15-bonemeal351:15|wire=packet24-type51+packet38-status3+packet29+packet21-352+packet53-crops59:7|oracle=skeleton-bone-drop-and-bonemeal-craft-use|column=17,platform=7x7-48grass,arena=fence85-24,spawner=4:72:4:52:0,entityid=Skeleton,mob=type51,night=14000,sword=276,drop=packet21-352,craft=351x3:15,wheat=3:72:4:59:0->59:7,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`131ebd45e9b81d7f65d182b85fef0d213bda1f3d8521b4e4f403d5958aa1f0c0`.
