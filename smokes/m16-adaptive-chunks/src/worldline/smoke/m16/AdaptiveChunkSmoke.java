package worldline.smoke.m16;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.aero.AeroChunkReadiness;
import worldline.aero.AeroDiagnostics;
import worldline.aero.AeroFrameOracle;

/** Qualifies adaptive visible-first work and frozen-tick strict pixel parity. */
public final class AdaptiveChunkSmoke {
    private AdaptiveChunkSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 9, "expected probe, Aero, frame logs, images, and limit");
        int limit = Integer.parseInt(arguments[8]);
        Metrics baseline = metrics(Paths.get(arguments[0]), limit);
        Metrics adaptive = metrics(Paths.get(arguments[1]), limit);
        Timing baselineTime = timing(Paths.get(arguments[2]), limit);
        Timing adaptiveTime = timing(Paths.get(arguments[3]), limit);
        AeroFrameOracle.Sample baselineFrame = frame(Paths.get(arguments[4]));
        AeroFrameOracle.Sample adaptiveFrame = frame(Paths.get(arguments[5]));
        FrameDiff.Result pixels = FrameDiff.compare(Paths.get(arguments[6]), Paths.get(arguments[7]));
        require(baseline.forced == 0 && baseline.falseReturns == baseline.calls
                && baseline.trueReturns == 0 && baseline.calls > baseline.frames,
                "vanilla retry behavior drifted");
        require(adaptive.forced == 0 && adaptive.calls == adaptive.frames
                && adaptive.trueReturns == adaptive.calls && adaptive.falseReturns == 0,
                "adaptive contract retried or skipped a frame");
        require(adaptive.accepted > adaptive.frames * 2 && adaptive.accepted <= adaptive.proposed
                && adaptive.visibleAccepted == adaptive.accepted && adaptive.budgetStops > 0
                && adaptive.deferred == adaptive.calls && adaptive.stalled == 0,
                "adaptive accepted-work envelope drifted");
        require(adaptive.minVisibleDirty <= baseline.minVisibleDirty
                && adaptive.maxVisibleReady >= baseline.maxVisibleReady,
                "adaptive scheduler did not close the initial readiness gap");
        require(baseline.finalVisibleDirty == 0 && adaptive.finalVisibleDirty == 0
                && baseline.finalVisibleReady == baseline.finalVisible
                && adaptive.finalVisibleReady == adaptive.finalVisible,
                "frozen worlds did not reach visible readiness");
        require(adaptiveFrame.tick == baselineFrame.tick && adaptiveFrame.width == baselineFrame.width
                && adaptiveFrame.height == baselineFrame.height, "frozen framebuffer shape mismatch");
        require(pixels.changedPixels <= 64 && pixels.maxChannelDelta <= 2,
                "frozen framebuffer exceeded strict pixel tolerance");
        String report = "scheduler=VISIBLE_FIRST_ADAPTIVE_ENVELOPE\n"
                + "contract=ACCEPTED_DEFERRED_NEXT_FRAME\nreadiness=VANILLA_PARITY_OR_BETTER\n"
                + "framebuffer=FROZEN_TICK_STRICT_PIXEL_PARITY\nshipping.status=CANDIDATE\n";
        System.out.println("WORLDLINE_M16_ADAPTIVE_CHUNKS=PASS");
        print("baseline", baseline); print("adaptive", adaptive);
        print("baseline", baselineTime); print("adaptive", adaptiveTime);
        System.out.println("baseline.readyFrames=" + baselineFrame.frames
                + " adaptive.readyFrames=" + adaptiveFrame.frames);
        System.out.println("baseline.frame.sha256=" + baselineFrame.hash
                + " adaptive.frame.sha256=" + adaptiveFrame.hash);
        System.out.println("frame.changedPixels=" + pixels.changedPixels
                + " maxChannelDelta=" + pixels.maxChannelDelta);
        System.out.print(report); System.out.println("evidence.sha256=" + sha256(report));
    }

    private static Metrics metrics(Path path, int limit) throws Exception {
        List<AeroChunkReadiness.Sample> all = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (row.startsWith("[WorldlineChunkProbe]")) all.add(AeroChunkReadiness.parse(row));
        require(all.size() >= limit, "too few readiness frames in " + path);
        Metrics value = new Metrics(limit, all.size());
        for (AeroChunkReadiness.Sample sample : all.subList(0, limit)) {
            value.calls += sample.calls; value.falseReturns += sample.falseReturns;
            value.trueReturns += sample.trueReturns; value.forced += sample.forced;
            value.accepted += sample.accepted; value.proposed += sample.proposed;
            value.visibleAccepted += sample.visibleAccepted; value.budgetStops += sample.budgetStops;
            value.deferred += sample.deferred; value.stalled += sample.stalled;
            value.minVisibleDirty = Math.min(value.minVisibleDirty, sample.visibleDirty);
            value.maxVisibleReady = Math.max(value.maxVisibleReady, sample.visibleReady);
        }
        AeroChunkReadiness.Sample last = all.get(all.size() - 1);
        value.finalVisible = last.visible; value.finalVisibleDirty = last.visibleDirty;
        value.finalVisibleReady = last.visibleReady; value.finalQueue = last.queue;
        return value;
    }

    private static Timing timing(Path path, int limit) throws Exception {
        List<Long> frames = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (row.startsWith("[Aero_")) frames.add(AeroDiagnostics.parse(row).frameUs);
        require(frames.size() >= limit, "too few Aero frames in " + path);
        frames = new ArrayList<>(frames.subList(0, limit)); Collections.sort(frames);
        return new Timing(frames.get(limit - 1), frames.get((int) ((limit - 1) * 0.95D)));
    }
    private static AeroFrameOracle.Sample frame(Path path) throws Exception {
        List<String> rows = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(rows.size() == 1, "expected one frame oracle record");
        return AeroFrameOracle.parse(rows.get(0));
    }

    private static void print(String name, Metrics value) {
        System.out.println(name + ".frames=" + value.frames + "/" + value.allFrames
                + " calls=" + value.calls + " accepted=" + value.accepted
                + " minVisibleDirty=" + value.minVisibleDirty + " maxReady=" + value.maxVisibleReady
                + " finalVisible=" + value.finalVisible + " finalQueue=" + value.finalQueue);
    }
    private static void print(String name, Timing value) {
        System.out.println(name + ".frameMaxUs=" + value.max + " frameP95Us=" + value.p95);
    }
    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Metrics {
        final long frames, allFrames; long calls, falseReturns, trueReturns, forced;
        long accepted, proposed, visibleAccepted, budgetStops, deferred, stalled;
        long minVisibleDirty = Long.MAX_VALUE, maxVisibleReady, finalVisible;
        long finalVisibleDirty, finalVisibleReady, finalQueue;
        Metrics(long frames, long allFrames) { this.frames = frames; this.allFrames = allFrames; }
    }
    private static final class Timing { final long max, p95;
        Timing(long max, long p95) { this.max = max; this.p95 = p95; } }
}
