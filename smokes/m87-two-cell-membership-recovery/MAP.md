<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=091dd5a68a9e7650ef91496f86cbc9dc5e82e006863d097a8e3c637402a103a4 -->

# M87 behavior map

M87 applies two sequential recovery cycles to distinct synchronized identities
in the same frozen M85 geometry: index zero at `(x,y,z)`, then index one at
`(x,y+1,z)`. Requests and ACKs carry coordinate, root nonce, generation,
operation, and index; restore state additionally carries the derived cell
nonce. Order, identity, coordinate, nonce, and duplicate drift fail closed.

Request one follows record 300; each later request waits thirty records after
the prior observed transition. Complete M74/M78 records prove membership
`16 -> 15 -> 16 -> 15 -> 16`. Removing index zero yields three page calls,
one direct fallback/render/list call, and no rebuild. Its restore rebuilds once
and returns to four page calls. Removing index one then rebuilds immediately:
four page calls, no fallback/render/list call, and one rebuild. Its restore
also rebuilds once. Cache count remains four; all other records rebuild zero.

The 76-byte sidecar stores schema, root, plan, and four
`{request,event,index}` triples. Topology remains authoritative in the complete
records. Two fresh replicas share plan/nonce; lifecycle and hashes fail closed.

Frozen trace SHA-256:
`091dd5a68a9e7650ef91496f86cbc9dc5e82e006863d097a8e3c637402a103a4`.

Nonclaims: arbitrary additions, more than two cells, reversed order,
concurrency, stale cleanup, merge/repacking, persistence, uninstrumented cost,
causality, regression/improvement, inference, pixels, cross-machine results,
combat, or historical lag reproduction.
