<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=87fa014b6cd31a48c7cffa7f839d0b407ecf823d815a80f1a578afa00828c649 -->

# M89 behavior map

M89 starts from the exact synchronized sixteen-identity Aero scene and fixed
camera qualified by M74/M78. It removes and restores index four at
`(x,y,z+1)`, with derived nonce `root*100+5`. Under the pinned 8-block cell
configuration, this is the other identity in index zero's natural two-member
page. Exact coordinate, block, block entity, root nonce, derived nonce, phase,
ACK, and restored state all fail closed.

The first retained fifteen-member record proves the same route observed for
index zero: page calls change `4 -> 3`, one direct fallback/render/list call
appears, cached pages remain four, and removal rebuilds zero pages. After at
least thirty records, restoration returns membership to sixteen, page calls to
four, direct/render/list calls to zero, and rebuilds exactly one page. Every
other retained record rebuilds zero pages and preserves M74 state
`0x1010/0xffff`.

The 52-byte sidecar binds plan, nonce, both request/event pairs, and transition
rebuild counts. Complete M74/M78 records remain authoritative for membership,
page topology, fallback, and rebuilds. Two fresh replicas share plan/nonce;
lifecycle, EOF, and hashes fail closed.

Frozen trace SHA-256:
`87fa014b6cd31a48c7cffa7f839d0b407ecf823d815a80f1a578afa00828c649`.

Nonclaims: arbitrary cell size or positions, other pages or identities,
additions, repeated cycles, concurrency, stale cleanup, merge/repacking,
persistence, uninstrumented cost, general page causality, performance or
inference, pixels, cross-machine results, combat, or historical lag.
