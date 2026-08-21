/** Official-name isolated vertical water column in three floor variants. */
final class OracleMemoryChunkLoader implements an {
    static final int OPEN = 0;
    static final int BLOCKED = 1;
    static final int SHAFT = 2;
    private static final int HEIGHT = 128;
    private final int fixture;
    OracleMemoryChunkLoader(int fixture) { this.fixture = fixture; }

    public hi a(dj world, int chunkX, int chunkZ) {
        byte[] blocks = new byte[16 * HEIGHT * 16];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            for (int y = 0; y <= 64; y++) set(blocks, x, y, z, y == 0 ? na.A.bn : na.u.bn);
        }
        if (chunkX == 0 && chunkZ == 0) buildColumn(blocks);
        hi chunk = new hi(world, blocks, chunkX, chunkZ);
        chunk.n = true;
        chunk.p = true;
        chunk.b();
        return chunk;
    }

    private void buildColumn(byte[] blocks) {
        for (int y = 61; y <= 68; y++) for (int x = 7; x <= 9; x++) {
            for (int z = 7; z <= 9; z++) if (x != 8 || z != 8) set(blocks, x, y, z, na.u.bn);
        }
        if (fixture == SHAFT) for (int y = 61; y <= 67; y++) set(blocks, 8, y, 8, 0);
        if (fixture == BLOCKED) set(blocks, 8, 67, 8, na.u.bn);
    }

    private static void set(byte[] blocks, int x, int y, int z, int id) {
        blocks[x << 11 | z << 7 | y] = (byte) id;
    }
    public void a(dj world, hi chunk) { }
    public void b(dj world, hi chunk) { }
    public void a() { }
    public void b() { }
}
