/** Official-name deterministic superflat chunk source. */
final class OracleRedstoneOreChunkLoader implements an {
    public hi a(dj world, int chunkX, int chunkZ) {
        byte[] blocks = new byte[16 * 128 * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 64; y++)
                    blocks[x << 11 | z << 7 | y] = (byte) (y == 0 ? na.A.bn : na.u.bn);
            }
        }
        hi chunk = new hi(world, blocks, chunkX, chunkZ);
        chunk.n = true;
        chunk.p = true;
        chunk.b();
        return chunk;
    }
    public void a(dj world, hi chunk) {
    }
    public void b(dj world, hi chunk) {
    }
    public void a() {
    }
    public void b() {
    }
}
