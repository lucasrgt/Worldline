<!-- worldline-map-schema=1 -->
<!-- boundary=m393-stair-facing-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1e94922033cceeec477b29842f80b9bce86737cb240b266bad8ad4cf93cf0253 -->

# M393 behavior map

One official session places oak stairs `53` and cobble stairs `67` with two
look-yaw facings each on a raised stone row. Packet12 yaw `-90` writes east
metadata `0`; yaw `90` writes west metadata `1`. Packet15 of item `53` yields
`53:0` then `53:1`. Packet15 of item `67` yields `67:0` then `67:1`. All four
cells survive a clean save plus fresh login.

This map does not re-qualify the shipping 1:1 east-only traces (M186/M187) or
the workbench craft family (M319). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oakstairs53+cobblestairs67|cause=packet15-item53+look-90+look90+packet15-item67+look-90+look90|wire=packet53-oakstairs53:0+53:1+cobblestairs67:0+67:1|oracle=look-facing-metadata-set+fresh-login|column=17,support=4:71:4:1:0,oak=4:72:4:53:0+5:72:4:53:1,cobble=6:72:4:67:0+7:72:4:67:1,look=-90+90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1e94922033cceeec477b29842f80b9bce86737cb240b266bad8ad4cf93cf0253`.
