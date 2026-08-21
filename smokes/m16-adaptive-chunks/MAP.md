# M16 Adaptive Chunk Scheduler Evidence Map

## Boundary

The runner pins Aero Model Lib revision
`436d65b38c53346b465e5e793bd943177ebfaa32` and seed `17320110707`. A seed
process fully generates and saves the target world. Baseline and adaptive
processes each receive a fresh byte-for-byte copy of that canonical save.

Both modes fix the player at x/z 8.5, y 67, yaw 45 degrees, and freeze measured
tick 20. Baseline retains vanilla caller retries. Adaptive redirects only the
single caller boundary, uses vanilla priority order, prefers in-frustum dirty
builders, and applies an 8-item maximum plus 12 ms elapsed-work envelope.

## Executable evidence

- Baseline must return `false` from non-forced calls and exhibit more than one
  call per rendered frame.
- Adaptive must make exactly one non-forced call per frame, return `true`,
  accept real work, report `ACCEPTED_DEFERRED`, and never stall.
- Every accepted item in the first 300 adaptive frames must be visible; accepted
  work cannot exceed proposed work, and the time envelope must stop some batch.
- Adaptive must reach at least baseline's first-window visible-readiness frontier
  and both modes must end with all visible builders clean and built.
- The oracle disables HUD/bobbing, fixes interpolation to current player state,
  waits for zero global dirty builders and 20 stable visible-ready frames, then
  hashes every RGBA framebuffer byte.
- Tick and dimensions must match. The complete framebuffer is still compared
  against the original 64-pixel/delta-2 threshold. After the startup-overlay
  correction, the gate requires and records the observed threshold violation.

## Frozen conclusion

The invariant report is:

```text
scheduler=VISIBLE_FIRST_ADAPTIVE_ENVELOPE
contract=ACCEPTED_DEFERRED_NEXT_FRAME
readiness=VANILLA_PARITY_OR_BETTER
framebuffer=POST_OVERLAY_DIVERGENCE_DETECTED
shipping.status=SUPERSEDED_BY_M17_NO_GO
```

Its SHA-256 is
`f274b0970e16939ba56b8f8796360d54c5f7981168a1e52e9d85da95585eb26b`.

## Non-claims

Frame timings, raw framebuffer hashes, and divergence counts are run evidence,
not frozen cross-machine constants. The original full-frame threshold remains
the decision boundary. This smoke does not modify Aero, establish an optimal
budget, cover moving cameras or multiple saves, or prove elimination of the
historical random spike.
