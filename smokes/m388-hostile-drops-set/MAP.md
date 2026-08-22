# M388 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Zombie` and `Skeleton`. Console time `14000` makes
the platform dark enough for `EntityMob.getCanSpawnHere`. Packet24 identities
type `54` and type `51` are killed with a diamond sword. Packet21 must include
feather `288` and arrow `262`. The actor equips the official leather armor
family `298-301` before combat so the drop oracle cannot be lost to an
unrelated player death. Drop count stays outside the frozen hash.

This map does not claim pig identity (M141), identity-only hostiles (M363),
XP, or other hostile types.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52+leather-armor298-301|cause=nbt-entityid-zombie+skeleton+time-14000+personal-window-equip+diamond-sword-packet7|wire=packet24-type54+packet24-type51+packet106-armor-equip+packet38-status3+packet29+packet21-288+packet21-262|oracle=hostile-zombie-feather-and-skeleton-arrow-drops|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Zombie+Skeleton,mobs=type54+type51,night=14000,sword=276,survival=leather298-301,drops=packet21-288+packet21-262,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`2e16ed6ddaaf65b63a82ad4de3734ab19f25ce46bb46154ef8ecd10ce680415c`.
