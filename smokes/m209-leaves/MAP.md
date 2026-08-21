# M209 behavior map

Packet15 places oak log item `17` on the east face of a raised stone
column, then Packet15 places leaves item `18` on the top face. The
official server writes leaf `18:8` beside log `17:0`. That exact leaf
cell survives a clean save plus fresh login.

This map does not claim decay-without-log as the hashed success path or
shear drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17+leaves18|cause=packet15-item18|wire=packet53-leaves18:8|oracle=live-block18:8+fresh-login|column=17,support=4:71:4:1:0,log=5:71:4:17:0,leaves=4:72:4:18:8,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`aad065fc1b2eee7b0366a5095df49496d2002a0991c39ffefad789bb6896d5bd`.
