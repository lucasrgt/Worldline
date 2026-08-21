# M217 behavior map

Packet15 places mossy cobblestone item `48` on a raised stone column. The official
server writes mossy cobble `48:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim regular cobblestone `4` or dungeon generation.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+mossy48|cause=packet15-item48|wire=packet53-mossy48:0|oracle=live-block48:0+fresh-login|column=17,support=4:71:4:1:0,mossy=4:72:4:48:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e7a7a8e7b99473c5e1e123d0d2542636867e0e5ebae855883295e487ee937a43`.
