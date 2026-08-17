# M33 Chunk Traversal Lifecycle

Two fresh protocol-14 clients accumulate a multi-chunk cache, rise eight blocks
under `allow-flight`, move east in quarter-block steps until crossing exactly
two chunk boundaries, and sustain the session again. The gate compares immutable before/after views and requires at
least one Packet50-qualified removal and one new decoded chunk.

Before movement, an adapter fixture proves unreserved MapChunk data still fails
closed. The first deliberate move enables bounded implicit edge loads, matching
the official server's observed Packet51 behavior while retaining the 256-chunk
ceiling.

Both cache topologies are rendered on the same 12x12 native grid through mapped
Minecraft `Tessellator`, LWJGL, and an offscreen Pbuffer. A removed chunk's
pixel must clear, an added chunk's pixel must appear, and the RGBA frame hash
must change. Movement alone is not accepted without the lifecycle transition.
