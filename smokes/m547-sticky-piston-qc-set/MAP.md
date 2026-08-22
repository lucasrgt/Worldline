# M547 behavior map

The cloned M367 west-facing sticky arm occupies the raised stone column.
Sticky piston `29:4` sits on the support at `(4,65,4)` with stone `1:0` in
front. Quasi-connectivity uses stone `1:0` on top of the piston at
`(4,66,4)` and a side lever `69:1` on that above-block, not on the piston
cell and not on the M144/M367 support.

Empty-hand Packet15 extends sticky piston `29` through QC (`29:4 -> 29:12`,
head `34:12`, displaced stone). A second Packet15 retracts with an official
sticky pull (`34:12 -> 1:0`, destination air). Fresh login Packet51 keeps
the pulled arm.

This map is distinct from M546 regular piston-`33` QC, from M367 dual-arm
motion (`33` extend/retract plus sticky pull from the support lever), and
from shipping M144 1:1 support-lever pull.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=sticky29-west+qc-stone-above|settle=200+10ticks|cause=packet15-lever-activate-on-block-above-piston|effect=official-sticky29-qc-extend+pull|observation=fresh-login-packet51|column=10,qc-extend=29:4->12,qc-pull=29:12->4,piston=4:65:4:29:4->12->4,head=3:65:4:1:0->34:12->1:0,pushed=2:65:4:0:0->1:0->0:0,qc=4:66:4:1:0,lever=5:66:4:69:1->9->1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.
