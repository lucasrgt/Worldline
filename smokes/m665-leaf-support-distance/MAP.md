<!-- worldline-map-schema=1 -->
<!-- boundary=leaf-decay -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8dec76efd445287c28769682efe28a4f16e065e688f0dc27a93654ac5022120a -->

# M665 leaf support distance behavior map

The official fixture starts from a raised stone pad and rejects nearby
natural logs or leaves before adding test blocks. One oak log `17:0` is
placed at the origin. An isolated player-placed oak leaf `18:8` is placed
four blocks east of the log, while a second leaf is placed five blocks
south. The leaves are five blocks apart, so neither can provide a leaf-chain
shortcut to the other.

The same bounded random-tick window observes the distance-four oak leaf
survive and the distance-five leaf become air `0:0`. The supported leaf may
clear its transient decay-check bit from `18:8` to `18:0`; the stable oracle
is the surviving oak leaf identity. The log and both stone support cells
remain unchanged. A clean save and fresh Packet51 login repeat the
near-leaf and far-leaf observations.

This map refines M385's all-support-removed decay oracle. It does not claim
leaf-chain propagation, species differences, shear drops, sapling rates, or
an emulated decay algorithm. Headless `B173WireClient` only. No GUI. No Aero.

Frozen signal:

```text
column=17,log=17:0,near=18:8@distance4->leaf,far=18:8@distance5->0:0,support-radius=4,persisted=true,clients=2,disconnect=clean
```

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17+isolated-leaves18-axis-distance4+distance5|cause=packet15-item17+packet15-item18+random-ticks|wire=packet53-leaves18:8->0:0+packet51-leaves18:8+packet51-log17:0|oracle=official-leaf-support-radius4+fresh-login|column=17,log=17:0,near=18:8@distance4->leaf,far=18:8@distance5->0:0,support-radius=4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8dec76efd445287c28769682efe28a4f16e065e688f0dc27a93654ac5022120a`.
