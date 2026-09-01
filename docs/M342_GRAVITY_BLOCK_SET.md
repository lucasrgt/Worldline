# M342 gravity block set

M342 opens the official compound gravity-block boundary. Supported sand
`12:0` and supported gravel `13:0` each lose one stone support through
Packet14. Packet23 type `70` is falling sand. Packet23 type `71` is
falling gravel. Both reuse the existing object tracker. The final
Packet53/Packet51 block states prove both one-cell settlements.

The frozen signal includes both `12` and `13` settled results. This
milestone clones M119 falling sand and M274 falling gravel into one
set. It does not claim long falls, flint drops, entity collisions,
piston interaction, unloaded chunks, or a second Packet23 tracker.

Frozen semantic SHA-256:
`959924fb1c3833226b9a7c0ffeebe212f9be0621ba06ca2f083e08492c72d066`.

Both scheduled-tick observations are normalized through the public
`BlockTickPolicyFixture`.

Headless `B173WireClient` only. No GUI. No Aero.
