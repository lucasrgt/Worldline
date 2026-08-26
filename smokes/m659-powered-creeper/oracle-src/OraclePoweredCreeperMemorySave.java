import java.io.File;
import java.util.List;

/** Official-name in-memory world persistence for the powered-creeper oracle. */
@SuppressWarnings("rawtypes")
final class OraclePoweredCreeperMemorySave implements om, fl {
    private final ct info;
    private final Chunks chunks = new Chunks();

    OraclePoweredCreeperMemorySave(long seed, String name) {
        info = new ct(seed, name);
        info.a(8, 64, 8);
    }

    public ct c() { return info; }
    public void b() { }
    public an a(os provider) { return chunks; }
    public void a(ct worldInfo, List players) { }
    public void a(ct worldInfo) { }
    public fl d() { return this; }
    public void e() { }
    public File b(String name) { return null; }
    public void a(em player) { }
    public void b(em player) { }

    private static final class Chunks implements an {
        private static final int HEIGHT = 128;

        @Override public hi a(dj world, int chunkX, int chunkZ) {
            byte[] blocks = new byte[16 * HEIGHT * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y <= 64; y++) {
                        set(blocks, x, y, z, y == 0 ? na.A.bn : na.u.bn);
                    }
                }
            }
            hi chunk = new hi(world, blocks, chunkX, chunkZ);
            chunk.n = true;
            chunk.p = true;
            chunk.b();
            return chunk;
        }

        private static void set(byte[] blocks, int x, int y, int z, int id) {
            blocks[x << 11 | z << 7 | y] = (byte) id;
        }

        @Override public void a(dj world, hi chunk) { }
        @Override public void b(dj world, hi chunk) { }
        @Override public void a() { }
        @Override public void b() { }
    }
}
