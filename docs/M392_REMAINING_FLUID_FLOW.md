# M392 remaining fluid flow

M392 opens the official compound remaining-fluid-flow set. Still water `9:0`
and still lava `11:0` each sit in a raised stone trench behind a dirt gate.
Packet14 opens the adjacent cell to air. Official scheduled flow then fills
those air cells while the sources remain still. The frozen signal includes
multiple fluid cells and metadata for both `9` and `11`.

This is distinct from shipping M114, M120, and M138 (single-fluid 1:1) and
from shipping M344 (bucket place plus pickup). It does not claim vertical
flow, mixing, buckets, infinite sources, or a Worldline fluid simulator.
Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`8ec5aefbab73a3cd36a48185fa30c6266c70c3392ce80a5b319f8a6d94f2cfba`.
