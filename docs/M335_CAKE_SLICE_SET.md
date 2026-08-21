# M335 cake slice set

M335 opens the official compound cake-slice boundary. Packet15 of cake
item `354` on a raised stone support places uneaten cake `92:0`. Empty-hand
Packet15 then eats three successive slices: metadata advances
`0 -> 1 -> 2 -> 3`. Each bite requires `health < 20` and restores three
health points. Beta 1.7.3 has no hunger bar; cake heals health.

The frozen signal includes multiple cake `92` metadata values. That is
distinct from M244 place-only (`92:0`) and M160 one-slice eat (`92:0->1`).
The bitten `92:3` cell remains after a clean save plus fresh login.

Frozen semantic SHA-256:
`3ef77cdef925e0457ef17467a33321cc83aaffe183eb51cc6fc7768273ff2f68`.

Official cake has six bites via metadata. This milestone proves three
successive slices. It does not claim the remaining bites, the sixth-bite
air cell, crafting, drops, or hunger-era food. Headless `B173WireClient`
only. No GUI. No Aero.
