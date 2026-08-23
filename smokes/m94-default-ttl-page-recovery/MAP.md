<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c2617f80713c9054acdf8ade17e4474a3a1ed275a2c092fc6d455363493acfcf -->

# M94 behavior map

M94 repeats M93's exact six-member page depletion, but does not set
`aero.becell.pageTtlFrames` or `aero.perf.memory`. The pinned normal-memory
default is therefore 600 frames. Runtime checks require both properties to be
absent.

After indices `1,2,3,5,6,7` are removed, the target page remains empty while
the other three pages continue to flush. The pinned cache sweeper runs every
128 nonempty flushes and expires entries whose age is greater than 600. The
client requires target cache `4 -> 3`, `expiredCachedPages 0 -> 1`, and
`evictedCachedPages == 0` before restoration may begin.

The first restored member remains direct with three cached pages. The second
restored member recompiles the target page and returns cache `3 -> 4`;
remaining reverse restores retain the batched route. All twelve transitions
preserve M93's exact coordinate, ordinal, operation, index, nonce, ACK, and
state checks.

The 184-byte sidecar stores plan/root, the exact expiry record and counters,
and twelve request/event/index triples. Complete M74/M78 records bind cache,
page calls, direct fallback, rebuild, membership, and `0x1010/0xffff` state.
Two fresh replicas share plan/nonce; hashes and lifecycle fail closed.

Frozen trace SHA-256:
`c2617f80713c9054acdf8ade17e4474a3a1ed275a2c092fc6d455363493acfcf`.

Nonclaims: high-memory or explicit TTLs, max-cache eviction, other pages or
geometries, concurrent mutation, merge/repacking, persistence, uninstrumented
cost, causality, regression/improvement, inference, pixels, cross-machine
results, combat, or historical lag.
