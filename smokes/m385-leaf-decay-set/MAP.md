<!-- worldline-map-schema=1 -->
<!-- boundary=m385-leaf-decay-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3974fe1e9ab8e39e20e8122dce05d183745ba923b5b1dd4306f63c308e0f2e1c -->

# M385 behavior map

The official fixture raises stone `1:0` out of water, then places oak log
`17:0` north of the pad, spruce log `17:1` on an east span, and birch log
`17:2` on a south span. Packet15 of leaves items `18:0`, `18:1`, and
`18:2` writes adjacent leaves `18:8`, `18:9`, and `18:10`. Packet14 then
removes each log. A bounded official random-tick wait must expose air at
all three leaf cells. After clean disconnect/save, a fresh Packet51 must
expose the same air cells.

This is distinct from M209, M291, and M292 place-only leaves, which keep
nearby wood so the decay-check bit persists. Headless `B173WireClient`
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17:0+spruce17:1+birch17:2+leaves18:0+18:1+18:2|cause=packet14-remove-log-support|wire=packet53-leaves18:8+18:9+18:10->0:0|oracle=bounded-tick-oak-spruce-birch-leaf-decay+fresh-login|column=17,oakLog=4:71:3:17:0->0:0,oakLeaves=4:72:3:18:8->0:0,spruceLog=11:71:4:17:1->0:0,spruceLeaves=11:72:4:18:9->0:0,birchLog=5:71:10:17:2->0:0,birchLeaves=5:72:10:18:10->0:0,items=18:0+18:1+18:2,persisted=true,clients=2,disconnect=clean
```

SHA-256: `3974fe1e9ab8e39e20e8122dce05d183745ba923b5b1dd4306f63c308e0f2e1c`.

Packet14 is request evidence; Packet53 air after the bounded wait is the
decay boundary. Fresh-login Packet51 is the persistence oracle.
