# M91 larger-page depletion recovery

Status: GO in Worldline v1.79.0.

M91 removes index one and then index two from their exact pinned six-member
natural page before restoring index two and index one. It preserves the M85-M90
seed, plan, camera, cache, and complete recording window. Derived nonces remain
`root*100+2` and `root*100+3`.

Both replicas proved membership `16 -> 15 -> 14 -> 15 -> 16`. Every removal
and restoration retained four page calls and zero direct fallback/render/list
calls while rebuilding the affected page exactly once. The cache stayed at
four pages and M74 identity state stayed `0x1010/0xffff`.

Canonical event indices were `365/492/618/746` and `396/580/839/1030`;
instrumented spans were `5700/4100/4600/4800 ns` and
`5800/5400/3800/6300 ns`. These values are descriptive only.

The result establishes bounded two-cell depletion and reverse recovery for
this exact larger page. It does not establish a batching threshold or general
page mechanism.

Nonclaims: further depletion, arbitrary cell size or positions, other pages,
additions, concurrent mutations, stale cleanup, merge/repacking, persistence,
uninstrumented cost, causal or inferential performance claims, pixels,
cross-machine generality, combat, or historical lag reproduction.
