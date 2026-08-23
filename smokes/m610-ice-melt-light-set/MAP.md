# M610 behavior map

The M193 raised stone column hosts ice `79` beside one floor torch `50:5`
on the east neighbor. Packet15 places ice on the support top, then places
the torch on the east stone top so official block light reaches the ice
cell. Official random ticks convert ice `79:0` to still water `9:0`. Exact
melt delay is not hashed.

This map does not re-qualify ice placement without melt (M193) or the
ice-and-snow compound torch melt (M386). It does not claim snow-layer
melt, snowfall, silk-touch, or slipperiness. Headless `B173WireClient`
protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ice79-torch-adjacent-melt|cause=packet15-item79+packet15-item50|wire=packet53-ice79:0->water9:0+packet53-torch50:5|oracle=official-block-light-random-tick-melt+fresh-login|column=17,support=4:71:4:1:0,east=5:71:4:1:0,ice=4:72:4:79:0->9:0,torch=5:72:4:50:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`63ded528e3f7faed4c46a7fcdc0d097771b808128f8d977e3034bf8b390230a0`.
