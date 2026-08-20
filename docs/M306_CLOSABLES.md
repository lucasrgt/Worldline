# M306 closables

M306 compounds the official wooden-door close boundary with the official
wooden-trapdoor close boundary on one raised stone fixture. Wooden door
item 324 is placed with Packet15 as lower `64:0` and upper `64:8`.
Empty-hand Packet15 opens those halves to `64:4` / `64:12`, then a second
empty-hand Packet15 closes them to `64:0` / `64:8`. Trapdoor item 96 is
placed against the same support's east face as closed `96:3`, opened with
empty-hand Packet15 to `96:7`, then closed back to `96:3`. Both closed
states remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`0287dd23ec4f04c0960b98f43f8e16ff75d416ad1fb8ffb16478c579b8bc4865`.

This milestone is distinct from M277 and M278 (open only). It does not
claim iron doors, redstone power, stacking, hinge or facing variants
beyond this official placement, waterlogging, iron trapdoors, or the
Nether. Headless `B173WireClient` only. No GUI. No Aero.
