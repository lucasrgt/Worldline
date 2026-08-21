# M77 qualification cycle

`DirectStageTimingCycle` verifies the pinned Aero checkout and server-safe
fixture closure, builds Aero in a disposable worktree, and runs two fresh
server/client replicas. Replica two receives replica one's exact plan; both use
the same nonce, fixture, camera, heap, frame-limit state, and minimum window.

The client writes M74 census and M77 stage artifacts only after the measurement
bracket seals. The parser binds their schema, exact file length and EOF, nonce,
plan, record count, elapsed time, and every corresponding structural record.
M74 must retain sixteen synchronized calls, state `0x1010`, identity mask
`0xffff`, and visible chunks. M77 must retain sixteen renderer calls, sixteen
`queueAtRest` direct-fallback calls, and two empty-page flush calls at every
index. The plain BE's server-safe class closure is also checked.

Renderer and queue totals must be positive and the enclosing renderer total
must not be smaller than its nested queue total. Flush totals are nonnegative
because multiple calls can fall inside one Windows clock quantum, but the full
series must contain positive time. The runner reports median, p95, p99, maximum,
zero-flush count, and both artifact hashes as dynamic descriptive evidence.

A diagnostic one-replica mode is explicit and cannot emit qualification
evidence. The canonical path always runs both replicas; failures are terminal.
Checkout/worktree state, server absence of Aero, normal disconnect, server stop,
runtime markers, sidecar bytes, and SHA-256 are all checked fail closed.

The frozen semantic trace reproduces SHA-256
`4ac829480cfb8a9409d89c35e002246e43a0a143815303e1ac520e8990988a4c`.
