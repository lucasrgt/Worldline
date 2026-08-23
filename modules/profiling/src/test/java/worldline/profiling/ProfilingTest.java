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
        frameClassification();
        completeFrameCensus();
        budgetGating();
        reportFraming();
        System.out.println("ProfilingTest passed");
    }

    private static void frameClassification() {
        long ms = 1_000_000L;
        FrameBreakdown save = FrameBreakdown.of(40 * ms, 20 * ms, 0L, 2 * ms, 0L, 3 * ms);
        require(save.classify(ms, 1, 4) == FrameBreakdown.HitchClass.SAVE,
                "save classification drifted");
        FrameBreakdown[] singles = {
            FrameBreakdown.of(40 * ms, 0L, 12 * ms, 0L, 0L, 0L),
            FrameBreakdown.of(40 * ms, 0L, 0L, 12 * ms, 0L, 0L),
            FrameBreakdown.of(40 * ms, 0L, 0L, 0L, 12 * ms, 0L),
            FrameBreakdown.of(40 * ms, 0L, 0L, 0L, 0L, 12 * ms)
        };
        FrameBreakdown.HitchClass[] expected = {
            FrameBreakdown.HitchClass.GC_RUNTIME, FrameBreakdown.HitchClass.CHUNK_WORK,
            FrameBreakdown.HitchClass.SUBJECT_WORK, FrameBreakdown.HitchClass.DISPLAY_PRESENT
        };
        for (int index = 0; index < singles.length; index++)
            require(singles[index].classify(ms, 1, 4) == expected[index],
                    "single-bucket classification drifted at " + index);
        FrameBreakdown mixed = FrameBreakdown.of(40 * ms, 12 * ms, 0L, 15 * ms, 0L, 0L);
        require(mixed.classify(ms, 1, 4) == FrameBreakdown.HitchClass.MIXED,
                "mixed classification drifted");
        FrameBreakdown unknown = FrameBreakdown.of(40 * ms, 0L, 0L, 2 * ms, 3 * ms, 1 * ms);
        require(unknown.classify(5 * ms, 1, 4) == FrameBreakdown.HitchClass.UNKNOWN,
                "unknown classification drifted");
        FrameBreakdown rounded = FrameBreakdown.of(10L, 3L, 0L, 0L, 0L, 0L);
        require(rounded.classify(0L, 1, 3) == FrameBreakdown.HitchClass.UNKNOWN,
                "rational ceiling drifted");
        require(save.frameNanos() == 40 * ms && save.saveNanos() == 20 * ms
                && save.gcNanos() == 0L && save.chunkNanos() == 2 * ms
                && save.subjectNanos() == 0L && save.displayNanos() == 3 * ms,
                "frame breakdown accessors drifted");
        rejects(() -> FrameBreakdown.of(0L, 0L, 0L, 0L, 0L, 0L));
        rejects(() -> FrameBreakdown.of(1L, -1L, 0L, 0L, 0L, 0L));
        rejects(() -> save.classify(-1L, 1, 4));
        rejects(() -> save.classify(0L, 5, 4));
    }

    private static void completeFrameCensus() {
        String[] metrics = {"frame.nanos", "save.nanos", "chunks.remaining"};
        long[][] rows = {{41L, 1_000L, 16L, 0L, 8L}, {42L, 1_017L, 17L, 3L, 0L}};
        FrameCensus census = FrameCensus.of(metrics, rows);
        require(census.frames() == 2 && census.metrics() == 3
                && census.sequence(1) == 42L && census.monotonicNanos(1) == 1_017L,
                "frame census identity drifted");
        require(census.value(1, "save.nanos") == 3L
                && java.util.Arrays.equals(census.metricNames(), metrics)
                && java.util.Arrays.equals(census.row(0), rows[0]),
                "frame census values drifted");
        byte[] encoded = FrameCensusCodec.encode(census);
        FrameCensus decoded = FrameCensusCodec.decode(encoded);
        require(java.util.Arrays.equals(encoded, FrameCensusCodec.encode(census))
                && decoded.frames() == 2 && decoded.value(1, "chunks.remaining") == 0L,
                "frame census codec drifted");
        byte[] corrupt = encoded.clone(); corrupt[12] ^= 1;
        rejects(() -> FrameCensusCodec.decode(corrupt));
        byte[] trailing = java.util.Arrays.copyOf(encoded, encoded.length + 1);
        rejects(() -> FrameCensusCodec.decode(trailing));
        rejects(() -> FrameCensusCodec.decode(new byte[8]));
        rows[0][2] = 999L; metrics[0] = "mutated";
        require(census.value(0, "frame.nanos") == 16L,
                "frame census retained caller storage");
        rejects(() -> FrameCensus.of(new String[] {}, new long[][] {{0L, 0L}}));
        rejects(() -> FrameCensus.of(new String[] {"Bad"}, new long[][] {{0L, 0L, 1L}}));
        rejects(() -> FrameCensus.of(new String[] {"x", "x"},
                new long[][] {{0L, 0L, 1L, 2L}}));
        rejects(() -> FrameCensus.of(new String[] {"x"}, new long[][] {{0L, 0L}}));
        rejects(() -> FrameCensus.of(new String[] {"x"},
                new long[][] {{0L, 2L, 1L}, {2L, 3L, 1L}}));
        rejects(() -> FrameCensus.of(new String[] {"x"},
                new long[][] {{0L, 2L, 1L}, {1L, 2L, 1L}}));
        rejects(() -> FrameCensus.of(new String[] {"x"}, new long[][] {{0L, 0L, -1L}}));
        rejects(() -> census.value(0, "missing"));
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
