<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=986d67c17068113e152c7cec8614bbc518629fff4c27619ec488da6c2548c079 -->

# M88 behavior map

M88 reverses M87's sequential recovery order in the same frozen geometry:
index one at `(x,y+1,z)` is removed/restored first, then index zero at
`(x,y,z)`. Requests and ACKs carry coordinate, root nonce, generation,
operation, and index; restore state additionally carries the derived cell
nonce. Order, identity, coordinate, nonce, and duplicate drift fail closed.

Request one follows record 300; each later request waits thirty records after
the prior observed transition. Complete M74/M78 records prove membership
`16 -> 15 -> 16 -> 15 -> 16`. Removing index one first rebuilds immediately:
four page calls, no fallback/render/list call, and one rebuild. Its restore
also rebuilds once. Removing index zero second yields three page calls, one
direct fallback/render/list call, and no rebuild. Its restore rebuilds once
and returns to four page calls. Cache count remains four; all other records
rebuild zero.

The 76-byte sidecar stores schema, root, plan, and four
`{request,event,index}` triples. Topology remains authoritative in the complete
records. Two fresh replicas share plan/nonce; lifecycle and hashes fail closed.

Frozen trace SHA-256:
`986d67c17068113e152c7cec8614bbc518629fff4c27619ec488da6c2548c079`.

Nonclaims: arbitrary positions or additions, more than two cells, concurrency,
stale cleanup, merge/repacking, persistence, uninstrumented cost, general
position causality, regression/improvement, inference, pixels, cross-machine
results, combat, or historical lag reproduction.
