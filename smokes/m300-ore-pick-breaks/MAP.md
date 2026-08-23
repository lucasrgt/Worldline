<!-- worldline-map-schema=1 -->
<!-- boundary=m300-ore-pick-breaks -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=5fa840f6542410b38170ae4dc2fe1d2731c8d7ca7335ba9d105d3c1feed61b1a -->

# M300 behavior map

Packet15 places cobble item `4` on the top face of the raised stone
column, coal ore item `16` on the east face, and diamond ore item `56`
on top of that coal ore. Packet16 holds iron pickaxe `257` for cobble
and coal ore, then diamond pickaxe `278` for diamond ore. Packet14 fully
breaks each cell to air and emits Packet21 stacks `4:1:0`, `263:1:0`,
and `264:1:0`.

This map is distinct from M222 cobble place, M225 coal-ore place, and
M228 diamond-ore place. It reuses the existing Packet21 tracker and does
not claim pickaxe durability, bare-hand rejection, fortune, or other
ores.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobble4+ore16+ore56|cause=packet14-ironpick257+diamondpick278|wire=packet53-air+packet21-id4+263+264|oracle=ore-pick-breaks-family|column=17,support=4:71:4:1:0,cobble=4:72:4:4:0->0:0,coal=5:71:4:16:0->0:0,ore=5:72:4:56:0->0:0,picks=257+278,drops=packet21-4+263+264,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`5fa840f6542410b38170ae4dc2fe1d2731c8d7ca7335ba9d105d3c1feed61b1a`.
