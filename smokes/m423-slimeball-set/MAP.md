# M423 behavior map

Seed `17320110707` slime chunk `-2,-2` is entered below `y=16`. A default
mob spawner `52:0` is placed in that cave and the saved MobSpawner
`EntityId` is rewritten from `Pig` to `Slime`. Official `spawn-monsters=true`
lets EntitySlime `getCanSpawnHere` succeed at `y<16`. Packet24 type `55`
identities are killed with Packet7 diamond sword `276` until a size-1 death
emits Packet21 slimeball `341`. The same session Packet102-crafts sticky
piston `29` from piston `33` plus slimeball `341` on workbench `58`. The SET
is that drop plus that craft, distinct from M412 parent-split whose drop
stayed outside the hash.

This map does not claim XP, loot-table counts, M371 machine-block crafts, or
other hostile types.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=slime-chunk-y<16-spawner52+workbench58+piston33+slimeball341|cause=nbt-entityid-slime+diamond-sword-packet7+packet102-33+341|wire=packet24-type55+packet38-status3+packet29+packet21-341+packet106-sticky29|oracle=size-1-slimeball-drop-and-sticky-piston-craft|chunk=-2:-2,room=-29:13:-27,spawner=-29:13:-27:52:0,entityid=Slime,lowy=true,mobs=type55,size=1,drop=packet21-341,craft=sticky29-from-33+341,workbench=-30:13:-27:58:0,sword=276,kills<=12,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`8a525200f72521e3f129de58b27232e197c39cf2d41da689d2772e9a830ac411`.
