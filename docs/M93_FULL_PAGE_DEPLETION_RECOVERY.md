# M93 full-page depletion recovery

Status: GO in Worldline v1.81.0.

M93 removes indices `1,2,3,5,6,7` from their exact pinned six-member
natural page before restoring `7,6,5,3,2,1`. It preserves the M85-M92 seed,
plan, camera, complete recorder, and Aero-free common/server closure. Every
request, ACK, and restored state binds ordinal, operation, index, coordinate,
root nonce, and the derived cell nonce.

Both replicas proved membership
`16 -> 15 -> 14 -> 13 -> 12 -> 11 -> 10 -> 11 -> 12 -> 13 -> 14 -> 15 -> 16`.
At page cardinalities two through six, the page retained four calls, zero
direct fallback/render/list calls, and one rebuild. At cardinality one it used
three page calls, one direct call, and no rebuild. At zero it retained three
page calls with neither direct work nor rebuild. Reverse restoration traversed
the same boundaries symmetrically.

The page TTL is explicitly fixed and runtime-checked at 100000 frames. This
keeps all four cached pages present while the experiment isolates cardinality.
Default-TTL expiration and stale-cache eviction are not inferred from this
result.

Canonical event indices were
`445/660/926/1151/1290/1417/1562/1745/1918/2083/2285/2489` and
`501/705/847/995/1112/1266/1413/1585/1815/2062/2244/2396`;
instrumented spans were
`3700/5400/5500/3500/2900/2300/4600/4700/4400/3900/6100/4300 ns` and
`4100/2500/5400/6600/7100/4100/3600/3300/3300/3200/3300/4600 ns`.
These values are descriptive only.

Nonclaims: other pages/geometries, default-TTL or stale-cache eviction,
concurrent mutations, merge/repacking, persistence, uninstrumented cost,
causal or inferential performance claims, pixels, cross-machine generality,
combat, or historical lag reproduction.
