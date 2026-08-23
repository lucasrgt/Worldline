<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a82e3eb16c9c12a3901e03775d53898a725914562f5bd971d7dc5d2444c75104 -->

# M92 behavior map

M92 extends M91 inside the exact same pinned six-member natural page. It
removes indices one, two, and three at `(x,y+1..3,z)`, then restores indices
three, two, and one. Every request and ACK binds coordinate, root nonce,
ordinal, operation, and index; restore state also carries the derived cell
nonce. Order, identity, coordinate, nonce, or duplicate drift fails closed.

Request one follows record 300; each later request waits thirty records after
the prior observed transition. Complete M74/M78 records prove membership
`16 -> 15 -> 14 -> 13 -> 14 -> 15 -> 16`. Every transition preserves four
page calls, zero direct fallback/render/list calls, and produces exactly one
rebuild at its correlated event record. Cache count remains four and every
other record has zero rebuilds.

The 100-byte sidecar stores schema, root, plan, and six
`{request,event,index}` triples with indices `1,2,3,3,2,1`. Topology remains
authoritative in the complete records. Two fresh replicas share plan and
nonce; artifact hashes and clean lifecycle fail closed.

Frozen trace SHA-256:
`a82e3eb16c9c12a3901e03775d53898a725914562f5bd971d7dc5d2444c75104`.

Nonclaims: emptying this page or crossing a batching threshold, arbitrary
additions, other pages or geometries, concurrency, stale cleanup,
merge/repacking, persistence, uninstrumented cost, causality,
regression/improvement, inference, pixels, cross-machine results, combat, or
historical lag.
