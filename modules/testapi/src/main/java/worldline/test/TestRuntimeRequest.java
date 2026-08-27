package worldline.test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable request passed to a runtime-specific test provider. */
public final class TestRuntimeRequest {
    private final long seed;
    private final Path worldPath;
    private final Path modPath;
    private final String testPath;
    private final Map<String, String> runtimeOptions;

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath) {
        this(seed, worldPath, modPath, null);
    }

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath, String testPath) {
        this(seed, worldPath, modPath, testPath, Collections.<String, String>emptyMap());
    }

    public TestRuntimeRequest(long seed, Path worldPath, Path modPath, String testPath,
            Map<String, String> runtimeOptions) {
        if (worldPath == null) throw new NullPointerException("worldPath");
        if (runtimeOptions == null) throw new NullPointerException("runtimeOptions");
        if (testPath != null && testPath.trim().isEmpty()) {
            throw new IllegalArgumentException("testPath is blank");
        }
        if (runtimeOptions.size() > 32) {
            throw new IllegalArgumentException("too many runtime options");
        }
        for (Map.Entry<String, String> option : runtimeOptions.entrySet()) {
            validateOption(option.getKey(), option.getValue());
        }
        this.seed = seed; this.worldPath = worldPath; this.modPath = modPath;
        this.testPath = testPath;
        this.runtimeOptions = Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(runtimeOptions));
    }

    public long seed() { return seed; }
    public Path worldPath() { return worldPath; }
    public Path modPath() { return modPath; }
    /** Fully qualified TestKit path, or null for direct provider callers. */
    public String testPath() { return testPath; }
    public Map<String, String> runtimeOptions() { return runtimeOptions; }
    public String runtimeOption(String key) { return runtimeOptions.get(key); }

    private static void validateOption(String key, String value) {
        if (key == null || !key.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("invalid runtime option key");
        }
        if (value == null || value.isEmpty() || value.length() > 256) {
            throw new IllegalArgumentException("invalid runtime option value");
        }
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) < 0x20 || value.charAt(index) > 0x7e) {
                throw new IllegalArgumentException("runtime option value must be visible ASCII");
            }
        }
    }
}
