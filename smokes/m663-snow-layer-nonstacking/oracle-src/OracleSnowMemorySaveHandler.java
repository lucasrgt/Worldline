import java.io.File;
import java.util.List;

/** Official-name wet or dry WorldInfo with in-memory chunks. */
@SuppressWarnings("rawtypes")
final class OracleSnowMemorySaveHandler implements om, fl {
    private final ct info;

    OracleSnowMemorySaveHandler(long seed, String name, boolean snowfall) {
        info = new ct(seed, name);
        info.b(snowfall);
        info.c(1000);
    }

    @Override
    public ct c() {
        return info;
    }

    @Override
    public void b() {
    }

    @Override
    public an a(os provider) {
        return new OracleSnowMemoryChunkLoader();
    }

    @Override
    public void a(ct worldInfo, List players) {
    }

    @Override
    public void a(ct worldInfo) {
    }

    @Override
    public fl d() {
        return this;
    }

    @Override
    public void e() {
    }

    @Override
    public File b(String name) {
        return null;
    }

    @Override
    public void a(em player) {
    }

    @Override
    public void b(em player) {
    }
}
