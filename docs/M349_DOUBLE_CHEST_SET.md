# M349 double chest set

M349 opens the official adjacent-chest large-window set. Packet15 of
chest item `54` on two neighboring raised stone cells writes block
`54:0` twice. Empty-hand Packet15 on either cell opens Packet100 title
`Large chest` with 54 container-owned slots and a matching 90-slot
Packet104 full view. Both chest cells remain after a clean save plus
fresh login, and the reopened window is still 54 owned slots.

This is the Beta 1.7.3 double-chest compound: two adjacent chest blocks
plus one large chest window. It is distinct from M232 single-chest
place, which never opens Packet100, and from M54's single-chest
descriptor of title `Chest`, 27 owned slots, and 63 total slots. It
does not claim clicks, transfers, chest minecarts, or a Worldline
inventory simulation.

The frozen semantic SHA-256 is
`ec079803ad133072d794b370d1dd5988e5931287cded14a33e3abd7702c0fd26`.

Headless `B173WireClient` protocol 14 only. No GUI. No Aero.
