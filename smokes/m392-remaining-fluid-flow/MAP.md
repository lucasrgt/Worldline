<!-- worldline-map-schema=1 -->
<!-- boundary=m392-remaining-fluid-flow -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8ec5aefbab73a3cd36a48185fa30c6266c70c3392ce80a5b319f8a6d94f2cfba -->

# M392 behavior map

One official session builds two raised stone trenches and seeds still water
`9:0` and still lava `11:0` behind dirt gates. Packet14 opens each gate onto
air. Official fluid updates then publish flowing water and flowing lava in
those adjacent cells while both sources remain still. The four fluid cells
and their metadata survive a clean save plus fresh login.

This map does not re-qualify the shipping 1:1 single-fluid traces (M114/M120
/M138) or the bucket place/pickup set (M344). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-trenches+seeded-still-water9+seeded-still-lava11+dirt-gates3|settle=40+70ticks|cause=packet14-open-horizontal-air-cells|confirmation=packet53-air|effect=official-horizontal-water-and-lava|observation=live-packet53+fresh-login-packet51|oracle=still-water9+still-lava11-horizontal-flow-set|column=17,water-source=4:72:4:9:0,water-target=5:72:4:3:0->0:0->9:1,lava-source=4:72:8:11:0,lava-target=5:72:8:3:0->0:0->11:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8ec5aefbab73a3cd36a48185fa30c6266c70c3392ce80a5b319f8a6d94f2cfba`.
