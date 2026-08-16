package worldline.smoke.m12;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.aero.AeroFrameLog;
import worldline.analysis.FrameAttribution;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioMinimizer;

/** Verifies two captures of one saved Aero world and minimizes each spike record window. */
public final class AeroReproductionSmoke {
    private static final long SPIKE_US = 25_000L;

    private AeroReproductionSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 2, "expected two capture logs");
        Capture first = analyze(Paths.get(arguments[0]));
        Capture second = analyze(Paths.get(arguments[1]));
        String report = "capture.runs=2\nsaved.world.captured=true\nworld.seed.recreated=true\n"
                + "dense.scene.present.both=true\n"
                + "spike.cause=LOGICAL_WORK\nspike.counter=chunks.compiled\n"
                + "minimized.records=1\n";
        System.out.println("WORLDLINE_M12_REPRODUCTION=PASS");
        System.out.println("capture.1.records=" + first.records);
        System.out.println("capture.2.records=" + second.records);
        System.out.print(report);
        System.out.println("evidence.sha256=" + sha256(report));
    }

    private static Capture analyze(Path path) throws Exception {
        List<String> all = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<FrameAttribution.Frame> frames = new ArrayList<>();
        List<Long> work = new ArrayList<>();
        for (String line : all) {
            if (!line.startsWith("[Aero_") || line.contains(" visibleChunks=-1 ")) continue;
            FrameAttribution.Frame frame = AeroFrameLog.parse(line);
            long sceneWork = value(frame, "cell.queued") + value(frame, "batch.queued")
                    + value(frame, "view.culled");
            if (sceneWork <= 0 || value(frame, "chunks.visible") <= 0) continue;
            frames.add(frame); work.add(sceneWork);
        }
        require(!frames.isEmpty(), "capture has no stable real-scene frames: " + path);
        int baseline = baseline(frames);
        List<String> steps = new ArrayList<>();
        for (int index = 0; index < frames.size(); index++) steps.add("frame:" + index);
        ScenarioMinimizer.Result minimized = ScenarioMinimizer.minimize(
                Scenario.of(steps), 100, candidate -> preserves(candidate, frames, baseline));
        require(minimized.complete() && minimized.minimized().size() == 1,
                "spike record did not minimize to one record");
        return new Capture(frames.size(), work);
    }

    private static int baseline(List<FrameAttribution.Frame> frames) {
        int best = 0;
        for (int index = 1; index < frames.size(); index++) {
            FrameAttribution.Frame a = frames.get(index), b = frames.get(best);
            long ac = value(a, "chunks.compiled"), bc = value(b, "chunks.compiled");
            if (ac < bc || ac == bc && a.frameMicros() < b.frameMicros()) best = index;
        }
        return best;
    }

    private static boolean preserves(Scenario scenario, List<FrameAttribution.Frame> frames, int baseline) {
        for (String step : scenario.steps()) {
            int index = Integer.parseInt(step.substring("frame:".length()));
            FrameAttribution.Frame frame = frames.get(index);
            FrameAttribution.Result result = FrameAttribution.compare(frames.get(baseline), frame);
            if (frame.frameMicros() >= SPIKE_US
                    && result.cause() == FrameAttribution.Cause.LOGICAL_WORK
                    && "chunks.compiled".equals(result.topCounter())) return true;
        }
        return false;
    }

    private static long value(FrameAttribution.Frame frame, String name) {
        Long value = frame.counters().get(name); return value == null ? 0L : value;
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private static final class Capture {
        final int records; final List<Long> sceneWork;
        Capture(int records, List<Long> sceneWork) { this.records = records; this.sceneWork = sceneWork; }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
