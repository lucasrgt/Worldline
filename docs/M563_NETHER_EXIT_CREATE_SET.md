# M563 nether-exit-create set

M563 opens the official Nether-exit create set. The actor builds an M382
obsidian `49` frame, ignites portal `90`, and enters the Nether. A relog
shifts the session east so no Overworld portal remains in the 128-block
search window. Returning through a second Nether frame makes the official
server create a new Overworld portal: fourteen obsidian `49` cells plus
six portal `90` cells.

This is distinct from M134 roundtrip reuse of the original M382 frame,
from M561 destination search on the Nether side, and from M562 pair
collapse. M563 does not claim exact created coordinates, concurrent
travelers, entity transport, death/respawn, or client rendering.
Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.
