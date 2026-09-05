package worldline.m792;

import java.util.Arrays;
import worldline.profiling.*;

/** Shared profiler definition validated before starting an official client. */
public final class OcclusionMetricSchema {
    static final String[] NAMES = {"render", "animated", "pages", "allocation", "matches", "bytes",
        "animatedallocation", "pageallocation"};
    static final ProfilerSchema SCHEMA = ProfilerSchema.of(Arrays.asList(
        duration("render"), duration("animated"), duration("pages"), bytes("allocation"),
        WorldlineProfilerMetrics.extensionCounter("mod.aero.matches", "aero"), bytes("bytes"),
        bytes("animatedallocation"), bytes("pageallocation")));

    private OcclusionMetricSchema() {}

    public static void main(String[] args) {
        for (String name : NAMES) if (!SCHEMA.contains("mod.aero." + name))
            throw new IllegalStateException("profile channel missing: " + name);
        System.out.println("WORLDLINE_M792_PROFILE_SCHEMA=PASS");
    }

    private static ProfilerMetric duration(String name) {
        return WorldlineProfilerMetrics.extensionDuration("mod.aero." + name, "aero");
    }
    private static ProfilerMetric bytes(String name) {
        return ProfilerMetric.of("mod.aero." + name, "aero", ProfilerMetric.Unit.BYTES,
            ProfilerMetric.Kind.GAUGE, ProfilerMetric.Causality.DIAGNOSTIC);
    }
}
