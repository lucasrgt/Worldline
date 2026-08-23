<!-- worldline-map-schema=1 -->
<!-- boundary=m593-door-upper-break-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ff0b9e220d130cc2f0c5f67eabccc0b161c409c17334c027717bda1cab2aa07a -->

# M593 behavior map

The cloned M162 west-facing wooden door occupies one raised stone column.
Wooden door item `324` places BlockDoor lower `64:0` at `(4,72,4)` and upper
`64:8` at `(4,73,4)`. Packet14 while holding iron axe `258` fully breaks the
UPPER half. Official leftover cleanup removes the lower half to air and drops
Packet21 wooden-door item `324`. Both cells are air. Fresh login Packet51
keeps those leftover cells.

This map is distinct from M162/M277 open/toggle (`64:0/8 -> 64:4/12`), M428
remaining hinge/face pairs, and M379 iron door `71`. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+woodendoor64|settle=5+40ticks|cause=packet15-item324-place+packet14-ironaxe258-upper|effect=official-door64-upper-break+both-air+packet21-324|observation=fresh-login-packet51|column=17,support=4:71:4:1:0,lower=4:72:4:64:0->0:0,upper=4:73:4:64:8->0:0,drops=packet21-324,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ff0b9e220d130cc2f0c5f67eabccc0b161c409c17334c027717bda1cab2aa07a`.
