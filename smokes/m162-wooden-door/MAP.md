# M162 behavior map

Official `BlockDoor` (block 64) is placed from wooden door item 324 onto a
raised stone support. Vanilla emits two Packet53 cells: lower `64:0` and upper
`64:8` for this facing. Empty-hand Packet15 on the lower half toggles open
(`64:4` / `64:12`) and closed back to the placed metadata. A clean save plus
fresh login retains the closed door.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+woodendoor64|cause=packet15-item324-place+empty-hand-packet15-toggle|wire=packet53-door64:0/8->4/12->0/8|oracle=woodendoor-open-close+fresh-login|column=17,support=4:71:4:1:0,lower=4:72:4:64:0->4->0,upper=4:73:4:64:8->12->8,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d14b3dc599ec9ecd4d0f39074ae46e401c37bd078d558f4b0dd0b477a3f1bfea`.
