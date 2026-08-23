<!-- worldline-map-schema=1 -->
<!-- boundary=m369-cake-full-eat-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1e7b764b96a4af45a053eec0e064137715747cb2554f80daaece626bee17a371 -->

# M369 behavior map

Official `BlockCake` (block 92) is placed from cake item 354 as uneaten
`92:0`, then eaten with empty-hand Packet15 through the remaining metadata
slices beyond M335's three. Vanilla cake stores six bites as metadata
`0..5`; the sixth bite removes the block (`air/0`). Each bite requires
`health < 20` and restores three health points.

This map continues M335's three-slice set through slices 3-6 until air.
The frozen signal includes multiple cake `92` metadata values past M335
(`3`, `4`, `5`) plus the sixth-bite air cell.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+blockcake92|cause=packet15-item354-place+empty-hand-packet15-remaining-slices|wire=packet53-cake92:0->1->2->3->4->5->0|oracle=blockcake-full-eat-metadata+fresh-login|column=17,support=4:71:4:1:0,cake=4:72:4:92:0->1->2->3->4->5->0,slices=6,bites=6,air=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1e7b764b96a4af45a053eec0e064137715747cb2554f80daaece626bee17a371`.
Headless protocol-14 only. No GUI. No Aero.
