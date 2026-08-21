# M84 four-page topology contrast

Status: GO in Worldline v1.72.0.

M84 extends the constant-cardinality topology boundary to a scene with four
natural page keys. Two fresh arms use the same seed, plan `(10,77,29)`, nonce,
camera, page configuration, warmup, and complete-census window. Both remove
exactly three synchronized members and finish with live membership thirteen.

The aligned 4x4 scene has page-key populations `9/3/3/1`. With pinned Aero's
minimum of two instances, three pages are cached batches and the singleton is
a stable direct fallback. The one-page arm removes indices `0,1,2` from the
nine-member page. The three-page arm removes indices `0,3,12`, one from each
batch page. The resulting populations are `6/3/3/1` and `8/2/2/1`, so both
retain three cached pages/page calls and one direct fallback.

The Aero-free server validates the exact blocks, block entities, coordinates,
root nonce, derived cell nonces, and topology code before mutation and ACK.
The client requires the ACK and all three target blocks as air before binding
the first thirteen-member record. The event rebuild count is exactly one in
the concentrated arm and three in the distributed arm, with zero rebuilds in
all other retained records.

The qualified transition records were 417 and 398. Their instrumented
renderer/enqueue/flush spans were `4400/1800/91800 ns` and
`8100/4100/138800 ns`. These values are descriptive observations only; they
do not isolate page cost or establish performance direction.

Nonclaims: additive per-page cost, performance regression/improvement,
arbitrary topology or page population, additions, repeated/dense waves,
cleanup, persistence, causality, inference, pixels, cross-machine generality,
combat, or historical lag reproduction.
