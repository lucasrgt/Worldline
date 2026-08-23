<!-- worldline-map-schema=1 -->
<!-- boundary=m216-obsidian -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=37b0c135b01ef8c65cbbb0636a135f26610fb0825a3acf378e396d5150a32bce -->

# M216 behavior map

Packet15 places obsidian item `49` on a raised stone column. The official
server writes obsidian `49:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim water-lava reaction (M139) or piston rejection
(M146).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+obsidian49|cause=packet15-item49|wire=packet53-obsidian49:0|oracle=live-block49:0+fresh-login|column=17,support=4:71:4:1:0,obsidian=4:72:4:49:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`37b0c135b01ef8c65cbbb0636a135f26610fb0825a3acf378e396d5150a32bce`.
