# M74 behavior map

M74 reuses M73's exact paired sixteen-cell plan, fixed camera, tracked readiness,
explicit per-cell synchronization, and opposite `present/absent`,
`absent/present` order. Measurement starts only after the client has reconciled
the complete mode-specific fixture: zero cells for absent and all sixteen exact
coordinate/nonce identities for present.

The selective Aero spike logger is disabled. A test-only invoker resets the
pinned Aero at-rest counters at each renderer HEAD. The first HEAD after arming
establishes the baseline; every later HEAD records the preceding complete
HEAD-to-HEAD interval together with the primitive snapshot captured at its TAIL.
The fixed arrays hold 65,536 records and fail on overflow rather than wrapping.
The hot sampler performs `nanoTime`, primitive reads/writes, validation, and the
counter reset, with no retained per-sample allocation or I/O.

After at least 720 complete intervals and twelve seconds, the recorder seals at
HEAD. Its following TAIL writes one versioned big-endian binary artifact. The
runner reparses the exact schema and length, rejects trailing bytes, verifies
positive intervals and aggregate duration, and binds every row to the arm,
nonce, plan, fixture state, renderer work, and visible chunks. Absent rows must
contain zero content/Aero work; present artifacts must finish with the exact
sixteen-cell mask and nonzero real renderer work.

Frozen trace:

```text
v1|design=2-balanced-pairs-16/0+0/16|fixture=exact-plan+tracked-camera+explicit16|census=every-complete-head-to-head-interval-after-fixture-ready|capture=fixed65536-primitive-memory+prior-tail-counters|window=min720intervals+12s|flush=single-binary-after-seal|fields=intervalNs+atRest+listCalls+visibleChunks+contentCalls+received+applied+rendered|aero-log=disabled+explicit-mesh-counter-reset|stats=descriptive-whole-census+paired-dynamic-deltas|regression-causality-density-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `2cc4533688aa06ba1d69309639c36e16688b09eb4deeeb27d044277550d2d1a7`.

Nonclaims: all game-loop/process frames, isolated Aero cost, zero-overhead
measurement, causal or inferential effects, regression/improvement, density
response, pixel visibility, complete world equivalence, cross-machine
generality, combat relation, or reproduction of the historical lag mechanism.
