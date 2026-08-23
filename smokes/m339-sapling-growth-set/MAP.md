<!-- worldline-map-schema=1 -->
<!-- boundary=m339-sapling-growth-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=cbb09ab44fa0804f8304e414f683a868c16aabac0c29c00ba78b525e6678ec5e -->

# M339 behavior map

A raised dirt fixture plants oak sapling `6:0`, spruce sapling `6:1`, and
birch sapling `6:2` on isolated dirt pads five blocks apart. Packet15 of
bonemeal `351:15` grows each root into the matching log damage `17:0`,
`17:1`, and `17:2`. Official sapling stage bit `8` is not hashed; species
is `metadata & 3`. If one fertilize attempt fails, the sapling is restored
and the smoke retries, then waits a bounded random-tick window. A fresh
login rereads all three roots.

Exact tree geometry, extra height, and wait length are not hashed. The
frozen oracle is the three sapling-to-log damage pairs after a clean save.

This map is distinct from M140 single oak bonemeal and from M305 wheat,
cactus, and sugar cane. It does not claim giant trees, leaf decay, or
harvest.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+sapling6:0+6:1+6:2|cause=packet15-bonemeal351:15|wire=packet53-log17:0+17:1+17:2|oracle=bonemeal-oak-spruce-birch-root-logs+fresh-login|column=17,oak=4:73:4:6:0->17:0,spruce=9:73:4:6:1->17:1,birch=4:73:9:6:2->17:2,bonemeal=351:15,saplings=6:0+6:1+6:2,logs=17:0+17:1+17:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`cbb09ab44fa0804f8304e414f683a868c16aabac0c29c00ba78b525e6678ec5e`.
