<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f0f506ffa69950d8d4030819a4c6c5ca3f190edcfd3f4ba29f3a4ef4129959ad -->

# M93 behavior map

M93 completely depletes the exact pinned six-member natural page qualified by
M90-M92. It removes indices `1,2,3,5,6,7`, then restores `7,6,5,3,2,1`.
Every request and ACK binds coordinate, root nonce, ordinal, operation, and
index; restore state also carries the derived cell nonce. Identity, order,
coordinate, nonce, or duplicate drift fails closed.

Request one follows record 300; each later request waits thirty records after
the prior observed transition. Complete M74/M78 records prove membership
`16 -> 15 -> 14 -> 13 -> 12 -> 11 -> 10 -> 11 -> 12 -> 13 -> 14 -> 15 -> 16`.
The client fixes page TTL at 100000 frames so cardinality is isolated from
stale-cache eviction. At page counts two through six, four page calls and one rebuild preserve the
batched route. At count one, page calls drop to three, direct render/list is
one, and rebuild is zero. At count zero, page calls remain three while direct
render/list and rebuild are zero. Reverse restoration mirrors these states.

The 172-byte sidecar stores schema, root, plan, and twelve
`{request,event,index}` triples. Cache count remains four and complete M74
identity state remains `0x1010/0xffff`. Two fresh replicas share plan/nonce;
artifact hashes and clean lifecycle fail closed.

Frozen trace SHA-256:
`f0f506ffa69950d8d4030819a4c6c5ca3f190edcfd3f4ba29f3a4ef4129959ad`.

Nonclaims: other pages/geometries, default-TTL/stale-cache eviction, concurrent mutation,
merge/repacking, persistence, uninstrumented cost, causality,
regression/improvement, inference, pixels, cross-machine results, combat, or
historical lag.
