# M15 Explicit Chunk-Work Contract Evidence Map

## Boundary

Two fresh Aero-disabled worlds use the same seed, camera, zero measurement
warmup, and 120 ticks. Baseline preserves vanilla caller behavior. Contract
mode redirects the single `GameRenderer` call site into the adapter-owned
`Aero_ChunkWorkContract`, accepts two priority-selected real rebuilds, records
remaining work as deferred, and resumes only on the next rendered frame.

## Executable evidence

- Every baseline `false` is observed together with its same-frame retry calls.
- Every contract frame distinguishes accepted work from a non-empty deferred
  queue and maps that outcome to one caller completion, with no stalled batch.
- A runtime-independent smoke exercises complete, accepted/deferred, and
  stalled/deferred results and their end-current-frame compatibility mapping.
- Dirty age and visible dirty/ready counts are sampled from the real chunk
  array after culling on every rendered frame.
- Each rebuilt chunk's complete Tessellator vertex stream, texture coordinates,
  color, normal, and layer-emptiness bits are hashed. At least 100 common
  non-empty positions and more than two thirds of the comparable cohort must
  match exactly, while nonzero mismatches preserve the
  observed temporal divergence caused by rebuilds at different world ticks.

## Non-claims

Timing thresholds are reported but not frozen. Exact per-chunk vertex-stream equality is
a chunk-geometry oracle, not a full framebuffer, HUD, transparency-order, or
driver-equivalence proof. The contract remains a Worldline adapter candidate;
M15 does not modify or release the pinned Aero checkout.
