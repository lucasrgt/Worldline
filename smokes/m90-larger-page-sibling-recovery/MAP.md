# M90 behavior map

M90 starts from the exact synchronized sixteen-identity Aero scene and fixed
camera qualified by M74/M78. It removes and restores index two at
`(x,y+2,z)`, with derived nonce `root*100+3`. Under the pinned 8-block cell
configuration, index two shares index one's natural six-member page. Exact
coordinate, block, block entity, root nonce, derived nonce, phase, ACK, and
restored state all fail closed.

The first retained fifteen-member record remains fully batched: membership
changes `16 -> 15`, page calls stay four, direct fallback/render/list calls
stay zero, cached pages stay four, and the affected page rebuilds exactly once.
After at least thirty records, restoration returns membership to sixteen and
rebuilds that page exactly once again, with the same four page calls and zero
fallback. Every other retained record rebuilds zero pages and preserves M74
state `0x1010/0xffff`.

The 52-byte sidecar binds plan, nonce, both request/event pairs, and transition
rebuild counts. Complete M74/M78 records remain authoritative for membership,
page topology, fallback, and rebuilds. Two fresh replicas share plan/nonce;
lifecycle, EOF, and hashes fail closed.

Frozen trace SHA-256:
`aac17bb2f371a10cf09b7350c228e000700ac36270dc6d3535e3de74a132a402`.

Nonclaims: arbitrary cell size or positions, further depletion, other pages or
identities, additions, repeated cycles, concurrency, stale cleanup,
merge/repacking, persistence, uninstrumented cost, general page causality,
performance or inference, pixels, cross-machine results, combat, or historical
lag.
