# M554 behavior map

The cloned M367 west-facing piston arm occupies one raised stone column.
Normal piston `33:4` sits on the support at `(4,65,4)` with stone `1:0` in
front and a side lever `69:1`. Empty-hand Packet15 extends piston `33`
(`33:4 -> 33:12`, head `34:4`, displaced stone). Packet14 while holding
iron pickaxe `257` then fully breaks the extended BASE. Official leftover
cleanup removes head `34` to air and drops Packet21 piston `33:1:0`. The
base cell is air. The lever stays powered `69:9`. Fresh login Packet51
keeps those leftover cells.

This map is distinct from M367 retract-by-unpower (`33:12 -> 33:4` with
the base retained) and from M293/M294 place-only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west-extended|settle=200+20ticks|cause=packet15-lever-activate+packet14-ironpick257-base|effect=official-extended-piston-base-break+head34-removed|observation=fresh-login-packet51|column=10,extend=33:4->12,head-break=33:12->0,piston=4:65:4:33:4->12->0,head=3:65:4:1:0->34:4->0:0,pushed=2:65:4:0:0->1:0->1:0,lever=5:64:4:69:1->9,drops=packet21-33,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.
