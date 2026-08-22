# M549 behavior map

The cloned M367 west-facing sticky arm occupies one raised stone column.
Sticky piston `29:4` sits on the support at `(4,65,4)` with stone payload
in front. A lever-powered stone stands diagonal-above at `(5,66,4)`;
north-face lever `69:4` is not adjacent to the piston cell, so there is
no continuous direct power (`direct-power=false`).

Official BlockPistonBase therefore stays retracted: BUD primed `29:4`.
Packet15 of stone onto the north face is the neighbor update. That
update BUD-extends `29:4 -> 29:12` with sticky head `34:12` and
displaced stone. Empty-hand Packet15 then unpowers the diagonal lever
(`69:12 -> 69:4`); the sticky pulse pulls (`29:12 -> 29:4`, head
`34:12 -> 1:0`). Fresh login Packet51 keeps the pulled arm.

This map is distinct from M547 sticky QC (lever on the block directly
above, immediate extend), M548 regular piston-`33` BUD, M367
lever-on-support motion, and M293 place-only `29:1`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=sticky29-west+diagonal-above-lever69-qc+payload|settle=200+10ticks|cause=packet15-neighbor-stone-north|effect=official-sticky29-bud-extend+head34:12+sticky-pull|observation=fresh-login-packet51|column=10,primed=29:4,bud-extend=29:4->12,sticky-pull=29:12->4,piston=4:65:4:29:4->12->4,head=3:65:4:1:0->34:12->1:0,pushed=2:65:4:0:0->1:0->0:0,powered=5:66:4:1:0,lever=5:66:3:69:4->12->4,update=4:65:3:0:0->1:0,direct-power=false,continuous-power=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d9de32a7e37b272dd97be1d211464f0bf67b7b66ba71ddedbf3742d0f345747b`.
