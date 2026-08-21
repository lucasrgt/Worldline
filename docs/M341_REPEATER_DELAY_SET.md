# M341 repeater delay set

M341 opens the official diode delay-bit boundary. Packet15 of repeater item
`356` on a raised west-facing stone line places unpowered block `93:3` with
look yaw `90`. Empty-hand Packet15 on that cell then cycles delay bits
through 1, 2, 3, and 4 ticks (`93:3 -> 93:7 -> 93:11 -> 93:15`). Packet53
confirms each metadata change. A clean save plus fresh login retains
`93:15`.

Frozen semantic SHA-256:
`5dfcac91e31b99f9d578961c42075eb4456a7e3dde14bf19c6d069bf7dc49136`.

This milestone is distinct from M170's single 1-tick place and lever pulse
to `94:3`. It does not claim powered hold, repeater locking, comparators
(they do not exist in Beta 1.7.3), or quasi-connectivity. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.
