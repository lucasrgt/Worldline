package worldline.m791;

import aero.modellib.Aero_FrameSpikeLogger;
import aero.modellib.Aero_AnimatedBatcher;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerRecorder;
import worldline.profiling.ProfilerRun;
import worldline.profiling.ProfilerSchema;
import worldline.profiling.ProfilerSummary;
import worldline.profiling.WorldlineProfilerMetrics;

/** Diagnostic stage timings; this short mixed capture is not a promotion benchmark. */
public final class OcclusionProfile {
    private static final String[] NAMES = {"render", "world", "terrain", "enqueue", "flush", "prepare",
        "animated", "pages"};
    private static final ProfilerSchema SCHEMA = ProfilerSchema.of(Arrays.asList(
        metric("render"), metric("world"), metric("terrain"),
        metric("enqueue"), metric("flush"), metric("prepare"), metric("animated"), metric("pages")));
    private static final int CAPACITY = OcclusionScene.CHECKPOINTS * OcclusionScene.HOLD_FRAMES;
    private static final ProfilerRecorder[] RECORDERS = {
        new ProfilerRecorder(SCHEMA, CAPACITY), new ProfilerRecorder(SCHEMA, CAPACITY)};
    private static final long EPOCH = System.currentTimeMillis();
    private static final long[][] COUNTERS = new long[2][4];
    private static long started;

    private OcclusionProfile() {}

    public static void begin() {
        OcclusionStages.reset();
        started = OcclusionState.retaining() ? System.nanoTime() : 0L;
    }

    public static void sample() {
        if (!OcclusionState.retaining() || started == 0L) return;
        long ended = System.nanoTime();
        ProfilerRecorder recorder = RECORDERS[OcclusionState.candidateEnabled() ? 1 : 0];
        recorder.beginFrame(recorder.frames(), ended);
        recorder.set(0, ended - started);
        recorder.set(1, Aero_FrameSpikeLogger.renderWorldNanos());
        recorder.set(2, Aero_FrameSpikeLogger.terrainRenderNanos());
        recorder.set(3, Aero_FrameSpikeLogger.entityRenderNanos());
        recorder.set(4, Aero_FrameSpikeLogger.worldFlushNanos());
        recorder.set(5, Aero_FrameSpikeLogger.aeroPrepareNanos());
        recorder.set(6, OcclusionStages.nanos(0));
        recorder.set(7, OcclusionStages.nanos(1));
        long[] counters = COUNTERS[OcclusionState.candidateEnabled() ? 1 : 0];
        counters[0] += Aero_AnimatedBatcher.batchPosesResolvedThisFrame();
        counters[1] += Aero_AnimatedBatcher.batchPosesReusedThisFrame();
        counters[2] += Aero_AnimatedBatcher.batchVerticesTransformedThisFrame();
        counters[3] += Aero_AnimatedBatcher.batchVertexTransformsReusedThisFrame();
        recorder.endFrame();
        started = 0L;
    }

    public static void write(File directory, String order) throws Exception {
        for (int arm = 0; arm < 2; arm++) {
            String name = arm == 0 ? "occlusion-off" : "occlusion-on";
            if (RECORDERS[arm].frames() != CAPACITY) {
                throw new IllegalStateException("incomplete M791 profiler arm: " + name);
            }
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("scenario", "m791-aero-be-occlusion-integrity");
            tags.put("order", order);
            tags.put("arm", name);
            tags.put("qualification", "diagnostic-only");
            tags.put("boundary", "render-update-excludes-display-and-framebuffer-read");
            ProfilerRun run = ProfilerRun.of(SCHEMA, RECORDERS[arm].snapshot(),
                ProfilerRun.Mode.MIXED, EPOCH, System.currentTimeMillis(), tags);
            File artifact = new File(directory, "profile-" + name + ".wlpr");
            ProfilerArtifacts.write(artifact.toPath(), run);
            ProfilerSummary summary = new ProfilerSummary(ProfilerArtifacts.read(artifact.toPath()));
            try (PrintWriter out = new PrintWriter(new FileWriter(
                    new File(directory, "profile-" + name + ".properties")))) {
                out.println("qualification=diagnostic-only");
                out.println("frames=" + summary.frames());
                out.println("poses.resolved=" + COUNTERS[arm][0]);
                out.println("poses.reused=" + COUNTERS[arm][1]);
                out.println("vertices.transformed=" + COUNTERS[arm][2]);
                out.println("vertices.reused=" + COUNTERS[arm][3]);
                for (String stage : NAMES) {
                    String key = "mod.aero." + stage + ".nanos";
                    out.println(stage + ".mean.nanos=" + summary.mean(key));
                    out.println(stage + ".p99.nanos=" + summary.percentile(key, 99, 100));
                    out.println(stage + ".maximum.nanos=" + summary.maximum(key));
                }
            }
        }
    }

    private static worldline.profiling.ProfilerMetric metric(String name) {
        return WorldlineProfilerMetrics.extensionDuration("mod.aero." + name + ".nanos", "aero");
    }
}
