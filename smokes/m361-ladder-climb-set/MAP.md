# M361 behavior map

The server half sends Packet15 for ladder item `65`, observes two east-facing
Packet53 cells `65:5`, saves, and verifies both cells through a fresh login. Its
trace explicitly claims placement and persistence only.

The climb half belongs to the controlled client differential.
`B173PhysicsProbe.ladder` builds equivalent wall-air and two-cell ladder
fixtures and calls the mapped Beta 1.7.3 player movement root. The official
oracle calls the corresponding obfuscated root with the same fixture and tick
count. Two mapped and two official processes must produce the same trace.

No Packet13 climb or Worldline vertical-motion equation is accepted as the
physics oracle. Replacement signatures remain pending official qualification.
