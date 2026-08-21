# M31 Incremental Remote World

M31 extends the cached multiplayer session with neutral begin/finish block-break
intent and exact expected-block waiting. Sending intent never mutates the local
view. Only inbound server packets may replace a cached immutable snapshot.

The adapter decodes native Packet53 single-block changes and Packet52 packed
multi-block arrays. Coordinates follow mapped vanilla semantics; ID and metadata
replace only the addressed block while existing light planes remain intact.
Updates outside decoded chunks are consumed but do not create speculative data.

The fixture proves Packet52/53 byte layout and three exact changes. Each of two
live scenarios uses the official server at view distance three, explicitly
grants operator status, chooses a nearby breakable block from the server
snapshot, sends native dig intent, and waits until the
official server reports air at that exact coordinate.

## Non-claims

M31 does not predict mining, simulate tools/durability/drops, decode entities or
tile entities, sustain a full client tick loop, render terrain, or step the
server externally.
