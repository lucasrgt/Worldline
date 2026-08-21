# M32 Sustained Remote Terrain Render

M32 extends the incremental multiplayer session with `sustainTicks`. The
protocol adapter pauses 50 milliseconds after each play update and pumps a
bounded set of currently available inbound packets between ticks. For each 20-tick interval,
19 Packet10 flying updates are followed by one unchanged Packet13 pose update.
A byte-level fixture freezes that 38/2 cadence across 40 ticks.

Each live scenario connects to an unmodified official Beta 1.7.3 server and
accumulates a multi-chunk immutable cache. A smoke-only renderer selects an 8x8
vertical block slice around a reachable target, maps every non-air `BlockState`
to geometry, and submits it through mapped Minecraft `Tessellator`, native
LWJGL, and an offscreen Pbuffer.

Outbound dig intent remains non-authoritative. The second frame is accepted
only after Packet53 changes the exact cache coordinate to `0:0`, the matching
pixel becomes the fixed background color, and the full RGBA frame hash changes.

## Non-claims

M32 does not instantiate Minecraft's complete terrain renderer, textures,
lighting, entities, background network thread, collision simulation, or
external server tick control. Its renderer is an exact cache-to-native-geometry
composition oracle, not graphical multiplayer parity.
