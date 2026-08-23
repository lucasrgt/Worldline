# M582 behavior map

Painting item `321` hangs on the cloned M177 raised 2x2 stone wall through
Packet15 on the west face. Packet14 with iron pickaxe `257` then breaks the
attached support cell at the Packet25 origin. Both peers decode Packet29
destroy of that painting entity and Packet21 drop of item `321`.

This map does not claim Packet25 spawn identity (M177), opposed-face
orientation (M351), or remaining motive sizes 4x2/4x3/4x4 (M430).
Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-2x2-stone-wall|cause=packet15-item321-west+packet14-ironpick257-support|wire=packet25+packet29+packet21-321|oracle=support-break-destroys-painting-not-spawn-not-orient-not-motives|column=17,wall=5:72:4-5:73:5:1:0,support=5:72:4:1:0->0:0,painting=5:72:4:dir1,packet25+packet29+packet21-321,shared-id,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`10031f7cd4b860e20a01c8677180286b98d788c6ab5ce7d754879b484a81000d`.
