# M562 portal-pair set

M562 opens the official compound portal-pair set. The first Overworld frame
generates a Nether exit. After returning, a second frame is built beside the
returned Overworld portal, in that portal's same floor-divided 8:1 cell. The
second frame exits through the exact same generated Nether portal. The signal
names the shared exit, `sameReturnCell=1`, one nearby Nether portal geometry,
and two `0→-1` transitions. The returned frame may be the source frame or a
new frame created by vanilla.

This is distinct from shipping M134 (one portal's `0→-1→0` roundtrip), from
M560 portal-scale coordinate mapping, and from M561 distant portal search.
M562 claims returned-cell adjacency plus shared Nether exit only. It does not claim
that the original source frame and second frame share one floor-divided cell, or
exact generated Nether coordinates, search radius, entity transport, or
client rendering. Headless `B173WireClient` only. No GUI. No Aero.

The semantic SHA-256 is reconfrozen only by the final serialized qualification.
