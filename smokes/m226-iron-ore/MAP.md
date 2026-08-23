<!-- worldline-map-schema=1 -->
<!-- boundary=m226-iron-ore -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a0e1b3a34325710f72942c88c73bd15a8cad197fb7b04c8bbfc5f867b97389b7 -->

# M226 behavior map

Packet15 places iron ore item `15` on a raised stone column. The official
server writes iron ore `15:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim ore generation, drops, or smelting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ironore15|cause=packet15-item15|wire=packet53-ironore15:0|oracle=live-block15:0+fresh-login|column=17,support=4:71:4:1:0,ironore=4:72:4:15:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a0e1b3a34325710f72942c88c73bd15a8cad197fb7b04c8bbfc5f867b97389b7`.
