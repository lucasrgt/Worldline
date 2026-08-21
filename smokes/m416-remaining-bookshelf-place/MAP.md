# M416 behavior map

One official workbench epoch crafts bookshelf `47` twice from six oak planks
`5` plus three books `340`, then Packet15 places both crafted `47` cells.
Gold axe `286` Packet14 harvests one cell to air. Official Beta 1.7.3
bookshelves drop nothing, so Packet21 book `340` is absent. The unharvested
`47:0` cell survives a clean save plus fresh login.

This map does not re-qualify M189's one-cell Packet15 of seeded bookshelf
`47`, and it does not re-qualify M329's isolated workbench crafts of fence
`85`, ladder `65`, and a single unplaced `47`. Headless `B173WireClient`
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+planks5x6+book340x3+two-47|cause=packet102-craft-47+packet15-item47+packet14-goldaxe286|wire=packet53-47-to-air+no-packet21-id340|oracle=craft-47+two-places+no-drop-340+fresh-login-not-m189|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,craft=47-from-5x6+340x3,places=5:72:4:47:0+3:72:4:47:0,harvest=5:72:4:47:0->0:0,drop=no-packet21-340,axe=286,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`63f78903270a88d3a7b5dafcb9aae55b9ffdacf7d785ff4b0f3d7616a975cc64`.
