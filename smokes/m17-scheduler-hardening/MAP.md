# M17 Scheduler Hardening Evidence Map

## Matrix

The runner pins Aero Model Lib revision
`436d65b38c53346b465e5e793bd943177ebfaa32` and seed `17320110707`. It creates
canonical empty and dense saves, restores exact copies for every measured
process, and executes three scenarios:

- `stationary-empty`: stationary camera, view distance 2, tick 20;
- `stationary-dense`: stationary camera, view distance 0, tick 20;
- `moving-dense`: a 60-tick diagonal camera path, view distance 1, tick 60.

Each scenario compares vanilla caller retries, Aero's old compile governor, and
the M16 visible-first adaptive contract. Baseline and adaptive then freeze game
ticks, remove non-player render entities, fix daylight/weather/interpolation,
drain the complete chunk queue, and hold readiness for 200 rendered frames.

## Executable findings

- Vanilla performs multiple non-forced calls per frame while work returns
  incomplete. Once an empty queue drains, later calls may correctly complete.
- Adaptive performs exactly one successful contract call per measured frame,
  accepts visible and later background work, never stalls, and drains every
  tested global queue.
- The old governor records rejected work but leaves substantial queue debt in
  each bounded run. It remains rejected.
- Adaptive reaches at least vanilla's normalized readiness frontier while
  stationary. The moving run lags that frontier during the measurement window
  but reaches complete global drainage.
- The elapsed budget stops batches, but one individual rebuild cannot be
  preempted and can exceed the configured 12 ms envelope.
- All three final framebuffer pairs exceed M16's 64-pixel/delta-2 tolerance.
  The evidence records that divergence rather than relaxing the threshold.

Frame timings and pixel counts are printed observations and are not frozen
cross-machine constants. The invariant conclusion is frozen by SHA-256.

## Decision

M17 completes the generalization audit but rejects upstream promotion. The
evaluation profile at
`adapters/aero-model-lib/opt-in/worldline-adaptive.properties` is default-off
and marked `lab-only-no-go`. M18 must address single-item overshoot and explain
or eliminate final-frame divergence before any Aero integration is proposed.
