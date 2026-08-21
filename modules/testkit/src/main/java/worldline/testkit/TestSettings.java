package worldline.testkit;

import java.nio.file.Path;
import worldline.test.TestBody;
import worldline.test.TestCase;

/** Effective immutable runtime settings for one collected test. */
final class TestSettings {
    final long seed;
    final Path mod, world;
    final String runtimeId;
    final boolean requiresRuntime;

    private TestSettings(long seed, Path mod, Path world, String runtimeId, boolean requiresRuntime) {
        this.seed = seed; this.mod = mod; this.world = world; this.runtimeId = runtimeId;
        this.requiresRuntime = requiresRuntime;
    }
    static TestSettings resolve(TestBody body, RunnerOptions options) {
        if (!(body instanceof TestCase)) {
            return new TestSettings(options.seed, options.modPath, options.worldPath, null,
                    options.modPath != null);
        }
        TestCase value = (TestCase) body;
        return new TestSettings(value.configuredSeed() == null ? options.seed : value.configuredSeed(),
                value.mod() == null ? options.modPath : value.mod(),
                value.world() == null ? options.worldPath : value.world(), value.runtimeId(),
                value.runtimeId() != null || value.mod() != null || value.world() != null
                        || options.modPath != null);
    }
    void validate(RunnerOptions options) {
        if (options.provider == null && requiresRuntime) throw new IllegalStateException(
                "WLTEST E2201: test requires a runtime provider"
                        + (runtimeId == null ? "" : " for " + runtimeId));
        if (runtimeId == null) return;
        if (!runtimeId.equals(options.provider.runtimeId())) throw new IllegalStateException(
                "WLTEST E2202: test requires runtime " + runtimeId + " but provider is "
                        + options.provider.runtimeId());
    }
}
