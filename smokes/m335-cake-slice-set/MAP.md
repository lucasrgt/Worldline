<!-- worldline-map-schema=1 -->
<!-- boundary=m335-cake-slice-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3ef77cdef925e0457ef17467a33321cc83aaffe183eb51cc6fc7768273ff2f68 -->

# M335 behavior map

Official `BlockCake` (block 92) is placed from cake item 354 as uneaten
`92:0`, then eaten with empty-hand Packet15 for three successive metadata
slices `0 -> 1 -> 2 -> 3`. Vanilla cake stores six bites as metadata
`0..5`; the sixth bite removes the block. Each bite requires
`health < 20` and restores three health points.

This map is the compound of M244 place-only and M160 one-slice eat. The
frozen signal includes multiple cake `92` metadata values.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+blockcake92|cause=packet15-item354-place+empty-hand-packet15-three-slices|wire=packet53-cake92:0->1->2->3|oracle=blockcake-three-slice-metadata+fresh-login|column=17,support=4:71:4:1:0,cake=4:72:4:92:0->1->2->3,slices=3,bites=6,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3ef77cdef925e0457ef17467a33321cc83aaffe183eb51cc6fc7768273ff2f68`.
Headless protocol-14 only. No GUI. No Aero.
