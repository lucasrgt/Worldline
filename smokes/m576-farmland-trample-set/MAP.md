<!-- worldline-map-schema=1 -->
<!-- boundary=m576-farmland-trample-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=460485cad949455638ecc3bf33174cb4a4e28e8d8a0ef7c1f26a829cfdfe72ba -->

# M576 farmland trample set behavior map

Packet15 of wooden hoe `290` only tills dirt `3` into dry farmland `60:0`
as fixture setup on the raised stone column. The actor then stands on
that cell and Packet13 jump-fall trampling converts it to dirt `3:0`.
The frozen signal names `trample=60->3` and `farmland=60:0`. That dirt
cell survives a clean save plus fresh login.

This map does not claim M156 hydration, M304 hoe-till plus trample, or
M354 dry-versus-hydrated moisture. It does not claim wheat, hoe
durability, rain, other hoe materials, or a Worldline soil simulation.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-farmland60|cause=packet13-jump-fall|wire=packet53-dirt3|oracle=live-trample-60->3+fresh-login-dirt3:0|column=17,support=4:71:4:1:0,cell=4:72:4:3:0,hoe=290,farmland=60:0,trample=60->3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`460485cad949455638ecc3bf33174cb4a4e28e8d8a0ef7c1f26a829cfdfe72ba`.
