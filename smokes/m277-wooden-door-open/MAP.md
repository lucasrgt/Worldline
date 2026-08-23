<!-- worldline-map-schema=1 -->
<!-- boundary=m277-wooden-door-open -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1f0b2fd8a64b2092de4a093f2d5cf0c8110b4363e2ee0199faf1ca2ae7ff2eb0 -->

# M277 behavior map

Official `BlockDoor` (block 64) is placed from wooden door item 324 onto a
raised stone support. Vanilla emits two Packet53 cells: lower `64:0` and upper
`64:8` for this facing, matching M162. Empty-hand Packet15 on the lower half
toggles open (`64:4` / `64:12`). A clean save plus fresh login retains those
open halves. Iron door M241 does not toggle by empty hand.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+woodendoor64|cause=packet15-item324-place+empty-hand-packet15-open|wire=packet53-door64:0/8->4/12|oracle=woodendoor-open+fresh-login|column=17,support=4:71:4:1:0,lower=4:72:4:64:0->4,upper=4:73:4:64:8->12,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1f0b2fd8a64b2092de4a093f2d5cf0c8110b4363e2ee0199faf1ca2ae7ff2eb0`.
