# M86 repeated membership recovery

Status: GO in Worldline v1.74.0.

M86 qualifies two bounded remove/restore generations over the same exact
server-authored synchronized Aero cell. It preserves M85's seed and scene so
the only treatment change is repetition plus an explicit generation protocol.

Each generation uses a typed removal request and a typed restoration request.
The server validates the coordinate, root nonce, derived cell nonce, current
block and block-entity state, generation, and operation order before mutating
the world. ACKs repeat the complete generation-bound request. Restore-state
messages carry the original cell nonce and generation; the client buffers them
until the flattened block update exists and then creates or validates the
exact block entity.

The complete census proves `16 -> 15 -> 16 -> 15 -> 16`. Both removed periods
use three cached page calls plus one direct fallback, with one public
render/list call and no rebuild. Both restoration records recover four cached
page calls, no fallback or public render/list call, and exactly one rebuild.
The cached-page count remains four and M74 identity state remains
`0x1010/0xffff` throughout.

The 60-byte sidecar binds the four requests and four observed transitions to
the common plan and nonce. Rebuild and topology assertions come from the exact
complete M78 records at those indices. This avoids treating phase-sensitive
live counter reads as authoritative evidence.

Event indices and instrumented spans are descriptive runtime evidence only.
The two canonical replicas observed transition indices `517/739/1013/1208`
and `389/527/682/842`; their instrumented spans were
`4000/7100/4000/6500 ns` and `5200/4500/5900/4900 ns`. M86 does not attribute
cost or performance direction.

Nonclaims: arbitrary additions, more than two generations, different or
multiple cells, concurrent mutation, stale client block-entity cleanup,
merge/repacking policy, persistence, uninstrumented/additive cost, causal or
inferential performance claims, pixels, cross-machine generality, combat, or
historical lag reproduction.
