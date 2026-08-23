<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=5f019eb32c7f34b31ca907e9fdbec3b827254a08cdf0cbe11a91c703644b2f7e -->

# M91 behavior map

M91 depletes two synchronized identities from the exact six-member natural
page qualified by M90, then restores them in reverse order. The four requests
are remove index one `(x,y+1,z)`, remove index two `(x,y+2,z)`, restore index
two, and restore index one. Every request and ACK binds the coordinate, root
nonce, ordinal, operation, and index; restore state also carries the derived
cell nonce. Order, identity, coordinate, nonce, or duplicate drift fails closed.

Request one follows record 300; each later request waits thirty records after
the prior observed transition. Complete M74/M78 records prove membership
`16 -> 15 -> 14 -> 15 -> 16`. Every transition preserves four page calls,
zero direct fallback/render/list calls, and produces exactly one rebuild at
its correlated event record. Cache count remains four and every other record
has zero rebuilds.

The 76-byte sidecar stores schema, root, plan, and four
`{request,event,index}` triples with indices `1,2,2,1`. Topology remains
authoritative in the complete records. Two fresh replicas share plan and
nonce; artifact hashes and clean lifecycle fail closed.

Frozen trace SHA-256:
`5f019eb32c7f34b31ca907e9fdbec3b827254a08cdf0cbe11a91c703644b2f7e`.

Nonclaims: further depletion or a batching threshold, arbitrary additions,
other pages or geometries, concurrency, stale cleanup, merge/repacking,
persistence, uninstrumented cost, causality, regression/improvement,
inference, pixels, cross-machine results, combat, or historical lag.
