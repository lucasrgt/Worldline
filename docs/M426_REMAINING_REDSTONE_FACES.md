# M426 remaining redstone faces

M426 opens the official remaining lever wall/ground attachment family and
the remaining repeater delay-facing family as one SET.

Packet15 of lever item `69` places `69:2` on the west face of a raised
stone column, `69:3` on the south face, and `69:4` on the north face.
Two UP-face placements attach ground levers; vanilla Beta 1.7.3 chooses
metadata `5` or `6` from `World.rand`, so the freeze records ground
attachment rather than a single orientation bit. East wall `69:1` stays
with M242/M340.

Packet15 of repeater item `356` with look yaw `180`, `-90`, and `0` places
remaining unpowered facings `93:0`, `93:1`, and `93:2`. Empty-hand
Packet15 tunes the east cell to delay 2 (`93:5`). Torch `76:5` on the
west input then powers that remaining-facing diode to `94:5`. West-facing
delay `93:3->7->11->15` stays with M341. The M170 west 1-tick pulse
`93:3->94:3->93:3` is not repeated.

The frozen signal names remaining lever faces, remaining repeater
facings, delay-2 east `93:1->5`, and powered `94:5`. Those cells survive
a clean save plus fresh login. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.

Frozen semantic SHA-256:
`1bb55855bc7d7a3c3f9eef22fd7e235e02c3e5220a782fb29ed29a27bb69b44e`.
