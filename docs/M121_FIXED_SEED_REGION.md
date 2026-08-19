# M121 fixed-seed region

Status: GO in Worldline v1.109.0.

M121 expands the fixed-seed vanilla world oracle from one chunk to the exact
3x3 region `[-1,1] x [-1,1]`. Each replica loads all nine official chunks,
advances 200 player heartbeats, saves, restarts the same world and obtains a
fresh complete Packet51 view before hashing 294,912 positions.

Both fresh replicas contain exactly 128,529 blocks other than air/water/lava,
reproduce the exact top Y, legacy ID and metadata for all 2,304 columns, and
match solid/empty occupancy throughout every internal chunk seam. Surface
evidence includes water wherever it is the exact column surface.

The experiment also found an official-runtime boundary worth preserving:
scheduled and random ticks can leave different interior IDs, metadata and even
position masks after an equal heartbeat count. Those values are retained as
diagnostics but are deliberately excluded from the frozen semantic claim.

M121 does not claim a deterministic interior volume, raw fluid state, biomes,
climate, structures, entities, regional lighting, chunks outside the nine-cell
window, arbitrary seeds, generation-order independence, or a Worldline world
generator.
