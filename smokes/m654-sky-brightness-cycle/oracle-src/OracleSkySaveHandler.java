import java.io.File;
import java.util.List;

/** Official-name in-memory save boundary that rejects terrain access. */
@SuppressWarnings("rawtypes")
final class OracleSkySaveHandler implements om, fl {
    private final ct info;
    private final an chunks = new EmptyChunks();

    OracleSkySaveHandler(long seed, String name) {
        info = new ct(seed, name);
        info.a(0, 64, 0);
    }

    public ct c() {
        return info;
    }

    public void b() { }

    public an a(os provider) {
        return chunks;
    }

    public void a(ct worldInfo, List players) { }

    public void a(ct worldInfo) { }

    public fl d() {
        return this;
    }

    public void e() { }

    public File b(String name) {
        return null;
    }

    public void a(em player) { }

    public void b(em player) { }

    private static final class EmptyChunks implements an {
        public hi a(dj world, int chunkX, int chunkZ) {
            throw new IllegalStateException("sky brightness oracle does not load chunks");
        }

        public void a(dj world, hi chunk) { }

        public void b(dj world, hi chunk) { }

        public void a() { }

        public void b() { }
    }
}
