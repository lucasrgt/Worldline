package worldline.testkit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import worldline.test.TestRuntimeProvider;

/** Validated options for one deterministic runner invocation. */
public final class RunnerOptions {
    TestRuntimeProvider provider;
    Path modPath, worldPath = Paths.get("worldline-test-world");
    Path artifacts = Paths.get(".worldline/test-results"), snapshots = Paths.get("__snapshots__");
    Path runtimeLock = Paths.get(".worldline/official-runtime.lock");
    long seed = 173L, timeout = 5_000L;
    int retry, bail, line, minimizeBudget = 64;
    boolean ci, allowOnly, updateSnapshots, minimize, shuffle, passWithNoTests, unicode = true;
    boolean timeoutOverride;
    Pattern namePattern, filePattern;
    String tag;

    public RunnerOptions provider(TestRuntimeProvider value) { provider = value; return this; }
    public RunnerOptions mod(Path value) { modPath = value; return this; }
    public RunnerOptions world(Path value) { worldPath = required(value, "world"); return this; }
    public RunnerOptions artifacts(Path value) { artifacts = required(value, "artifacts"); return this; }
    public RunnerOptions snapshots(Path value) { snapshots = required(value, "snapshots"); return this; }
    public RunnerOptions runtimeLock(Path value) { runtimeLock = required(value, "runtime lock"); return this; }
    public RunnerOptions seed(long value) { seed = value; return this; }
    public RunnerOptions timeout(long value) {
        timeout = bounded(value, 1, 3_600_000L, "timeout"); timeoutOverride = true; return this;
    }
    public RunnerOptions retry(int value) { retry = (int) bounded(value, 0, 10, "retry"); return this; }
    public RunnerOptions bail(int value) { bail = (int) bounded(value, 0, 1_000_000, "bail"); return this; }
    public RunnerOptions line(int value) { line = (int) bounded(value, 0, Integer.MAX_VALUE, "line"); return this; }
    public RunnerOptions minimizeBudget(int value) {
        minimizeBudget = (int) bounded(value, 1, 10_000, "minimize budget"); return this;
    }
    public RunnerOptions ci(boolean value) { ci = value; return this; }
    public RunnerOptions allowOnly(boolean value) { allowOnly = value; return this; }
    public RunnerOptions updateSnapshots(boolean value) { updateSnapshots = value; return this; }
    public RunnerOptions minimize(boolean value) { minimize = value; return this; }
    public RunnerOptions shuffle(boolean value) { shuffle = value; return this; }
    public RunnerOptions passWithNoTests(boolean value) { passWithNoTests = value; return this; }
    public RunnerOptions unicode(boolean value) { unicode = value; return this; }
    public RunnerOptions name(Pattern value) { namePattern = value; return this; }
    public RunnerOptions file(Pattern value) { filePattern = value; return this; }
    public RunnerOptions tag(String value) { tag = value; return this; }

    private static Path required(Path value, String role) {
        if (value == null) throw new NullPointerException(role); return value;
    }
    private static long bounded(long value, long min, long max, String role) {
        if (value < min || value > max) throw new IllegalArgumentException("invalid " + role); return value;
    }
}
