<!-- worldline-map-schema=1 -->
<!-- boundary=m309-rail-power -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ff3995ce5426f88877abdf561aada4f7f2968dfa7fbdc44f768202ec4c14ff80 -->

# M309 behavior map

Packet15 places powered-rail item `27` on a raised stone column as unpowered
`27:0`, then floor torch item `76` on an east stone pad. The official server
sets powered bit 8, writing `27:8` beside floor torch `76:5`. Two stone pads
west of the column keep detector rail item `28` out of powered-rail reach.
Packet15 of minecart item `328` on that detector emits Packet23 type `10` and
sets occupancy bit 8, writing `28:8`. Those exact cells survive a clean save
plus fresh login.

This map does not re-qualify unpowered powered rail `27:0` (M184), unpowered
detector `28:0` (M185), torch-only `76:5` (M182), or minecart spawn on
regular rail `66` (M155). It reuses `awaitObjectSpawn(10)` and does not add a
second Packet23 tracker. It does not claim minecart acceleration.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+detector28+minecart328+powered-rail27+torch76|cause=packet15-item28+packet15-minecart328+packet15-item27+packet15-item76|wire=packet23-type10+thrower0+packet53-detector28:8+packet53-rail27:8+packet53-torch76:5|oracle=occupied-detector+powered-bit8+fresh-login|column=17,support=4:71:4:1:0,detectorSupport=2:71:4:1:0,pad=5:71:4:1:0,detector=2:72:4:28:8,rail=4:72:4:27:8,cart=type10+thrower0+fixed80:2331:144,powered=1,torch=5:72:4:76:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ff3995ce5426f88877abdf561aada4f7f2968dfa7fbdc44f768202ec4c14ff80`.
