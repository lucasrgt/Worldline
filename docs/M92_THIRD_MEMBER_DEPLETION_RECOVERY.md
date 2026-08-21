# M92 third-member depletion recovery

Status: GO in Worldline v1.80.0.

M92 removes indices one, two, and three from their exact pinned six-member
natural page before restoring three, two, and one. It preserves the M85-M91
seed, plan, camera, cache, and complete recording window. Derived nonces remain
`root*100+2`, `root*100+3`, and `root*100+4`.

Both replicas proved membership `16 -> 15 -> 14 -> 13 -> 14 -> 15 -> 16`.
Every transition retained four page calls and zero direct fallback/render/list
calls while rebuilding the affected page exactly once. The cache stayed at
four pages and M74 identity state stayed `0x1010/0xffff`.

Canonical event indices were `521/703/849/978/1120/1224` and
`441/704/903/1077/1251/1370`; instrumented spans were
`4900/3200/5300/4600/4700/5600 ns` and
`4900/7100/6100/15400/3800/6500 ns`. These values are descriptive only.

The result establishes bounded three-cell depletion and reverse recovery for
this exact larger page. It does not establish an empty-page boundary or a
general batching threshold.

Nonclaims: emptying this page, arbitrary cell size or positions, other pages,
additions, concurrent mutations, stale cleanup, merge/repacking, persistence,
uninstrumented cost, causal or inferential performance claims, pixels,
cross-machine generality, combat, or historical lag reproduction.
