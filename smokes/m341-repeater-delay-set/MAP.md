# M341 behavior map

Repeater item 356 is placed on a raised west-facing stone line as unpowered
block `93:3` (delay 1, facing metadata 3 from look yaw `90`). Empty-hand
Packet15 then tunes the same cell through delay bits `7`, `11`, and `15`
(2, 3, and 4 ticks). Packet53 confirms each metadata change. A fresh
Packet51 login retains unpowered `93:15`.

This map is distinct from M170's single 1-tick place and lever pulse. It
does not claim powered `94` hold, repeater locking, or comparators.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-west-line+repeater93|cause=packet15-item356-place+empty-hand-packet15-tune|wire=packet53-repeater93:3->7->11->15|oracle=delay-bits-1-4+fresh-login-93:15|column=17,support=4:71:4:1:0,repeater=4:72:4:93:3->7->11->15,facing=3,delay=1->2->3->4,look=90:0,persisted=93:15,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`5dfcac91e31b99f9d578961c42075eb4456a7e3dde14bf19c6d069bf7dc49136`.
