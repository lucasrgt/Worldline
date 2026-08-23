<!-- worldline-map-schema=1 -->
<!-- boundary=m433-remaining-chest-orient-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b9750e81a03028d1bb7345d6699d951772dea723fefb2cb303312f4c43423f03 -->

# M433 behavior map

One official session places remaining chest `54` look-yaw facings and both
adjacent-pair axes on a raised stone row. Packet12 yaw `-90` then `90`
writes isolated `54:0` cells two apart. Packet15 then writes an east-west
pair and a north-south pair, each two adjacent `54:0` cells. All six cells
survive a clean save plus fresh login.

This map does not open Packet100, re-qualify M232's single `54:0` place, or
claim the M349 `Large chest` 54-slot window. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+chest54-look-faces+ew-pair+ns-pair|cause=packet15-item54+look-90+look90+packet15-item54-ew+packet15-item54-ns|wire=packet53-chest54:0+54:0+54:0+54:0+54:0+54:0|oracle=look-facing+adjacent-pair-orientations+fresh-login|column=17,support=4:71:4:1:0,iso=4:72:4:54:0+6:72:4:54:0,ew=8:72:4:54:0+9:72:4:54:0,ns=4:72:6:54:0+4:72:7:54:0,look=-90+90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b9750e81a03028d1bb7345d6699d951772dea723fefb2cb303312f4c43423f03`.
