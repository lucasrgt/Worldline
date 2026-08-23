<!-- worldline-map-schema=1 -->
<!-- boundary=m578-cactus-adjacent-break-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=437e319b299a6629b32f3d6d89b00e7ce02d3f26fdb1300eeace32cc656301ca -->

# M578 behavior map

A raised stone column receives sand `12:0`. Packet15 plants cactus item
`81` as isolated block `81:0`. Packet15 of stone `1` against the cactus
east face then places a horizontally adjacent solid at `(5,73,4)`.
Official `canBlockStay` fails, Packet53 replaces cactus with air, and
Packet21 drops cactus item `81`. Fresh login Packet51 keeps cactus air
beside neighbor stone `1:0` and sand `12:0`.

This map is distinct from M167 isolated cactus persistence, M275 cactus
contact damage, and M384 cactus plus sugar-cane height growth.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-sand12+cactus81|cause=packet15-item1-east-adjacent-solid|wire=packet53-cactus81->0+packet21-81|oracle=adjacent-solid-pop+fresh-login-air|column=17,sand=4:72:4:12:0,cactus=4:73:4:81:0->0:0,neighbor=5:73:4:1:0,drops=packet21-81,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`437e319b299a6629b32f3d6d89b00e7ce02d3f26fdb1300eeace32cc656301ca`.
