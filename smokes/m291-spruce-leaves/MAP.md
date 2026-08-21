# M291 behavior map

Packet15 places spruce log item `17` damage `1` on the east face of a
raised stone column, then Packet15 places leaves item `18` damage `1` on
the top face. The official server writes spruce leaf `18:9` beside spruce
log `17:1`. That exact leaf cell survives a clean save plus fresh login.

This map is distinct from M209 oak leaves `18:8`. It does not claim
decay-without-log as the hashed success path, birch leaves, or shear
drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+spruce17:1+leaves18:1|cause=packet15-item18:1|wire=packet53-leaves18:9|oracle=live-block18:9+fresh-login|column=17,support=4:71:4:1:0,log=5:71:4:17:1,leaves=4:72:4:18:9,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`665728a20dbb06b11792f4b355f3b52d189d5cf5b8c0d06099db1447b1b7f0d5`.
