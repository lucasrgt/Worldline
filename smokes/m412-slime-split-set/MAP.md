# M412 behavior map

Seed `17320110707` slime chunk `-2,-2` is entered below `y=16`. A default
mob spawner `52:0` is placed in that cave and the saved MobSpawner
`EntityId` is rewritten from `Pig` to `Slime`. Official `spawn-monsters=true`
lets EntitySlime `getCanSpawnHere` succeed at `y<16`. A Packet24 type `55`
parent is killed with Packet7 diamond sword `276`. The SET is the parent
Packet38 status 3 plus Packet29 destroy together with at least one child
Packet24 type `55`, not a single spawn. Tiny slimes may drop slimeball
`341`; that drop is not frozen.

This map does not claim M363 zombie/skeleton identities, M388 hostile drops,
XP, or other hostile types.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=slime-chunk-y<16-spawner52|cause=nbt-entityid-slime+diamond-sword-packet7|wire=packet24-type55+packet38-status3+packet29+child-packet24-type55|oracle=slime-parent-death-plus-child-spawns|chunk=-2:-2,room=-29:13:-27,spawner=-29:13:-27:52:0,entityid=Slime,lowy=true,mobs=type55,split=parent-death+child-packet24-type55,sword=276,kills<=8,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`04232de5b9eb6e2e741dbbf008ade42638370d907b361856800b70fe8cb6e59b`.
