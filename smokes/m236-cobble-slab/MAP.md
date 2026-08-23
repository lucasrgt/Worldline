<!-- worldline-map-schema=1 -->
<!-- boundary=m236-cobble-slab -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0659df1d047139297efabd6988985a6f3c1bb9b16e6dfa93f6bc1387d8dbc335 -->

# M236 behavior map

Packet15 places cobble slab item `44` with damage `3` on a raised stone
column. The official server writes single slab `44:3`. That exact cell
survives a clean save plus fresh login.

This map does not re-qualify stone slab `44:0` (M188).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab44:3|cause=packet15-item44-damage3|wire=packet53-slab44:3|oracle=cobble-metadata+fresh-login|column=17,support=4:71:4:1:0,slab=4:72:4:44:3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0659df1d047139297efabd6988985a6f3c1bb9b16e6dfa93f6bc1387d8dbc335`.
