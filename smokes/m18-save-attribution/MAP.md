<!-- worldline-map-schema=1 -->
<!-- boundary=aero-save-window -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=855ae55bc5944ae98d3fb6b66fe6840fc7561d425ce620b9ba45a55720f6c7bd -->

# M18 Save Attribution Evidence Map

## Pair

The runner pins Aero Model Lib revision
`436d65b38c53346b465e5e793bd943177ebfaa32` and seed `17320110707`. It creates
one canonical dense save, restores exact copies for both measured processes,
and injects one non-forced `World.saveWithLoadingDisplay` at tick 80:

- `skipped`: `-Daero.benchmark.skipNonForcedSaves=true` cancels that save;
- `live`: the same call runs and is timed.

Both windows are stationary, view distance 0, and analyze every Aero line
after capture readiness (at least 120). Extra `WorldFlush` lines are kept
because they share the same counters. The Aero checkout is not modified.
Skip-versus-live is a Worldline Gradle property plus the existing Aero
test-mod mixin.

## Executable findings

- The skipped window records a cancelled save (`worldSaveSkipped > 0`) and
  zero `worldSaveMs`.
- The live window records a visible `worldSaveMs` on the same line as
  `compileChunksMs`, `gcTimeDeltaMs`, `heap`, and allocation counters.
- Frame timings, heap peaks, and the worst-frame class are printed
  observations and are not frozen cross-machine constants.
- The historical random spike on a real machine-dense map remains a
  non-claim. This pair only proves that a non-forced save is now visible on
  the compile/GC/heap timeline that M17 had disabled. The lived hitch is
  periodic after the world is already loaded; this window still includes
  startup compile and must not be read as that post-load spike.

The invariant conclusion is frozen by SHA-256.

## Decision

M18 completes save-path attribution on the synthetic dense fixture. It does
not promote the adaptive scheduler, does not relax the M16 visual threshold,
and does not declare the historical spike eliminated.

Frozen expected signature SHA-256: `855ae55bc5944ae98d3fb6b66fe6840fc7561d425ce620b9ba45a55720f6c7bd`
