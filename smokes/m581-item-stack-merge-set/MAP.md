# M581 behavior map

Two identical stone stacks are Q-dropped from hotbar slots 0 and 1 while the
actor looks down. Official Packet14 status 4 creates two Packet21 entities
`1x1:0` in contact. A 30-tick observation window stays inside the 40-tick
pickup delay, so collection cannot explain removal.

The live Packet21 count stays 2. Packet29 absorb is absent, collector Packet22
is absent, and neither spawn count increases. Official `EntityItem.onUpdate`
has no combine path in b1.7.3; nearby identical drops remain separate stacks.

This map does not claim M517 age-6000 despawn, M50 held-slot emptying, M51
spawn kinematics, or M52 named collection.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=two-stone-1x1-look-down|cause=packet14-status4-twice|wire=packet21x2-count1|oracle=item-stack-merge-absent-not-despawn-age|drops=2,item=1x1:0,live=2,destroyed=0,collected=0,merged=false,contact=30,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`e4e993fb7359eaf26b59c61d904faefeef4a3fa3e5193f800d5a8538015a22fc`.
