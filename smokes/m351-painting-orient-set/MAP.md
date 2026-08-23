<!-- worldline-map-schema=1 -->
<!-- boundary=m351-painting-orient-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8f60b715dc6a3aeab49aaae89f1f147dd7822ab37806a8da79597e86acd2e9aa -->

# M351 behavior map

Painting item `321` is used through Packet15 on the west face of a raised
2x2 stone wall and on the east face of a second 2x2 stone wall offset
north. Two peers decode the same protocol-14 Packet25 pair at `5:72:4`
direction `1` and `3:72:3` direction `3`. Official art titles vary across
JVMs, so the frozen oracle is shared identity, type, and two distinct
facing values, not the RNG titles.

This map is distinct from M177's single west-face painting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-2x2-stone-walls-west+east|cause=packet15-item321-west+packet15-item321-east|wire=packet25+packet25|oracle=two-peer-identical-painting-spawns-multi-facing|column=17,west-wall=5:72:4-5:73:5:1:0,west=5:72:4:dir1,east-wall=3:72:3-3:73:2:1:0,east=3:72:3:dir3,packet25+packet25,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8f60b715dc6a3aeab49aaae89f1f147dd7822ab37806a8da79597e86acd2e9aa`.
