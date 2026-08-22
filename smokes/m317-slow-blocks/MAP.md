# M317 behavior map

Frozen expected signature SHA-256: bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a

The server half sends Packet15 for cobweb item `30` and soul sand item `88`,
observes the resulting Packet53 cells, saves, and verifies `30:0` plus `88:0`
through a fresh login. Its trace explicitly claims placement and persistence
only.

The slowdown half belongs to the controlled client differential.
`B173PhysicsProbe.slowBlocks` builds equivalent air, cobweb, and soul-sand
corridors and calls the mapped Beta 1.7.3 player movement root. The official
oracle calls the corresponding obfuscated root with the same fixture and tick
count. Two mapped and two official processes must produce the same trace.

No Packet13 displacement or Worldline slowdown equation is accepted as the
physics oracle. Replacement signatures remain pending official qualification.
