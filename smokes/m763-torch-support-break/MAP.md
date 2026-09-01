<!-- worldline-map-schema=1 -->
<!-- boundary=m763-torch-support-break -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=be5e59f43b9242f0a2828026130188ee1d0ed53ca5a69604da1eb6630919598e -->

# M763 torch support break behavior map

The cloned raised stone column ends in one support stone cell at `(4,71,4)`.
Packet15 of torch item `50` against its east face writes an official wall
torch `50:1` into the adjacent cell `(5,71,4)`; the metadata proves the torch
is wall-mounted rather than floor-placed. Packet14 with iron pickaxe `257`
then breaks that single support cell.

The authoritative removal of the supporting block pops the dependent wall
torch to air `0:0` and drops exactly one torch item as a Packet21 entity.
After clean save plus fresh login the popped torch cell and the support cell
are both persisted air, so no torch remnant survives the cycle.

This map does not claim floor-torch placement metadata (M175), the remaining
wall-torch faces `50:2` through `50:4` (M400), redstone torch inversion or
burnout (M312, M555), or water wash-off of a floor torch (M599). It claims no
lighting propagation, no item pickup, and no generic attachment model beyond
this one east-facing support break. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.

Frozen signal:
`column=17,support=4:71:4:1:0->0:0,torch=5:71:4:50:1->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean`

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-column+east-wall-torch50:1|cause=packet15-item50-east-face+packet14-ironpick257-support|wire=packet53-50:1->air+packet21-50|oracle=torch-support-break-not-floor-not-wash-not-burnout|column=17,support=4:71:4:1:0->0:0,torch=5:71:4:50:1->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`be5e59f43b9242f0a2828026130188ee1d0ed53ca5a69604da1eb6630919598e`.
