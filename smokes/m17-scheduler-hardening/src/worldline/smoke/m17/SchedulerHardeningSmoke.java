package worldline.smoke.m17;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import worldline.aero.AeroFrameOracle;

/** Qualifies the M17 scheduler across empty, dense, and moving workloads. */
public final class SchedulerHardeningSmoke {
    private SchedulerHardeningSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 4, "expected build, scenarios, frame limit, and budget");
        Path build = Paths.get(arguments[0]);
        List<String> scenarios = Arrays.asList(arguments[1].split(","));
        int limit = Integer.parseInt(arguments[2]); long budgetUs = Long.parseLong(arguments[3]);
        require(scenarios.equals(Arrays.asList("stationary-empty", "stationary-dense", "moving-dense")),
                "M17 scenario matrix drifted");
        int budgeted = 0, overshot = 0, visualDivergence = 0;
        long worstBaseline = 0, worstAdaptive = 0;
        for (String scenario : scenarios) {
            RunMetrics baseline = RunMetrics.load(build, scenario, "baseline", limit);
            RunMetrics adaptive = RunMetrics.load(build, scenario, "adaptive", limit);
            RunMetrics governor = RunMetrics.load(build, scenario, "governor", limit);
            print(scenario, "baseline", baseline, budgetUs);
            print(scenario, "governor", governor, budgetUs);
            print(scenario, "adaptive", adaptive, budgetUs);
            require(baseline.forced == 0 && baseline.falseReturns > 0
                    && baseline.falseReturns + baseline.trueReturns == baseline.calls
                    && baseline.calls > baseline.frames,
                    scenario + " vanilla retry behavior drifted");
            require(adaptive.forced == 0 && adaptive.calls == adaptive.frames
                    && adaptive.trueReturns == adaptive.calls && adaptive.falseReturns == 0,
                    scenario + " adaptive call contract drifted");
            require(adaptive.accepted > 0 && adaptive.accepted <= adaptive.proposed
                    && adaptive.visibleAccepted > 0 && adaptive.visibleAccepted <= adaptive.accepted
                    && adaptive.deferred > 0 && adaptive.deferred <= adaptive.calls
                    && adaptive.stalled == 0,
                    scenario + " adaptive accepted-work envelope drifted");
            require(scenario.equals("moving-dense") ||
                    adaptive.bestReadyRatio + 0.000001D >= baseline.bestReadyRatio,
                    scenario + " stationary normalized readiness regressed");
            require(baseline.globallyDrained() && adaptive.globallyDrained(),
                    scenario + " did not globally drain");
            require(governor.budgetSkipped > 0, scenario + " governor did not reject work");
            if (adaptive.budgetStops > 0) budgeted++;
            if (adaptive.compileMaxUs > budgetUs) overshot++;
            worstBaseline = Math.max(worstBaseline, baseline.compileMaxUs);
            worstAdaptive = Math.max(worstAdaptive, adaptive.compileMaxUs);
            if (verifyCheckpoint(build, scenario)) visualDivergence++;
        }
        require(budgeted > 0, "adaptive elapsed-work envelope never stopped a batch");
        require(overshot > 0, "M17 no longer demonstrates non-preemptive overshoot");
        require(visualDivergence == scenarios.size(), "checkpoint divergence finding drifted");
        String report = "matrix=STATIONARY_EMPTY_STATIONARY_DENSE_MOVING_DENSE\n"
                + "scheduler=VISIBLE_FIRST_ADAPTIVE_ENVELOPE\n"
                + "contract=ONE_CALL_ACCEPTED_DEFERRED_NEXT_FRAME\n"
                + "governor=REJECT_RETRYABLE_BACKLOG\n"
                + "readiness=STATIONARY_PARITY_OR_BETTER_MOVING_OBSERVED_NOT_FROZEN\n"
                + "starvation=NONE_AT_GLOBAL_DRAIN\n"
                + "budget=COOPERATIVE_NON_PREEMPTIVE_OVERSHOOT\n"
                + "latency=OBSERVED_NOT_FROZEN\n"
                + "framebuffer=THREE_CHECKPOINT_DIVERGENCE_DETECTED\n"
                + "shipping.status=LAB_ONLY_NO_GO\n";
        System.out.println("matrix.compileMaxUs.baseline=" + worstBaseline
                + " adaptive=" + worstAdaptive + " budgetUs=" + budgetUs);
        System.out.println("WORLDLINE_M17_SCHEDULER_HARDENING=PASS");
        System.out.print(report); System.out.println("evidence.sha256=" + sha256(report));
    }

    private static boolean verifyCheckpoint(Path build, String scenario) throws Exception {
        AeroFrameOracle.Sample baseline = frame(build.resolve(scenario + "-baseline-frame.log"));
        AeroFrameOracle.Sample adaptive = frame(build.resolve(scenario + "-adaptive-frame.log"));
        boolean moving = scenario.equals("moving-dense"); int view = moving ? 1
                : scenario.equals("stationary-empty") ? 2 : 0;
        double x = moving ? 23.5D : 8.5D, z = moving ? 16.0D : 8.5D;
        double yaw = moving ? 165.0D : 45.0D;
        for (AeroFrameOracle.Sample sample : Arrays.asList(baseline, adaptive))
            require(sample.path.equals(moving ? "moving" : "stationary") && sample.view == view
                    && close(sample.x, x) && close(sample.y, 67.0D) && close(sample.z, z)
                    && close(sample.yaw, yaw), scenario + " checkpoint pose drifted");
        require(baseline.tick == adaptive.tick && baseline.width == adaptive.width
                && baseline.height == adaptive.height, scenario + " framebuffer shape drifted");
        FrameDiff.Result pixels = FrameDiff.compare(build.resolve(scenario + "-baseline-frame.png"),
                build.resolve(scenario + "-adaptive-frame.png"));
        System.out.println(scenario + ".frame.changedPixels=" + pixels.changedPixels
                + " maxChannelDelta=" + pixels.maxChannelDelta);
        return pixels.changedPixels > 64 || pixels.maxChannelDelta > 2;
    }

    private static AeroFrameOracle.Sample frame(Path path) throws Exception {
        List<String> rows = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(rows.size() == 1, "expected one frame oracle record in " + path);
        return AeroFrameOracle.parse(rows.get(0));
    }
    private static void print(String scenario, String policy, RunMetrics value, long budgetUs) {
        System.out.println(scenario + "." + policy + ".frames=" + value.frames + "/" + value.allFrames
                + " calls=" + value.calls + " accepted=" + value.accepted
                + "/" + value.proposed + " visibleAccepted=" + value.visibleAccepted
                + " deferred=" + value.deferred + " stalled=" + value.stalled
                + " readyRatio=" + value.bestReadyRatio + " finalQueue=" + value.finalQueue
                + " frameP95Us=" + value.frameP95Us + " compileMaxUs=" + value.compileMaxUs
                + " overshootUs=" + Math.max(0, value.compileMaxUs - budgetUs)
                + " budgetStops=" + value.budgetStops + " budgetSkipped=" + value.budgetSkipped);
    }
    private static boolean close(double left, double right) { return Math.abs(left - right) < 0.0001D; }
    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
