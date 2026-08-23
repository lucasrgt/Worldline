# M458 behavior map

Seed `17320110707` slime chunk `-2,-2` is entered below `y=16`. A default
mob spawner `52:0` is placed in that cave and the saved MobSpawner
`EntityId` is rewritten from `Pig` to `Slime`. The placement path uses four
bounded attempts and accepts only the authoritative remote block state, avoiding
a one-packet placement race without changing the fixture. Official
`spawn-monsters=true` lets EntitySlime `getCanSpawnHere` succeed at `y<16`. Packet24 type `55`
metadata index 16 shows both size-1 and larger slimes. AABB contact with a
larger slime emits Packet38 status 2 then Packet8. The SET is that size
family plus contact damage, not a split and not a slimeball drop.

This map does not claim M412 parent-split children, M423 slimeball `341`,
or the cave geometry as its own freeze. Movement packets stay at or under
9 blocks.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=slime-chunk-y<16-spawner52|cause=nbt-entityid-slime+move-into-type55-aabb|wire=packet24-type55+packet38-status2-before-packet8|oracle=slime-contact-damage-not-split-not-slimeball|chunk=-2:-2,room=-29:13:-27,spawner=-29:13:-27:52:0,entityid=Slime,lowy=true,mobs=type55,size=1+larger,touch=packet8,status=2,hunts<=16,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`84cc0baf6465c46adf5437018728a84b237f8d611fa461bcc6335932432f2d26`.
