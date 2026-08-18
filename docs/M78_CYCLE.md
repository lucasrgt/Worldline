# M78 qualification cycle

`PagedStageTimingCycle` verifies the pinned Aero checkout and server-safe class
closure, builds Aero in a disposable worktree, and runs two fresh graphical
client/modded-server replicas. The second receives the first replica's exact
plan; nonce, camera, heap, frame-limit state, and minimum window are equal.

The client marker is absent from common/server classes. After fixture readiness
the overlay validates `fpsLimit=0` and disabled Aero pacing, aligns to a fresh
M74 baseline, and records renderer/enqueue/flush spans plus public Aero page
counters. Serialization occurs only after M74 seals.

The parser cross-binds both binary schemas, exact lengths and EOF, nonce, plan,
count, elapsed duration, and every record. M74 must contain `0/0` per-BE Aero
counters, sixteen identity calls, state `0x1010`, mask `0xffff`, and visible
chunks. M78 must contain calls `16/16/2`, queued count 16, cached/page calls 4,
direct fallback 0, and rebuild 0 at every index.

Renderer/enqueue totals are positive and nested correctly; the complete flush
series must be positive. Median, p95, p99, maximum, artifact hashes, and any
zero-flush count are dynamic descriptive evidence, never release thresholds.
Diagnostic one-replica mode cannot qualify. Canonical failures are terminal.

The frozen semantic trace reproduces SHA-256
`dbb52fb098cf377aa90027c4000ab7073efa6cbe5bc4f4fa56fa2090d38ae894`.
