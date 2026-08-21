# M289 behavior map

Packet15 places sapling item `6` damage `1` on dirt `3` capping the M175
raised stone column. The official server writes spruce sapling `6:1`.
That exact cell survives a clean save plus fresh login.

This map is distinct from M202 oak sapling `6:0`. It does not wait for
tree growth and does not claim birch `6:2` or bone meal.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+sapling6:1|cause=packet15-item6:1|wire=packet53-sapling6:1|oracle=live-block6:1+fresh-login|column=17,dirt=4:72:4:3:0,sapling=4:73:4:6:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`338c07cf0cc26fad4d048f900242741d71662eac8c8f48d98d41ede8c541dc2c`.
