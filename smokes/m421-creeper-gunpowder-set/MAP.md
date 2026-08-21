# M421 behavior map

The fixture raises an isolated `7×7` grass platform, Packet15-places
workbench `58:0`, and places one default mob spawner `52:0`. After a clean
save the region NBT `EntityId` is rewritten from `Pig` to `Creeper`. Console
time `14000` makes the platform dark enough for `EntityMob.getCanSpawnHere`.
Packet24 identity type `50` is killed with a diamond sword from outside the
vanilla 3-block proximity fuse. Packet21 must include gunpowder `289`. The
same session opens workbench `58` and Packet102-crafts TNT `46` from five
gunpowder `289` plus four sand `12`. TNT is kept in inventory and is not
placed. Drop count and leftover gunpowder stay outside the frozen hash.

This map does not claim creeper Packet60 strength `3` (M391), TNT place or
flint-and-steel prime (M417 / M381 / M219), charged creepers, XP, or other
hostile types.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52+workbench58+sand12x4+gunpowder289x5|cause=nbt-entityid-creeper+time-14000+diamond-sword-packet7+packet102-workbench-tnt46|wire=packet24-type50+packet38-status3+packet29+packet21-289+packet106-accepted+packet200-craft-stat|oracle=creeper-gunpowder-drop-and-tnt-craft-not-explode3-not-place46|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,workbench=-3:72:4:58:0,entityid=Creeper,mob=type50,night=14000,sword=276,drop=packet21-289,craft=46-from-289+12,kills<=8,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f01c7a65ddde0ddb0cd8f27f6e1c76e896f866c0bf9cc6f8af973bd1def648dc`.
