# M14 Chunk Backlog Evidence Map

## Boundary

The cycle runs two fresh empty worlds on the pinned Aero 3.0.0 consumer. Both
use the same seed, camera, warmup, capture interval, and smoke-only probes. The
baseline preserves vanilla `compileChunks`; bounded mode uses vanilla's
`DirtyChunkSorter`, rebuilds at most two candidates once per rendered frame,
removes them from the dirty queue, and reports the frame complete.

## Executable findings

- `GameRenderer` calls `compileChunks(camera, false)` and retries while it
  returns `false`, bounded by the current frame deadline.
- The fixed camera begins measurement with thousands of dirty builders. Most
  measured frames have no invalidation or `markDirty` event, yet compilation
  continues while that initial queue drains.
- Vanilla usually rebuilds one candidate per call and returns `false`, causing
  multiple synchronous calls in a frame.
- The prototype performs one call and two real rebuilds per measured frame,
  then returns `true`; it does not create the M13 false-return retry storm.

## Tradeoff and non-claims

The prototype deliberately drains the queue more slowly, so terrain update
latency can increase. Timing distributions are reported but not frozen because
the desktop run is scheduling-sensitive. M14 does not approve this policy for
shipping, prove visual equivalence, or claim that the initial backlog explains
every historical lag spike.

Frozen expected signature SHA-256: `65f43a875d18e96066441cb308fed7089bab8414b087f4398c1555211f2bae6a`
