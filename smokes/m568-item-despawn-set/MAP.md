# M568 behavior map

A raised stone pad plus east walkway seeds cobble `4`. Packet14 status 4
drops that stack so Packet21 item `4` appears. The actor walks east so
Packet22 collection cannot fire. After a clean save, McRegion NBT
advances EntityItem `Age` to `5900`. Reload still emits Packet21, then
the official server emits Packet29 when `Age` reaches `6000`.

This map does not re-qualify M51 spawn-only Packet21, M52 Packet22
named collection, or M436 remaining arrow life. Headless
`B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-pad+east-walk+item4|cause=packet14-drop-4+nbt-age-5900|wire=packet21-4+packet29-no-packet22|oracle=item-despawn-age-6000-not-m52-collect-not-m51-spawn-only-not-m436-arrow|column=17,support=4:71:4:1:0,item=4,age=5900,age-limit=6000,wire=packet21-4+packet29,collect=false,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b90bfdf125255b880fd496ce52fa92b784d5e3879fbf448482c44004bd2574f2`.
