package worldline.test;

import java.nio.file.Path;

/** Immutable per-test runtime configuration created by {@link TestCaseBuilder}. */
public final class TestCase implements TestBody {
    private final String runtimeId;
    private final Long seed;
    private final Path mod, world;
    private final TestBody body;

    TestCase(String runtimeId, Long seed, Path mod, Path world, TestBody body) {
        this.runtimeId = runtimeId; this.seed = seed; this.mod = mod; this.world = world; this.body = body;
    }
    public String runtimeId() { return runtimeId; }
    public Long configuredSeed() { return seed; }
    public Path mod() { return mod; }
    public Path world() { return world; }
    public TestBody body() { return body; }
    @Override public void run(TestContext context) throws Exception { body.run(context); }
}
