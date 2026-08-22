# M562 portal-pair set

M562 opens the official compound portal-pair set. Two Overworld frames whose
X/Z interiors floor-divide by eight to the same Nether cell both exit through
one generated Nether portal. The frozen signal names the shared exit, scale
`8`, `sameCell=1`, one Nether portal, and two `0→-1` transitions. Exact
pair coordinates are dynamic because vanilla may reuse the source frame or
create another Overworld portal on return.

This is distinct from shipping M134 (one portal's `0→-1→0` roundtrip), from
M560 portal-scale coordinate mapping, and from M561 distant portal search.
M562 claims pair collapse plus shared Nether exit only. It does not claim
exact generated Nether coordinates, search radius, entity transport, or
client rendering. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`d9c652c6452861ad1eda49be87a165111895c142551b462152ebb388ffb81b6c`.
