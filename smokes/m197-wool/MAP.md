# M197 behavior map

Packet15 places white wool item `35` damage `0` on a raised stone column.
The official server writes wool `35:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:0|cause=packet15-item35:0|wire=packet53-wool35:0|oracle=live-block35:0+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b6f11e7750a8b8ece43e987d07cc4862caf7e0e44f636b6ffb32f68b0601e8f6`.
