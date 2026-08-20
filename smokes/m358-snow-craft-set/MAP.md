# M358 behavior map

One official personal 2x2 epoch crafts snow block `80` from four snowballs
`332`. Packet14 while holding gold shovel `284` then breaks player-placed
snow layer `78` and snow block `80` to Packet21 snowball `332`. The crafted
`80` stack survives a clean save plus fresh login.

This map does not claim M194 snow-block place persistence or M203
snow-layer place persistence.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-snowball332-to-snowblock80+snowlayer78+snowblock80|cause=packet102-window0-4x332-to-80+packet14-goldshovel284|wire=packet106-accepted+packet53-air+packet21-id332|oracle=craft-80+shovel-78-and-80-to-332+fresh-login|column=17,support=4:71:4:1:0,craft=80x1-from-332x4,layer=5:72:4:78:0->0:0,drop=packet21-332:1:0,block=3:72:4:80:0->0:0,drop=packet21-332:1:0,shovel=284,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d35de53474c363b1b580a865ea4bcce9403b8f9092e3ca5be19e9f1bf6e6d1be`.
