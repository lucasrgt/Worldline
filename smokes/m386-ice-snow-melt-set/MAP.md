<!-- worldline-map-schema=1 -->
<!-- boundary=m386-ice-snow-melt-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=00d10f8cca091d8efcf6f005b84e192d110161deafabfb6a71d69862a5de6b7a -->

# M386 behavior map

The M193 raised stone column hosts ice `79` and snow layer `78` together
under one floor torch `50:5`. Packet15 places ice on the support top and
snow on the west wall top. Packet15 then places the torch on the east
wall top so official block light reaches both cells. Official random
ticks convert ice `79:0` to still water `9:0` and snow `78:0` to air
`0:0`. Exact melt delay is not hashed.

This map does not re-qualify ice placement without melt (M193), snow
block placement (M194), snow-layer placement without melt (M203), or the
fragile ice-break plus glass-break compound (M308). Headless
`B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ice79-torch-melt+snow78-torch-melt|cause=packet15-item79+packet15-item78+packet15-item50|wire=packet53-ice79:0->water9:0+packet53-snow78:0->0:0+packet53-torch50:5|oracle=official-block-light-random-tick-melt+fresh-login|column=17,support=4:71:4:1:0,west=3:71:4:1:0,westwall=3:72:4:1:0,snow=3:73:4:78:0->0:0,ice=4:72:4:79:0->9:0,east=5:71:4:1:0,eastwall=5:72:4:1:0,torch=5:73:4:50:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`00d10f8cca091d8efcf6f005b84e192d110161deafabfb6a71d69862a5de6b7a`.
