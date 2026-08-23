<!-- worldline-map-schema=1 -->
<!-- boundary=m290-birch-sapling -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=21f35395f38d2877297a2801023c0e7e0e0b5fc83a8ec278dee1ad7b7151b8a0 -->

# M290 behavior map

Packet15 plants sapling item `6` damage `2` on dirt `3` capping the M175
raised stone column. The official server writes birch sapling `6:2`,
distinct from oak `6:0` and spruce `6:1`. That exact cell survives a
clean save plus fresh login.

This map does not wait for tree growth and does not claim bone meal.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+sapling6:2|cause=packet15-item6:2|wire=packet53-sapling6:2|oracle=live-block6:2+fresh-login|column=17,dirt=4:72:4:3:0,sapling=4:73:4:6:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`21f35395f38d2877297a2801023c0e7e0e0b5fc83a8ec278dee1ad7b7151b8a0`.
