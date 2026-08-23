# M608 farmland dry set behavior map

Packet15 of wooden hoe `290` only tills isolated dirt `3` into dry
farmland `60:0` as fixture setup on a raised stone column. A stone rain
roof two cells above the plot blocks sky hydration. No water is placed
nearby, and the actor stays off the cell. Official random ticks then
write Packet53 farmland `60` to dirt `3`. The frozen signal names
`dry=60->3` and `farmland=60:0`. That dirt cell survives a clean save
plus fresh login.

This map does not claim M156 hydration, M304 hoe-till plus trample,
M354 dry-versus-hydrated moisture, or M576 jump-fall trample. It does
not claim wheat, hoe durability, rain hydration, other hoe materials,
or a Worldline soil simulation.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dry-farmland60+rain-roof|cause=random-ticks|wire=packet53-farmland60-to-dirt3|oracle=live-dry-60->3+fresh-login-dirt3:0|column=17,support=4:71:4:1:0,cell=4:72:5:3:0,cover=4:74:5:1:0,hoe=290,farmland=60:0,dry=60->3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`26798de451d0b61504e1945d3196c64186f4c482b60481d9d1b06a61303e1531`.
