# M114 causal water flow

Status: GO in Worldline v1.102.0.

M114 qualifies the first causal vanilla-fluid transition against the
unmodified official Beta 1.7.3 server. It starts from the fixed-seed M111 world
and finds naturally generated still water `9:0` at `(4,55,4)` above dirt
`3:0` at `(4,54,4)`.

A protocol-14 actor uses the already-qualified Packet14 begin/finish dig path.
Packet53 first proves that the dirt became air. After forty heartbeat ticks,
the actor's live cache and a second session's fresh Packet51 both expose
`9:8` in the opened cell. Across the complete chunk snapshot, exactly that one
block state changes; its ordered state-delta SHA-256 is
`33f402b3ec13c94b9dbba6028315449e5d84fc251c16206ed92d09748f9299b2`.

The fluid fixture also exposed an over-specialized inbound health path. The
internal tracker now accepts any valid local health decrease preceded by the
local hurt status; `awaitIncomingHit(expected)` still rejects a different
expected result. M66 was hardened independently with exact official-NBT item
seeds and bounded air-position heartbeats, and its original two-world
`20 -> 18` signature remains unchanged.

M114 is one downward still-water fixture. It does not claim generic flow
distance, lateral spread, source creation, lava, mixing, buckets, scheduled
tick timing, cross-chunk flow, entity pushing, drowning semantics, rendering,
alternate terrain, persistence across server restart, or a Worldline fluid
simulation.
