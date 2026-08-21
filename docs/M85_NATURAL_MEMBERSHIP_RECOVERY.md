# M85 natural membership recovery

Status: GO in Worldline v1.73.0.

M85 qualifies one bounded natural removal followed by restoration of the same
server-authored synchronized Aero cell. Two fresh replicas use the same seed,
plan, root nonce, fixed camera, page configuration, warmup, and complete M74
census window.

After retained record 300, the client requests removal of cell index zero. The
Aero-free server validates the exact coordinate, block, block entity, root
nonce, and derived cell nonce before setting air and returning the phase-one
ACK. Live membership becomes fifteen. The four-page cache remains allocated,
but only three pages remain batch-eligible; the orphan member uses one direct
fallback. Consequently page calls change `4 -> 3`, direct fallback and public
render/list calls change `0 -> 1`, and no page rebuild is reported.

Thirty retained records after the removal transition, the client requests
restoration at the same coordinate. The server requires air and no block
entity, replaces the registered block, restores the original derived nonce,
sends a dedicated state packet, and returns the phase-two ACK. The client
buffers that state across packet ordering until the block exists, then creates
or validates the exact block entity and applies the nonce.

The restored sixteen-member record has exactly one rebuild, four page calls,
zero direct fallback, and render/list calls back at zero. Every other retained
record has zero rebuilds. The complete M74 state stays `0x1010/0xffff`.

The qualified replicas recorded remove/restore event indices `403/564` and
`520/647`. Their instrumented transition renderer spans were `6300/10900 ns`
and `4900/4200 ns`. These values are descriptive only; M85 does not attribute
cost or performance direction.

Nonclaims: arbitrary additions, different cells, repeated cycles, stale client
block-entity cleanup, merge/repacking policy, persistence, uninstrumented or
additive cost, causality, inference, regression/improvement, pixels,
cross-machine generality, combat, or historical lag reproduction.
