package worldline.aero;

import net.minecraft.client.render.chunk.ChunkBuilder;

/** Hashes the exact Tessellator vertex stream produced by each chunk rebuild. */
public final class WorldlineChunkGeometry {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.chunkGeometry.enabled");
    private static final long OFFSET = 0xcbf29ce484222325L;
    private static final long PRIME = 0x100000001b3L;
    private static boolean active;
    private static int x, y, z, vertices;
    private static long hash;

    private WorldlineChunkGeometry() {}

    public static void begin(ChunkBuilder chunk) {
        if (!ENABLED) return;
        active = true; x = chunk.x; y = chunk.y; z = chunk.z; vertices = 0; hash = OFFSET;
        mix(x); mix(y); mix(z);
    }

    public static void vertex(double x, double y, double z, double u, double v,
            int color, int normal, int flags) {
        if (!active) return;
        mix(Double.doubleToLongBits(x)); mix(Double.doubleToLongBits(y));
        mix(Double.doubleToLongBits(z)); mix(Double.doubleToLongBits(u));
        mix(Double.doubleToLongBits(v)); mix(color); mix(normal); mix(flags); vertices++;
    }

    public static void end(ChunkBuilder chunk) {
        if (!active) return;
        active = false;
        System.out.println("[WorldlineChunkGeometry] x=" + x + " y=" + y + " z=" + z
                + " vertices=" + vertices + " hash=" + Long.toHexString(hash)
                + " layer0=" + chunk.renderLayerEmpty[0]
                + " layer1=" + chunk.renderLayerEmpty[1]);
    }

    private static void mix(long value) {
        for (int shift = 0; shift < 64; shift += 8) {
            hash ^= value >>> shift & 255L; hash *= PRIME;
        }
    }
}
