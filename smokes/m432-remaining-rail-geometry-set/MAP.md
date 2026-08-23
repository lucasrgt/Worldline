<!-- worldline-map-schema=1 -->
<!-- boundary=m432-remaining-rail-geometry-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3da03f5b4d6dd509fa5fc0925d5ea7422d5cd6ddb96e7acb84b5854de2ab61b1 -->

# M432 behavior map

Official server symbols:

- `net.minecraft.src.BlockMinecartTrack` writes remaining rail `66`
  geometry from neighbor rails and a one-block step. Metadata `2` is
  ascending east. Metadata `6` is the south-east curve.
- Packet15 of rail item `66` on a raised stone column plus an east step
  places slope `66:2`. A two-block-south L of three rails writes corner
  curve `66:6`. Look yaw `0` is pinned; neighbor geometry dominates.
- Both cells survive a clean save plus fresh login.

This map does not re-run M183 north-south `66:0`, M309 powered-rail `27:8`
or detector occupancy, M377 powered-rail motion, or M402 detector `28:0->8`.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+rail66-slope+rail66-curve|cause=packet15-item66-slope+curve|wire=packet53-rail66:2+rail66:6|oracle=remaining-rail-geometry-set+fresh-login|column=17,support=4:71:4:1:0,slope=4:72:4:66:2,curve=4:72:6:66:6,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3da03f5b4d6dd509fa5fc0925d5ea7422d5cd6ddb96e7acb84b5854de2ab61b1`.
