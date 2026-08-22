# M552 behavior map

Packet15 places TNT item `46` on a raised stone column, then a solid
stone cell on top of that TNT. Packet15 of lever item `69` attaches to
the east face of the block above. Empty-hand Packet15 powers that
upper stone. The lever is diagonal to the TNT cell, so this is not
adjacent redstone on the TNT and not flint-and-steel. Packet23 type
`50` is the primed-TNT object on the existing tracker. After the
bounded fuse, Packet60 strength `4` destroys the constructed support.
The TNT cell and support remain air after a clean save plus fresh
login.

This is distinct from M219 unprimed place, M137 Packet60 detonate
without Packet23 type `50`, and M381 flint-and-steel prime. Exact blast
rays and destroyed-cell count are not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+tnt46+stone-above+lever69|cause=packet15-item46+packet15-item1-above+packet15-item69-east+empty-hand-packet15-power-above|fuse=100ticks|wire=packet23-type50+packet60-center+strength+relative-destroyed-cells|oracle=live-prime-object+crater-support-air+fresh-login|column=15,support=4:69:4:1:0->0:0,tnt=4:70:4:46:0->0:0,above=4:71:4:1:0,lever=5:71:4:69:1->9,packet23=50,strength=4,crater=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a0ad8d6262175c29d1c7d1dadfcaf90f6a45d1db92c4c7dbbb63983a969b0732`.
