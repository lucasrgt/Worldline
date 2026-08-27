package worldline.test;

import java.nio.file.Path;

/** Immutable request passed to a runtime-specific test provider. */
public final class TestRuntimeRequest {
    private final long seed;
    private final Path worldPath;
    private final Path modPath;
    private final String testPath;

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath) {
        this(seed, worldPath, modPath, null);
    }

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath, String testPath) {
        if (worldPath == null) throw new NullPointerException("worldPath");
        if (testPath != null && testPath.trim().isEmpty()) {
            throw new IllegalArgumentException("testPath is blank");
        }
        this.seed = seed; this.worldPath = worldPath; this.modPath = modPath;
        this.testPath = testPath;
    }

    public long seed() { return seed; }
    public Path worldPath() { return worldPath; }
    public Path modPath() { return modPath; }
    /** Fully qualified TestKit path, or null for direct provider callers. */
    public String testPath() { return testPath; }
}
