<!-- worldline-map-schema=1 -->
<!-- boundary=m301-axe-log-breaks -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6e62367a3c72d64d2bda9180cb0e5b0484671ef7530e74968d511330d7a06365 -->

# M301 behavior map

Packet15 places oak `17:0`, spruce `17:1`, and birch `17:2` on a raised
stone column. Packet16 selects stone axe item `275`. Packet14 then fully
breaks each log to air. The official server emits Packet21 stacks
`17:1:0`, `17:1:1`, and `17:1:2`. Vanilla stone axe on wood drops the
matching log item, not planks.

This map compounds M208/M246/M247 place-only logs with the M322 stone-axe
oak harvest. It does not claim axe durability math.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17:0+spruce17:1+birch17:2|cause=packet14-stoneaxe275|wire=packet53-air+packet21-id17|oracle=stone-axe-log-drops-17:0-17:1-17:2|column=17,support=4:71:4:1:0,oak=4:72:4:17:0->0:0,spruce=4:72:4:17:1->0:0,birch=4:72:4:17:2->0:0,axe=275,drop=packet21-17:1:0+packet21-17:1:1+packet21-17:1:2,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`6e62367a3c72d64d2bda9180cb0e5b0484671ef7530e74968d511330d7a06365`.
