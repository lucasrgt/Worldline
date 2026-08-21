# M122 fixed-seed region lighting

Status: GO in Worldline v1.110.0.

M122 expands M112's one-chunk lighting oracle to the complete fixed-seed 3x3
region qualified by M121. After 200 official heartbeats, clean save and server
restart, a fresh reader receives all nine complete chunks and samples both
light planes at every one of their 294,912 block positions.

Two fresh world replicas reproduce the exact block-light SHA-256, sky-light
SHA-256 and all sixteen histogram bins for each plane. The block plane contains
539 nonzero samples; the sky plane contains 156,193. No state or light value is
normalized before hashing.

M122 proves deterministic post-restart light data for this fixed seed, region,
heartbeat window and official artifact. It does not claim causal propagation,
arbitrary regions/seeds, day-night updates, weather, dimensions, generated
structure semantics, lighting performance, or a Worldline light engine.
