<!-- worldline-map-schema=1 -->
<!-- boundary=m600-ladder-climb-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=70add79ff9fcd22f9d99b2ade7b5e0033e213175af28ac85e8b18b07e24966e3 -->

# M600 behavior map

The fixture raises a two-cell stone wall and Packet15-places ladder item `65`
as two east-facing `65:5` cells. A headless protocol-14 client then
Packet13-falls in the adjacent air column and Packet13-stations while occupying
the ladder, colliding toward the supporting wall. Pose y must hold or increase
versus the air fall. Both ladder cells survive a clean save plus fresh login.

This map does not claim M174 east-face placement persistence alone, M361
controlled-client ladder physics, or M447 spider Packet31/33/34 wall climb.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ladder65-east-column2|cause=packet15-item65+packet13-air-fall+packet13-ladder-pose|wire=packet13-pose-air-vs-ladder|oracle=ladder-climb-or-hold-vs-air-fall|column=18,support=4:71:4:1:0,upper=4:72:4:1:0,ladder=5:71:4:65:5+5:72:4:65:5,face=east,ticks=10,air-fall=true,ladder-hold=true,climbed=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`70add79ff9fcd22f9d99b2ade7b5e0033e213175af28ac85e8b18b07e24966e3`.
