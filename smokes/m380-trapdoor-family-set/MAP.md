# M380 behavior map

One official session places trapdoor item `96` against the four side faces
of a raised stone support. South is `96:1`, north `96:0`, east `96:3`, and
west `96:2`. Empty-hand Packet15 opens then closes each cell
(`1->5->1`, `0->4->0`, `3->7->3`, `2->6->2`). The four closed trapdoors
survive a clean save plus fresh login.

This map does not re-qualify the shipping east-only M163 place or the
M306 wooden-door-plus-east-trapdoor close. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+trapdoor96-south-north-east-west|cause=packet15-item96-place-south-north-east-west+empty-hand-packet15-open-then-close|wire=packet53-trapdoor96:1->5->1+96:0->4->0+96:3->7->3+96:2->6->2|oracle=live-four-face-toggle+fresh-login-closed-trapdoors|column=17,support=4:71:4:1:0,south=4:71:5:96:1->5->1,north=4:71:3:96:0->4->0,east=5:71:4:96:3->7->3,west=3:71:4:96:2->6->2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ab78b72d72f7fa3016aff5ef1e7d1fa6d51961bb14c02d74afa5e1a5ecf036e7`.
