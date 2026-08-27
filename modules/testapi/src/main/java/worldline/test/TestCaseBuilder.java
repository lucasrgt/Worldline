package worldline.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** Fluent Java 8 configuration for one test runtime. */
public final class TestCaseBuilder {
    private String runtimeId;
    private Long seed;
    private Path mod, world;
    private final Map<String, String> runtimeOptions = new LinkedHashMap<String, String>();
    private boolean built;

    TestCaseBuilder() {}

    public TestCaseBuilder runtime(String value) {
        mutable(); require(runtimeId == null, "runtime is already configured");
        require(value != null && value.matches("[A-Za-z0-9._-]{1,64}"), "invalid runtime");
        runtimeId = value; return this;
    }
    public TestCaseBuilder seed(long value) {
        mutable(); require(seed == null, "seed is already configured"); seed = value; return this;
    }
    public TestCaseBuilder mod(String value) { return mod(path(value, "mod")); }
    public TestCaseBuilder mod(Path value) {
        mutable(); require(mod == null, "mod is already configured"); mod = required(value, "mod"); return this;
    }
    public TestCaseBuilder world(String value) { return world(path(value, "world")); }
    public TestCaseBuilder world(Path value) {
        mutable(); require(world == null, "world is already configured");
        world = required(value, "world"); return this;
    }
    public TestCaseBuilder runtimeOption(String key, String value) {
        mutable(); require(runtimeOptions.size() < 32, "too many runtime options");
        require(key != null && key.matches("[a-z0-9][a-z0-9._-]{0,63}"),
                "invalid runtime option key");
        require(value != null && !value.isEmpty() && value.length() <= 256,
                "invalid runtime option value");
        for (int index = 0; index < value.length(); index++) require(
                value.charAt(index) >= 0x20 && value.charAt(index) <= 0x7e,
                "runtime option value must be visible ASCII");
        require(!runtimeOptions.containsKey(key), "runtime option is already configured");
        runtimeOptions.put(key, value); return this;
    }
    public TestCase run(TestBody body) {
        mutable(); if (body == null) throw new NullPointerException("body"); built = true;
        return new TestCase(runtimeId, seed, mod, world, runtimeOptions, body);
    }
    private void mutable() { if (built) throw new IllegalStateException("test case is already built"); }
    private static Path path(String value, String role) {
        require(value != null && !value.trim().isEmpty(), role + " path is blank");
        return Paths.get(value.trim());
    }
    private static Path required(Path value, String role) {
        if (value == null) throw new NullPointerException(role); return value;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
