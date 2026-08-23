# M72 Aero server content boundary

Status: GO in Worldline v1.60.0.

M72 replaces M71's client-only/vanilla-content scene with the smallest honest
server-authored Aero-content fixture. The dedicated runtime is derived from the
pinned StationAPI `test-bare` host. It loads Fabric, StationAPI, and one
Worldline-owned universal content mod; it does not load Aero. The graphical
client loads the byte-equivalent content sources and pinned Aero 3.0.0.

The common source closure registers one custom block and a plain block entity
without importing Aero, Minecraft client classes, or LWJGL. A server-only
entrypoint places exactly one instance near the authenticated player after 80
ticks. Its positive nonce exists only as a server JVM property and differs in
the two qualification runs.

Vanilla/StationAPI block updates alone do not establish this block entity's
presentation state. M72 therefore defines an explicit, identifier-scoped
`MessagePacket` carrying exactly `x,y,z,nonce`. The client validates its shape,
buffers early delivery, requires the matching remote block, creates or validates
the expected block entity, and applies the nonce once. Conflicting coordinates,
types, state, and replay payloads fail closed; an exact
duplicate is idempotent.

The client-only renderer refuses to qualify an unset nonce. After exact state is
present it queues the original OBJ through the real pinned Aero at-rest renderer.
The oracle records successful return, then requires twenty subsequent renderer
TAIL completions and a strictly parsed Aero row with visible chunks. Aero pulse
counters are cumulative-window observations and may be zero after the separate
renderer-return marker has already proved this content path.

This is not a general StationAPI synchronization API. It proves one exact
server-authored block/entity and one explicit M72 state message reach and are
rendered by one real Aero client, twice. It makes no performance, pixel,
historical-lag, combat, persistence, or generic compatibility claim.
