# M164 behavior map

Stone pressure plate item `70` is placed on a raised stone support as block
`70:0`. The headless actor then `moveAndObserve`s onto that cell so the official
server powers the plate to `70:1` through Packet53. Stepping off depowers it
back to `70:0`. A clean save plus fresh login rereads unpowered `70:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+plate70|cause=packet15-item70+moveAndObserve-on-cell|wire=packet53-plate70:0->1->0|oracle=live-power+depower+fresh-login-unpowered|column=17,support=4:71:4:1:0,plate=4:72:4:70:0->1->0,persisted=70:0,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ab14f3bebb0157e814af07dd4950065b472c5d5b99f25736c02b57fd08b1f754`.
