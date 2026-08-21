package worldline.profiling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import worldline.minimization.Scenario;
import worldline.trace.CanonicalStateDocument;

/** Proves profile aggregates, budget gating, and canonical report framing. */
public final class ProfilingTest {
    private ProfilingTest() {}

    public static void main(String[] arguments) throws Exception {
        aggregatesAndFailClosed();
        budgetGating();
        reportFraming();
        System.out.println("ProfilingTest passed");
    }

    private static void aggregatesAndFailClosed() {
        TickProfile profile = TickProfile.of(
                new long[] {50L, 10L, 40L, 20L, 30L},
                new long[] {5L, 0L, 4L, 2L, 3L});
        require(profile.ticks() == 5 && profile.total() == 150L && profile.mean() == 30L,
                "total or mean drifted");
        require(profile.min() == 10L && profile.max() == 50L, "min or max drifted");
        require(profile.median() == 30L && profile.p95() == 50L, "rank aggregates drifted");
        require(profile.modTotal() == 14L && profile.modSharePercent() == 9,
                "mod aggregates drifted");
        rejects(() -> TickProfile.of(new long[] {1L}, new long[] {}));
        rejects(() -> TickProfile.of(new long[] {}, new long[] {}));
        rejects(() -> TickProfile.of(new long[] {0L}, new long[] {0L}));
        rejects(() -> TickProfile.of(new long[] {5L}, new long[] {6L}));
        rejects(() -> TickProfile.of(null, null));
    }

    private static void budgetGating() throws Exception {
        Path budget = Files.createTempFile("worldline-budget", ".properties");
        Path loose = Files.createTempFile("worldline-loose", ".properties");
        try {
            Files.write(budget, ("tick.total.nanos.max=100\n"
                    + "tick.mean.nanos.max=25\ntick.median.nanos.max=29\n"
                    + "tick.p95.nanos.max=49\ntick.max.nanos.max=51\n"
                    + "mod.share.percent.max=9\n").getBytes("UTF-8"));
            ProfileBudget tight = ProfileBudget.parse(budget);
            TickProfile profile = TickProfile.of(
                    new long[] {50L, 10L, 40L, 20L, 30L},
                    new long[] {5L, 0L, 4L, 2L, 3L});
            List<String> violations = tight.violations(profile);
            require(violations.size() == 4, "unexpected violation count: " + violations);
            require(violations.contains("tick.total.nanos=150>100")
                    && violations.contains("tick.mean.nanos=30>25")
                    && violations.contains("tick.median.nanos=30>29")
                    && violations.contains("tick.p95.nanos=50>49"), "violation text drifted");
            Files.write(loose, "tick.total.nanos.max=999999999\n".getBytes("UTF-8"));
            require(ProfileBudget.parse(loose).violations(profile).isEmpty(),
                    "loose budget reported violations");
            Path finalBudget = budget;
            rejects(() -> {
                try { ProfileBudget.parse(finalBudget).violations(null); }
                catch (java.io.IOException error) { throw new IllegalStateException(error); }
            });
        } finally {
            Files.deleteIfExists(budget);
            Files.deleteIfExists(loose);
        }
    }

    private static void reportFraming() {
        Scenario scenario = Scenario.of(java.util.Arrays.asList(
                "observe:before", "tick:3", "observe:after"));
        TickProfiledRun run = new TickProfiledRun(
                TickProfile.of(new long[] {10L, 20L, 30L}, new long[] {0L, 1L, 2L}),
                CanonicalStateDocument.parse("v2|seed=7|schema=x|t0=0"));
        ProfileReport report = ProfileReport.of(scenario, 7L, run);
        String text = new String(report.bytes(), java.nio.charset.StandardCharsets.UTF_8);
        require(text.startsWith("WORLDLINE-PROFILE/1\nseed=7\nscenario.sha256="
                + scenario.sha256() + "\nsteps=3\nticks=3\n"), "report header drifted");
        require(text.contains("sample.0=10,0") && text.contains("sample.2=30,2"),
                "sample lines drifted");
        require(text.contains("tick.mean.nanos=20") && text.contains("tick.p95.nanos=30")
                && text.contains("mod.share.percent=5"), "aggregate lines drifted");
        require(text.endsWith("sha256=" + report.sha256() + "\n"), "checksum line drifted");
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid input was accepted"); }
        catch (Exception expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
