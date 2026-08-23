<!-- worldline-map-schema=1 -->
<!-- boundary=m232-chest-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=aede6e11abbf46c3049a99931cbbaa22b00fa2ee80c21bca48aa57796ee2d1b9 -->

# M232 behavior map

Packet15 places chest item `54` on a raised stone column. The official
server writes chest `54:0`. That exact cell survives a clean save plus
fresh login. This map does not open Packet100.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+chest54|cause=packet15-item54|wire=packet53-chest54:0|oracle=place-metadata+fresh-login|column=17,support=4:71:4:1:0,chest=4:72:4:54:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`aede6e11abbf46c3049a99931cbbaa22b00fa2ee80c21bca48aa57796ee2d1b9`.
