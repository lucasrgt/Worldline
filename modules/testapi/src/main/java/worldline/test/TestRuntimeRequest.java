package worldline.test;

import java.nio.file.Path;

/** Immutable request passed to a runtime-specific test provider. */
public final class TestRuntimeRequest {
    private final long seed;
    private final Path worldPath;
    private final Path modPath;

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath) {
        if (worldPath == null) throw new NullPointerException("worldPath");
        this.seed = seed; this.worldPath = worldPath; this.modPath = modPath;
    }

    public long seed() { return seed; }
    public Path worldPath() { return worldPath; }
    public Path modPath() { return modPath; }
}
