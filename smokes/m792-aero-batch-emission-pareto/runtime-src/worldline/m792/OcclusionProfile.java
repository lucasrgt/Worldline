package worldline.m792;

import aero.modellib.Aero_MultiPoseReuse;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import worldline.profiling.*;

/** Separated render windows with allocation, cache activation and tail guardrails. */
public final class OcclusionProfile {
    private static final String[] NAMES = OcclusionMetricSchema.NAMES;
    private static final ProfilerSchema SCHEMA = OcclusionMetricSchema.SCHEMA;
    private static final int CAPACITY = 4 * (OcclusionScene.HOLD_FRAMES - OcclusionScene.WARM_FRAMES);
    private static final ProfilerRecorder[][] RECORDERS = new ProfilerRecorder[2][4];
    private static final long EPOCH = System.currentTimeMillis();
    private static final com.sun.management.ThreadMXBean ALLOCATION = allocation();
    private static long started, allocated;

    private OcclusionProfile() {}
    static long allocationNow() { return ALLOCATION.getThreadAllocatedBytes(Thread.currentThread().getId()); }

    public static void begin() {
        OcclusionStages.reset();
        started = OcclusionState.measured() ? System.nanoTime() : 0L;
        if (started != 0L) allocated = ALLOCATION.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    public static void sample() {
        if (started == 0L) return;
        long ended = System.nanoTime();
        long bytes = ALLOCATION.getThreadAllocatedBytes(Thread.currentThread().getId()) - allocated;
        int arm = OcclusionState.candidateEnabled() ? 1 : 0, scene = OcclusionState.checkpoint() / 4;
        ProfilerRecorder recorder = recorder(arm, scene);
        recorder.beginFrame(recorder.frames(), ended);
        recorder.set(0, ended - started);
        recorder.set(1, OcclusionStages.nanos(0));
        recorder.set(2, OcclusionStages.nanos(1));
        recorder.set(3, bytes);
        recorder.set(4, Aero_MultiPoseReuse.matchedThisFrame());
        recorder.set(5, Aero_MultiPoseReuse.bufferBytes());
        recorder.set(6, OcclusionStages.bytes(0));
        recorder.set(7, OcclusionStages.bytes(1));
        recorder.endFrame();
        started = 0L;
    }

    public static void write(File directory, String order) throws Exception {
        for (int arm = 0; arm < 2; arm++) for (int scene = 0; scene < 4; scene++)
            writeArm(directory, order, arm, scene);
    }

    private static void writeArm(File directory, String order, int arm, int scene) throws Exception {
        String name = (arm == 0 ? "replay-off" : "replay-on") + "-" + scene;
        ProfilerRecorder recorder = recorder(arm, scene);
        if (recorder.frames() != CAPACITY) throw new IllegalStateException("incomplete M792 profile " + name);
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("scenario", "m792-aero-batch-emission-pareto");
        tags.put("order", order); tags.put("arm", name);
        tags.put("boundary", "render-update-excludes-display-and-framebuffer-read");
        ProfilerRun run = ProfilerRun.of(SCHEMA, recorder.snapshot(), ProfilerRun.Mode.MIXED,
            EPOCH, System.currentTimeMillis(), tags);
        File artifact = new File(directory, "profile-" + name + ".wlpr");
        ProfilerArtifacts.write(artifact.toPath(), run);
        ProfilerSummary summary = new ProfilerSummary(ProfilerArtifacts.read(artifact.toPath()));
        try (PrintWriter out = new PrintWriter(new FileWriter(new File(directory, "profile-" + name + ".properties")))) {
            out.println("frames=" + summary.frames());
            out.println("render.hitches=" + summary.countAtLeast("mod.aero.render", 50000000L));
            out.println("artifact.sha256=" + com.google.common.hash.Hashing.sha256()
                .hashBytes(java.nio.file.Files.readAllBytes(artifact.toPath())).toString());
            for (String stage : NAMES) {
                String key = "mod.aero." + stage;
                out.println(stage + ".mean=" + summary.mean(key));
                out.println(stage + ".p99=" + summary.percentile(key, 99, 100));
                out.println(stage + ".maximum=" + summary.maximum(key));
                out.println(stage + ".total=" + summary.total(key));
            }
        }
    }

    private static ProfilerRecorder recorder(int arm, int scene) {
        if (RECORDERS[arm][scene] == null) RECORDERS[arm][scene] = new ProfilerRecorder(SCHEMA, CAPACITY);
        return RECORDERS[arm][scene];
    }

    private static com.sun.management.ThreadMXBean allocation() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!bean.isThreadAllocatedMemorySupported()) throw new IllegalStateException("allocation observer unavailable");
        bean.setThreadAllocatedMemoryEnabled(true);
        return bean;
    }
}
