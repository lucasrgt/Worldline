<!-- worldline-map-schema=1 -->
<!-- boundary=cross-lane-seed-matrix -->
<!-- nonclaims=arbitrary-seeds,distant-chunks,biome-names,structures,mutable-entities,client-rendering,particles,gui-portability,other-releases -->
<!-- frozen-trace=e7e55219d0ff2afc770f220ab309cf77ad0b0ed92928b5dd5342ed7494e79576 -->

# M634 behavior map

## Boundary

M634 maps `atlas.scenario.fixed-seed-terrain` to a permanent bounded
cross-lane matrix over three explicit seeds and four explicit neighboring
chunks. The public observation boundary remains
`worldline.api.RemoteChunkSnapshot#blockAt`.

## Frozen trace

The official dedicated-server oracle produces twelve ordered case censuses.
Each census binds the chunk coordinates, non-air count, and complete solid
occupancy hash. Their aggregate SHA-256 is the equatable matrix observation
compared by the Windows/Linux lane seal. Mutable surface decoration is outside
the contract because the discovery replicas proved it can vary independently
of solid occupancy.

The frozen trace signature is
`e7e55219d0ff2afc770f220ab309cf77ad0b0ed92928b5dd5342ed7494e79576`.
The frozen semantic signal is
`seeds=3,chunks=4,cases=12,matrix=0728eca033c0693f8b0016dec8c5e4353806278ac4bdfffb29a0ec75336533bb,clients=3,disconnect=clean`.

## Non-claims

This matrix does not claim arbitrary seeds, distant chunks, biome names,
structures, mutable entities, client rendering, particles, GUI portability,
or equivalence outside the official Beta 1.7.3 dedicated-server lane.
