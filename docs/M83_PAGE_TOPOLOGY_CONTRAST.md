# M83 page topology contrast

Status: GO in Worldline v1.71.0.

M83 isolates affected-page count from removed-member count. Two fresh arms use
the exact M82 scene, seed, plan `(10,65,31)`, nonce, camera, page configuration,
warmup, and complete-census window. Both remove exactly two synchronized
members and finish with live membership fourteen.

The same-page arm removes indices zero and one within the Z=31 page. The
cross-page arm removes indices zero and four, one on each side of Z=31/32.
The Aero-free server validates both exact blocks, block entities, coordinates,
root nonce, derived cell nonces, and topology code before mutation and ACK.
The client requires the ACK and both blocks as air before capturing the first
fourteen-member record.

Same-page produces exactly one rebuild; cross-page produces exactly two. Both
retain two cached pages/page calls, two flush calls, membership fourteen, and
zero direct fallback. For these exact constant-cardinality arms, rebuild count
therefore follows affected-page count.

The qualified transitions occurred at records 361 and 329. Their event
renderer/enqueue/flush spans were `5400/3900/103200 ns` and
`8500/6000/292900 ns`. These are descriptive nested-instrumentation values,
not isolated page cost or a performance comparison.

Nonclaims: additive per-page cost, performance regression/improvement,
arbitrary topology/cardinality, additions, repeated/dense waves, cleanup,
persistence, causality, inference, pixels, cross-machine generality, combat,
or historical lag reproduction.
