# M87 two-cell membership recovery

Status: GO in Worldline v1.75.0.

M87 extends recovery from repeated use of one identity to two distinct cells,
without concurrency. Generation one removes/restores index zero; generation
two removes/restores adjacent index one. The typed protocol binds generation,
operation, index, coordinates, root nonce, and derived cell nonce.

Both replicas proved membership `16 -> 15 -> 16 -> 15 -> 16`, but the two
removals followed different cache paths. Index zero produced three page calls
plus one direct fallback and no rebuild. After its restore rebuilt the page,
index one removal rebuilt immediately, retained four page calls, and produced
no fallback. Both restorations rebuilt exactly once. The cache stayed at four
pages and M74 identity state stayed `0x1010/0xffff`.

Canonical transition indices were `437/599/731/892` and `354/466/578/694`;
instrumented spans were `5700/7900/7800/8100 ns` and
`12300/8100/9100/8500 ns`. These values are descriptive only.

Nonclaims: arbitrary additions, more than two cells, reversed order,
concurrency, stale cleanup, merge/repacking, persistence, uninstrumented cost,
causal or inferential performance claims, pixels, cross-machine generality,
combat, or historical lag reproduction.
