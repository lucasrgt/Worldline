<!-- worldline-map-schema=1 -->
<!-- boundary=m305-plant-growth -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b5680a226269db829fd6689a395cc9f1f7930e9f253ad215acfc08a7f8955803 -->

# M305 behavior map

A raised dirt plot is hoed and sown as wheat `59:0`. Bonemeal `351:15`
Packet15 jumps that crop to `59:7`. Isolated cactus `81` on sand and sugar
cane `83` beside still water then grow by official random ticks. The frozen
oracle is the wheat age jump plus categorical height `>= 2` for cactus and
cane after a bounded wait, plus the same cells after a clean save and fresh
login. Exact wait length and extra height are not hashed.

This map does not claim harvest or bone meal on cactus or cane.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9+sand12|cause=packet15-bonemeal351:15+planted-random-wheat+item338-reed+item81-cactus|wire=packet53-crops59:7+random-crops59:metadata>0+reed83+cactus81|oracle=bonemeal-wheat-age-jump+official-random-tick-growth+fresh-login|column=17,wheat=4:73:3:59:0->59:7,farm=4:72:3:60,wheat-random=59:0->metadata>0,cane=4:73:4:83,cane-height>=2,cactus=2:73:4:81,cactus-height>=2,bonemeal=351:15,plants=59+81+83,persisted=true,testkit=ae3d35796f441b79a64f2f424a6796597e85a53287d3f17f5cbf9b1b28a3c93f,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b5680a226269db829fd6689a395cc9f1f7930e9f253ad215acfc08a7f8955803`.
