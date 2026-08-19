# M222 behavior map

Packet15 places cobblestone item `4` on a raised stone column. The official
server writes cobblestone `4:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim mossy cobblestone `48` or cobble stairs.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobble4|cause=packet15-item4|wire=packet53-cobble4:0|oracle=live-block4:0+fresh-login|column=17,support=4:71:4:1:0,cobble=4:72:4:4:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b85fbf8097da42d8a630be610e6ab4457bd0de302e1f3396daff2d3fd161ac20`.
