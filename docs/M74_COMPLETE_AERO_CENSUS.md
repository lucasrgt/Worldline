# M74 complete Aero frame census

Status: GO in Worldline v1.62.0.

M74 replaces M73's threshold/GC/heartbeat-selected rows with a bounded complete
census of renderer intervals in the same zero-versus-sixteen synchronized Aero
fixture. It preserves two fresh pairs in opposite order, the exact paired plan,
fixed camera, explicit content messages, and structural absent/present gates.

The client arms only after mode-specific reconciliation and renderer identity
validation. A preallocated primitive recorder then captures every complete
`GameRenderer.onFrameUpdate` HEAD-to-HEAD interval in its bracket. The previous
TAIL supplies at-rest renders, list calls, visible chunks, content calls,
received/applied counts, and the sixteen-bit identity mask. The first HEAD is a
baseline and partial boundary frames are excluded.

The Aero spike logger is disabled. M74 invokes the pinned package-private mesh
counter reset through a test-only Mixin invoker, so the census does not depend
on selective logger output or Mixin HEAD ordering. Sampling still adds a clock
read, primitive writes, validation, and resets; zero overhead is not claimed.

At 720 or more intervals and twelve or more seconds, the recorder seals at HEAD.
Only afterward, at TAIL, it writes one versioned binary artifact. The runner
strictly reparses its schema, size, records, aggregate duration, plan, nonce,
fixture state, and treatment-specific work. Artifact hashes and all numeric
summaries are dynamic evidence rather than release gates.

The reported medians, p95s, p99s, maxima, and present-minus-absent deltas are
descriptive. The experimental unit remains two pairs, not thousands of
autocorrelated frame records. M74 does not claim causality, significance,
regression or improvement, isolated Aero cost, density response, pixel
correctness, cross-machine generality, or historical lag reproduction.
