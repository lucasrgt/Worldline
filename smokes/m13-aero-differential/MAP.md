# M13 Aero Differential Evidence Map

## Boundary

The cycle runs the pinned Aero 3.0.0 test consumer without tracked upstream
changes. One smoke-only Mixin fixes seed, camera, chunks, warmup, and measured
ticks. Fresh dense, reload, empty, and compile-budget modes use the same client
path. Raw worlds and logs remain ignored derived artifacts.

## Executable findings

- The fresh fixture has more global BlockEntities than real entity blocks.
- Real entity blocks retain the same count after reload; the excess phantom
  BlockEntities disappear.
- Both dense and Aero-disabled empty scenes exercise chunk compilation;
  exploratory repetitions crossed the 10 ms compile and 25 ms frame
  thresholds, but those timing crossings are observations rather than gates.
- No stable dense-scene amplification is claimed.
- Making the current compile governor active on all frames produces over 100
  skipped retries per accepted compile, so it is rejected as a mitigation
  despite reducing sampled spikes. M14 later proved the main calls are
  non-forced and that each `false` is immediately retried until the deadline.

Frame times, threshold crossings, and exact counts are reported but not frozen. The invariant report,
Aero revision, seed, and control topology are frozen.

## Non-claims

M13 does not identify why vanilla/StationAPI chunk compilation is slow, prove
the historical random spike has one cause, or approve the current governor for
shipping. It isolates the fixture persistence split and narrows the next
investigation to the chunk compiler and its caller retry semantics.

Frozen expected signature SHA-256: `1759de8beeeef257a4027fd79f590ec7a72d364729863d1cb5fe373741399e80`
