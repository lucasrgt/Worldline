<!-- worldline-map-schema=1 -->
<!-- boundary=m428-remaining-door-orient-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=10dad6f6b34f4140a80e7a09abeebaa5ff502bc6eee4607964a64dae72626bd2 -->

# M428 behavior map

One official session places remaining wooden-door hinge/face halves on a
raised stone 2x2 of supports. Packet12 look yaw `-90`, `0`, `90`, and `180`
plus Packet15 of wooden door item `324` write lower/upper pairs `64:0/8`,
`64:1/9`, `64:2/10`, and `64:3/11`. The upper half is the `64:8` bit family.
Supports are spaced so vanilla hinge pairing does not rotate or open a
neighbor. All eight cells survive a clean save plus fresh login.

This map does not re-qualify M162 one-facing place-and-toggle, M306 wooden
door plus trapdoor close, or M379 iron door `71`. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+woodendoor64:0/8+64:1/9+64:2/10+64:3/11|cause=packet15-item324-look-90+0+90+180|wire=packet53-door64:0/8+64:1/9+64:2/10+64:3/11|oracle=remaining-door-hinge-face+fresh-login|column=17,support=4:71:4:1:0,face0=4:72:4:64:0/8,face1=6:72:4:64:1/9,face2=4:72:6:64:2/10,face3=6:72:6:64:3/11,look=-90+0+90+180,persisted=64:0/8+64:1/9+64:2/10+64:3/11,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`10dad6f6b34f4140a80e7a09abeebaa5ff502bc6eee4607964a64dae72626bd2`.
