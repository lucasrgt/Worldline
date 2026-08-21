# M90 larger-page sibling recovery

Status: GO in Worldline v1.78.0.

M90 removes/restores index two at `(x,y+2,z)` without changing the M85-M89
seed, plan, camera, cache, or recording window. Its derived nonce is
`root*100+3`. Under pinned Aero's 8-block cell configuration, index two shares
index one's natural six-member page.

Both replicas proved membership `16 -> 15 -> 16`. Removing index two retained
four page calls and zero direct fallback/render/list calls while rebuilding the
affected page exactly once. Restoration rebuilt exactly once again with the
same fully batched route. The cache stayed at four pages and M74 identity state
stayed `0x1010/0xffff`.

Canonical transition indices were `365/516` and `403/525`; instrumented spans
were `6600/5300 ns` and `7000/6400 ns`. These values are descriptive only.

The result matches index one for an exact sibling identity under this pinned
fixture. It does not establish a general page mechanism or causal cost.

Nonclaims: arbitrary cell size or positions, further depletion, other pages or
identities, additions, repeated cycles, concurrency, stale cleanup,
persistence, uninstrumented cost, causal or inferential performance claims,
pixels, cross-machine generality, combat, or historical lag reproduction.
