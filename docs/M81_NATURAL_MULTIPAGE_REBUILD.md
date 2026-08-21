# M81 natural multipage rebuild

Status: GO in Worldline v1.69.0.

M81 expands M80's single-member transition to one server-authored two-cell
change whose targets occupy distinct natural Aero page keys. The fixed plan
crosses the Z=31/32 cell boundary; target indices zero and eight therefore
remove one synchronized identity from each of two warmed pages.

After retained record 300, the client sends one typed StationAPI request with
the exact plan origin and root nonce. The Aero-free server validates both
blocks, both block entities, and their derived nonces before replacing them
with air and returning an exact acknowledgement. The client requires that
acknowledgement and both remote blocks to be air.

The first fourteen-member record contains exactly two page rebuilds, two
cached pages/page calls, fourteen renderer/enqueue calls, two flush calls, and
zero direct fallback. Earlier records contain sixteen identities and later
records fourteen; rebuilds remain zero outside the transition record.

M74's `0x1010` state and `0xffff` mask remain historical synchronization
evidence for the original sixteen identities. Live renderer calls are the
membership oracle. Timings and request latency are descriptive under nested
instrumentation and are never promotion thresholds.

Two fresh replicas requested at record 300 and observed their first
fourteen-member records at 351 and 355. Their event
renderer/enqueue/flush spans were `7800/5100/126500 ns` and
`7900/4800/200300 ns`. These values are descriptive evidence, not thresholds
or isolated costs.

Nonclaims: arbitrary page topology, additions, repeated or dense waves,
block-entity cleanup, persistence, uninstrumented/additive cost, causality,
regression or improvement, inference, pixels, cross-machine generality,
combat relation, or historical lag reproduction.
