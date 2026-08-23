<!-- worldline-map-schema=1 -->
<!-- boundary=m305-plant-growth -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b755666b909da0bc4583bf752a32ff032894d3959b4dd0a47c56d3e80c066721 -->

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
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9+sand12|cause=packet15-bonemeal351:15+item338-reed+item81-cactus|wire=packet53-crops59:7+reed83+cactus81|oracle=bonemeal-wheat-age-jump+official-random-tick-height>=2+fresh-login|column=17,wheat=4:73:3:59:0->59:7,farm=4:72:3:60,cane=4:73:4:83,cane-height>=2,cactus=2:73:4:81,cactus-height>=2,bonemeal=351:15,plants=59+81+83,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b755666b909da0bc4583bf752a32ff032894d3959b4dd0a47c56d3e80c066721`.
