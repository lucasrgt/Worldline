# M609 sapling dark halt behavior map

Packet15 of dirt item `3` and oak sapling item `6` builds a raised-stone
dirt pad: seven open sapling `6:0` samples and one sapling cell covered
by stone `1`. Official random ticks then write Packet53 sapling stage
bit `8` or log `17` on at least one lit sample. The covered/dark sapling
cell stays `6:0`. Exact wait length and which lit sample stages are not
hashed.

This map does not claim M339 bonemeal oak/spruce/birch growth, M202
sapling place, M140 single oak bonemeal, giant trees, or leaf decay.

Frozen signal: `column=17,support=4:71:4:1:0,sapling=6:0,lit=7,covered=4:73:6:6:0,cover=4:74:6:1:0,lit-stage>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lit-sapling6+covered-sapling6|cause=packet15-item6+random-ticks|wire=packet53-sapling6-stage+covered-6:0|oracle=lit-sapling-stage+dark-sapling-halt+fresh-login|column=17,support=4:71:4:1:0,sapling=6:0,lit=7,covered=4:73:6:6:0,cover=4:74:6:1:0,lit-stage>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`84a2148a1d8deae33271631d42a13f4a1c9e2727173bbd5f428463d0747134c7`.
