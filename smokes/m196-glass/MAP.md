# M196 behavior map

Packet15 places glass item `20` on a raised stone column. The official
server writes glass `20:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim glass pane, stained glass, or silk-touch.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+glass20|cause=packet15-item20|wire=packet53-glass20:0|oracle=live-block20:0+fresh-login|column=17,support=4:71:4:1:0,glass=4:72:4:20:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`559e9d38c638c27b461ef834cc558063696964fbdff90e68a729877cb3daa13e`.
