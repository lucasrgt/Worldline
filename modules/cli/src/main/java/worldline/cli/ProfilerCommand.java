package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerBudgetPolicy;
import worldline.profiling.ProfilerComparison;
import worldline.profiling.ProfilerExport;
import worldline.profiling.ProfilerMetric;
import worldline.profiling.ProfilerRegistry;
import worldline.profiling.ProfilerRun;
import worldline.profiling.ProfilerRunCodec;
import worldline.profiling.ProfilerSession;
import worldline.profiling.ProfilerSummary;

/** Inspects, exports, and compares canonical Worldline Profiler captures. */
final class ProfilerCommand {
    private ProfilerCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) throws IOException {
        if (arguments.length == 2 && "self-check".equals(arguments[1]))
            return selfCheck(output);
        if (arguments.length == 3 && "inspect".equals(arguments[1]))
            return inspect(Paths.get(arguments[2]), output);
        if (arguments.length == 5 && "export".equals(arguments[1]))
            return export(arguments[2], Paths.get(arguments[3]), Paths.get(arguments[4]), output);
        if ((arguments.length == 6 || arguments.length == 8)
                && "compare".equals(arguments[1])) return compare(arguments, output);
        return usage(error);
    }

    private static int inspect(Path artifact, PrintStream output) throws IOException {
        ProfilerRun run = ProfilerArtifacts.read(artifact);
        ProfilerSummary summary = new ProfilerSummary(run);
        output.println("WORLDLINE_PROFILER_INSPECT=PASS");
        output.println("mode=" + run.mode().name().toLowerCase(Locale.ROOT));
        output.println("frames=" + summary.frames());
        output.println("metrics=" + run.schema().size());
        for (ProfilerMetric metric : run.schema().metrics()) {
            String prefix = "metric." + metric.name();
            output.println(prefix + ".owner=" + metric.owner());
            output.println(prefix + ".category="
                    + metric.category().name().toLowerCase(Locale.ROOT));
            output.println(prefix + ".mean=" + summary.mean(metric.name()));
            output.println(prefix + ".p95=" + summary.percentile(metric.name(), 95, 100));
            output.println(prefix + ".max=" + summary.maximum(metric.name()));
        }
        return 0;
    }

    private static int selfCheck(PrintStream output) {
        ProfilerRun baseline = diagnosticRun(1L), candidate = diagnosticRun(2L);
        byte[] encoded = ProfilerRunCodec.encode(baseline);
        boolean roundTrip = Arrays.equals(encoded,
                ProfilerRunCodec.encode(ProfilerRunCodec.decode(encoded)));
        ProfilerSummary summary = new ProfilerSummary(baseline);
        ProfilerBudgetPolicy policy = new ProfilerBudgetPolicy(Collections.singletonList(
                ProfilerBudgetPolicy.Rule.of("frame.wall.nanos",
                        ProfilerBudgetPolicy.Statistic.P95, 35L,
                        ProfilerBudgetPolicy.Severity.CRITICAL)));
        ProfilerComparison.Result comparison = new ProfilerComparison(baseline, candidate)
                .requireMatchingTags("driver.id").compare("frame.wall.nanos",
                        ProfilerBudgetPolicy.Statistic.P95, 0L, 0);
        output.println("WORLDLINE_PROFILER_SELF_CHECK=PASS");
        output.println("schema.metrics=" + baseline.schema().size());
        output.println("frames=" + summary.frames());
        output.println("frame.p95.nanos=" + summary.percentile("frame.wall.nanos", 95, 100));
        output.println("budget.findings=" + policy.evaluate(baseline).size());
        output.println("comparison=" + comparison.verdict());
        output.println("artifact.roundtrip=" + roundTrip);
        output.println("json=" + ProfilerExport.json(baseline).startsWith("{\"schema\":1"));
        output.println("openmetrics=" + ProfilerExport.openMetrics(baseline).endsWith("# EOF\n"));
        return 0;
    }

    private static ProfilerRun diagnosticRun(long scale) {
        ProfilerMetric extension = worldline.profiling.WorldlineProfilerMetrics.extensionCounter(
                "mod.diagnostic.events.count", "diagnostic");
        ProfilerRegistry registry = ProfilerRegistry.builder()
                .support("frame.wall.nanos", "client.tick.total.nanos")
                .extension(extension).build();
        ProfilerSession session = new ProfilerSession(registry, 3);
        ProfilerRegistry.Handle tick = registry.require("client.tick.total.nanos");
        ProfilerRegistry.Handle events = registry.require(extension.name());
        long[] walls = {20L, 40L, 30L};
        for (int frame = 0; frame < walls.length; frame++) {
            long start = 100L + frame * 100L;
            session.beginFrame(10L + frame, start);
            session.add(tick, (5L + frame) * scale); session.add(events, frame + 1L);
            session.endFrame(start + walls[frame] * scale);
        }
        return session.seal(ProfilerRun.Mode.STEADY, 1_000L, 2_000L,
                Collections.singletonMap("driver.id", "diagnostic"));
    }

    private static int export(String format, Path input, Path target, PrintStream output)
            throws IOException {
        ProfilerRun run = ProfilerArtifacts.read(input);
        String text;
        if ("json".equals(format)) text = ProfilerExport.json(run) + "\n";
        else if ("openmetrics".equals(format)) text = ProfilerExport.openMetrics(run);
        else throw new IllegalArgumentException("unknown profiler export format: " + format);
        Files.write(target, text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        output.println("WORLDLINE_PROFILER_EXPORT=PASS");
        output.println("format=" + format); output.println("file=" + target);
        return 0;
    }

    private static int compare(String[] arguments, PrintStream output) throws IOException {
        String metric = arguments[2];
        ProfilerBudgetPolicy.Statistic statistic = ProfilerBudgetPolicy.Statistic.valueOf(
                arguments[3].toUpperCase(Locale.ROOT));
        ProfilerRun baseline = ProfilerArtifacts.read(Paths.get(arguments[4]));
        ProfilerRun candidate = ProfilerArtifacts.read(Paths.get(arguments[5]));
        long absoluteNoise = arguments.length == 8 ? nonnegative(arguments[6]) : 0L;
        int relativeNoise = arguments.length == 8 ? ppm(arguments[7]) : 0;
        ProfilerComparison.Result result = new ProfilerComparison(baseline, candidate)
                .requireMatchingTags("runtime.version", "driver.id", "scenario.id",
                        "machine.id", "jvm.version", "warmup.id")
                .compare(metric, statistic, absoluteNoise, relativeNoise);
        output.println("WORLDLINE_PROFILER_COMPARE=" + result.verdict());
        output.println("metric=" + result.metric()); output.println("statistic=" + result.statistic());
        output.println("baseline=" + result.baseline()); output.println("candidate=" + result.candidate());
        output.println("delta=" + result.delta()); output.println("relative.ppm=" + result.relativePpm());
        output.println("noise=" + result.noise());
        return result.verdict() == ProfilerComparison.Verdict.REGRESSION ? 3 : 0;
    }

    private static long nonnegative(String value) {
        long parsed = Long.parseLong(value);
        if (parsed < 0L) throw new IllegalArgumentException("negative profiler noise");
        return parsed;
    }
    private static int ppm(String value) {
        int parsed = Integer.parseInt(value);
        if (parsed < 0 || parsed > 1_000_000)
            throw new IllegalArgumentException("invalid profiler relative noise");
        return parsed;
    }
    private static int usage(PrintStream error) {
        error.println("usage: worldline profiler inspect <capture.wlpr>");
        error.println("   or: worldline profiler self-check");
        error.println("   or: worldline profiler export <json|openmetrics> <capture.wlpr> <output>");
        error.println("   or: worldline profiler compare <metric> <mean|p95|p99|max>"
                + " <baseline.wlpr> <candidate.wlpr> [absolute-noise relative-noise-ppm]");
        return 2;
    }
}
