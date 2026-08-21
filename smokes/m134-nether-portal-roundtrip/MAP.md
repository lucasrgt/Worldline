# M134 behavior map

The client performs M133's `0→-1` journey, discovers the generated Nether
portal by its exact six-cell geometry, leaves it for 220 official ticks, then
re-enters. A second Packet9 returns the same connection to dimension `0` and a
new chunk cache exposes a valid Overworld-side portal.

Both sides contain six portal blocks and fourteen obsidian blocks. The server
saves the player back in dimension `0`. Vanilla may reuse the source portal or
generate another one near the scaled destination; that choice and its exact
coordinate do not enter the frozen evidence.

Frozen semantic SHA-256:
`c2f903638b1e364b9781c247e61c22c77a28a036212dbe444db5c62498e2a74b`.
