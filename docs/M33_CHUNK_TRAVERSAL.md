# M33 Chunk Traversal Lifecycle

M33 composes the existing neutral movement and sustained-session contracts.
Each live client first accumulates a stable multi-chunk view, rises eight blocks
under explicit `allow-flight`, then moves east in steps of at most 0.25 blocks,
sustaining one protocol tick after every movement, until its neutral pose
crosses exactly two chunk boundaries. A final 40-tick
window receives the server's new view.

The official Beta 1.7.3 server sometimes streams a view-edge Packet51 without a
preceding Packet50 after movement. M33 therefore adds an explicit adapter mode:
before the first deliberate move, unreserved chunk data remains a hard error;
after movement, MapChunk may establish a bounded implicit edge load. The same
256-chunk ceiling applies, while Packet50 unload continues to evict immediately.
A byte-level fixture freezes this strict-to-moving transition.

The smoke compares immutable before/after cache keys and requires at least one
removed and one added chunk. Both topologies are then rendered on the same
12x12 grid through mapped Minecraft `Tessellator`, native LWJGL, and an
offscreen Pbuffer. The removed coordinate must change from loaded color to
background, the added coordinate must make the inverse transition, and the
RGBA hash must differ.

## Non-claims

M33 does not consume server Packet13 corrections into the neutral pose, model
collision or gravity, perform arbitrary pathfinding, instantiate Minecraft's
complete terrain renderer, or control server ticks.
