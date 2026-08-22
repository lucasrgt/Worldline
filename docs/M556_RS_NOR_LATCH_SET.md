# M556 RS-NOR latch set

M556 opens the official two-torch RS-NOR latch boundary. Packet15 of
redstone torch item `76` places north wall torch `76:4` on body A and south
wall torch `76:3` on body B. A west-facing repeater and dust line from B
holds A unlit `75:4`. A ground-lever SET pulse into B's repeater turns Q on
(`75:4 -> 76:4`) and Q-bar off (`76:3 -> 75:3`); both stay after the lever
drops. A RESET pulse returns the pair to `75:4` plus `76:3` and stays off.
The final RESET pair remains after a clean save plus fresh login.

This milestone is distinct from M312's single north invert `76:4 -> 75:4`
and from M555 torch burnout. It does not claim wire consumers, repeater
memory as a product, or the lighting plane. Headless `B173WireClient` only.
No GUI. No Aero.

The frozen semantic SHA-256 is
`7241b7297eea8617a084daaf981b2001119180794ec82ab3fbd7d664a55537ad`.
