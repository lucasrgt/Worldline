# M394 behavior map

One official session places remaining single slabs `44:1`, `44:2`, and
`44:3` plus double slab `43:0` on a raised four-pad stone fixture. Packet15
of sandstone slab item `44` damage `1` writes `44:1` on the center support.
Wood slab damage `2` writes `44:2` on the east pad, cobble slab damage `3`
writes `44:3` on the west pad, and double-slab item `43` writes `43:0` on
the south pad. All four cells survive a clean save plus fresh login.

This map does not re-qualify stone slab `44:0` (M188) or the M336
workbench crafts of `44:1`/`44:2`/`44:3`. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab44:1+slab44:2+slab44:3+slab43:0|cause=packet15-item44:1+item44:2+item44:3+item43:0|wire=packet53-slab44:1+44:2+44:3+43:0|oracle=remaining-slab-place-metadata+fresh-login|column=17,support=4:71:4:1:0,sandstone=4:72:4:44:1,wood=5:72:4:44:2,cobble=3:72:4:44:3,double=4:72:5:43:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7939076b43b10ef5972487f306388abb58dafba8b1ca28923a3fb952ef2c6a9f`.
