# M609-SAPLING-DARK-HALT-SET sapling dark halt set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M609 qualifies official sapling growth halt in darkness as one SET. Packet15 plants oak sapling 6:0 on a raised dirt pad. One sapling is covered by stone 1 so light at the cell above is 0 and the sapling stays 6:0 across a long random-tick window. Lit control saplings on the same pad set official stage bit 8 or become log 17. This is distinct from M339 sapling-growth-set, which bonemeal-grows uncovered oak, spruce, and birch into matching logs. Exact wait length and which lit sapling stages are not hashed. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone sapling pad in two fresh official server JVMs. Each run Packet15-places dirt 3 and oak sapling 6, covers one sapling with stone 1, then waits a bounded random-tick window until Packet53 stages a lit sapling while the covered sapling stays 6:0. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions.

Expected signal: `column=17,support=4:71:4:1:0,sapling=6:0,lit=7,covered=4:73:6:6:0,cover=4:74:6:1:0,lit-stage>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `84a2148a1d8deae33271631d42a13f4a1c9e2727173bbd5f428463d0747134c7`.
