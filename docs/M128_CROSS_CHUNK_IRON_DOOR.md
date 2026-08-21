# M128 cross-chunk iron door

Status: GO in Worldline v1.116.0.

M128 composes a cross-chunk redstone source with an official two-block
consumer. A lever in chunk `(1,0)` powers the stone supporting an iron door in
chunk `(0,0)`. Both door halves open with their exact vanilla metadata.

The final independent reader observes exactly two changes in the door chunk
and one in the lever chunk.

M128 does not claim arbitrary redstone topology, wire networks, other powered
consumers, closing/recovery, unloaded chunks, timing order, or a Worldline
redstone evaluator.
