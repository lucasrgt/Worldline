# M88 reverse two-cell membership recovery

Status: GO in Worldline v1.76.0.

M88 reverses M87's two exact cell identities without changing seed, plan,
nonce, camera, cache, or recording window. Generation one removes/restores
index one; generation two removes/restores index zero. The typed protocol binds
generation, operation, index, coordinates, root nonce, and derived cell nonce.

Both replicas proved membership `16 -> 15 -> 16 -> 15 -> 16`. Index one still
rebuilt immediately when removed first: four page calls, no direct fallback,
and one rebuild. Index zero still used the direct fallback when removed second:
three page calls, one render/list call, and no rebuild. Both restorations rebuilt
exactly once. The cache stayed at four pages and M74 identity state stayed
`0x1010/0xffff`.

Canonical transition indices were `492/754/956/1109` and `443/571/675/789`;
instrumented spans were `4900/5600/9000/10700 ns` and
`5200/6200/5400/5500 ns`. These values are descriptive only.

The result distinguishes the two exact cell identities under this fixture and
order reversal. It does not establish a general positional cause or mechanism.

Nonclaims: arbitrary positions or additions, more than two cells, concurrency,
stale cleanup, merge/repacking, persistence, uninstrumented cost, causal or
inferential performance claims, pixels, cross-machine generality, combat, or
historical lag reproduction.
