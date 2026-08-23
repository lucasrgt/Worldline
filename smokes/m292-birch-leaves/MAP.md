<!-- worldline-map-schema=1 -->
<!-- boundary=m292-birch-leaves -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=909703a5406842a4c1becff13064c13eebc661300e0ebe15b3400c822771f912 -->

# M292 behavior map

Packet15 places oak log item `17` on the east face of a raised stone
column, then Packet15 places birch leaves item `18` damage `2` on the
top face. The official server writes leaf `18:10` beside log `17:0`.
That exact birch leaf cell survives a clean save plus fresh login.

This map is distinct from oak `18:8` and spruce `18:9`. It does not
claim decay-without-log as the hashed success path or shear drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17+leaves18:2|cause=packet15-item18:2|wire=packet53-leaves18:10|oracle=live-block18:10+fresh-login|column=17,support=4:71:4:1:0,log=5:71:4:17:0,leaves=4:72:4:18:10,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`909703a5406842a4c1becff13064c13eebc661300e0ebe15b3400c822771f912`.
