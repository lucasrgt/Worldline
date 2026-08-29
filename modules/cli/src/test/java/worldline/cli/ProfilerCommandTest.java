package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import worldline.profiling.FrameCensus;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerRun;
import worldline.profiling.ProfilerSchema;
import worldline.profiling.WorldlineProfilerMetrics;

/** Proves CLI inspection, dashboard export, and A/B verdict exit status. */
public final class ProfilerCommandTest {
    private ProfilerCommandTest() {}

    public static void main(String[] arguments) throws Exception {
        Path directory = Files.createTempDirectory("worldline-profiler-cli");
        Path baseline = directory.resolve("baseline.wlpr");
        Path candidate = directory.resolve("candidate.wlpr");
        Path json = directory.resolve("report.json");
        try {
            ProfilerArtifacts.write(baseline, run(10L));
            ProfilerArtifacts.write(candidate, run(20L));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int status = WorldlineCli.run(new String[] {"profiler", "self-check"},
                    new PrintStream(out), new PrintStream(err));
            require(status == 0 && out.toString("UTF-8")
                    .contains("WORLDLINE_PROFILER_SELF_CHECK=PASS"),
                    "profiler CLI self-check failed");
            out.reset();
            status = WorldlineCli.run(new String[] {"profiler", "inspect",
                    baseline.toString()}, new PrintStream(out), new PrintStream(err));
            require(status == 0 && err.size() == 0
                    && out.toString("UTF-8").contains("WORLDLINE_PROFILER_INSPECT=PASS"),
                    "profiler CLI inspect failed");
            out.reset();
            status = WorldlineCli.run(new String[] {"profiler", "export", "json",
                    baseline.toString(), json.toString()}, new PrintStream(out), new PrintStream(err));
            require(status == 0 && Files.readString(json).startsWith("{\"schema\":1"),
                    "profiler CLI export failed");
            out.reset();
            status = WorldlineCli.run(new String[] {"profiler", "compare",
                    WorldlineProfilerMetrics.FRAME_WALL, "p95", baseline.toString(),
                    candidate.toString()}, new PrintStream(out), new PrintStream(err));
            require(status == 3 && out.toString("UTF-8")
                    .contains("WORLDLINE_PROFILER_COMPARE=REGRESSION"),
                    "profiler CLI regression verdict failed");
        } finally {
            Files.deleteIfExists(json); Files.deleteIfExists(candidate);
            Files.deleteIfExists(baseline); Files.deleteIfExists(directory);
        }
        System.out.println("ProfilerCommandTest passed");
    }

    private static ProfilerRun run(long wall) {
        ProfilerSchema schema = ProfilerSchema.of(Collections.singletonList(
                WorldlineProfilerMetrics.standardSchema().metric(
                        WorldlineProfilerMetrics.standardSchema().index(
                                WorldlineProfilerMetrics.FRAME_WALL))));
        return ProfilerRun.of(schema, FrameCensus.of(schema.metricNames(),
                new long[][] {{0L, 1L, wall}}), ProfilerRun.Mode.STEADY,
                1L, 2L, Collections.<String, String>emptyMap());
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
