# M220 behavior map

Packet15 places workbench item `58` on a raised stone column. The official
server writes workbench `58:0`. That exact cell survives a clean save plus
fresh login.

This map does not open Packet100 crafting. M62 already covers the workbench
window.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58|cause=packet15-item58|wire=packet53-workbench58:0|oracle=live-block58:0+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`75f1f8dccff9989eba1c5ac186c1f62a8054452d6d6ebe7c59e09de2742a37ed`.
