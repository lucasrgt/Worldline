<!-- worldline-map-schema=1 -->
<!-- boundary=m375-remaining-pick-breaks -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=22503c04e191d5edd6c2374799f5062269ff1e38d71c15709e468a2d2e787869 -->

# M375 behavior map

Packet15 places mossy cobble item `48` on the top face of the raised
stone column, gold ore item `14` on the east face, and obsidian item
`49` on top of that gold ore. Packet16 holds gold pickaxe `285` for
mossy cobble, then diamond pickaxe `278` for gold ore and obsidian.
Packet14 fully breaks each cell to air and emits Packet21 stacks
`48:1:0`, `14:1:0`, and `49:1:0`.

This map is distinct from M300 cobble/coal/diamond-ore iron-pick family,
M216 obsidian place, M217 mossy-cobble place, and M227 gold-ore place.
It reuses the existing Packet21 tracker and does not claim pickaxe
durability, bare-hand rejection, or other remaining ores.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+mossy48+ore14+obsidian49|cause=packet14-goldpick285+diamondpick278|wire=packet53-air+packet21-id48+14+49|oracle=remaining-pick-breaks-family|column=17,support=4:71:4:1:0,mossy=4:72:4:48:0->0:0,gold=5:71:4:14:0->0:0,obsidian=5:72:4:49:0->0:0,picks=285+278,drops=packet21-48+14+49,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`22503c04e191d5edd6c2374799f5062269ff1e38d71c15709e468a2d2e787869`.
