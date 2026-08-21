# M89 sibling-cell membership recovery

Status: GO in Worldline v1.77.0.

M89 removes/restores index four at `(x,y,z+1)` without changing the M85-M88
seed, plan, camera, cache, or recording window. Its derived nonce is
`root*100+5`. Under pinned Aero's 8-block cell configuration, index four is the
other identity in the same natural two-member page as index zero.

Both replicas proved membership `16 -> 15 -> 16`. Removing index four changed
four page calls to three plus one direct fallback/render/list call and rebuilt
zero pages. Restoration returned to four page calls, removed the fallback, and
rebuilt exactly once. The cache stayed at four pages and M74 identity state
stayed `0x1010/0xffff`.

Canonical transition indices were `542/696` and `487/785`; instrumented spans
were `7000/5400 ns` and `5700/5000 ns`. These values are descriptive only.

The result matches index zero for the exact sibling identity under this pinned
fixture. It does not establish a general page mechanism or causal cost.

Nonclaims: arbitrary cell size or positions, other pages or identities,
additions, repeated cycles, concurrency, stale cleanup, persistence,
uninstrumented cost, causal or inferential performance claims, pixels,
cross-machine generality, combat, or historical lag reproduction.
