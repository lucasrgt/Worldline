package worldline.smoke.m15;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import worldline.aero.AeroChunkGeometry;
import worldline.aero.AeroChunkReadiness;
import worldline.aero.AeroDiagnostics;

/** Qualifies the explicit work contract, readiness telemetry, and geometry oracle. */
public final class ChunkContractSmoke {
    private ChunkContractSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 7, "expected six logs and frame limit");
        int limit = Integer.parseInt(arguments[6]);
        Metrics baseline = metrics(Paths.get(arguments[0]), limit);
        Metrics contract = metrics(Paths.get(arguments[1]), limit);
        Geometry geometry = geometry(Paths.get(arguments[2]), Paths.get(arguments[3]));
        Timing baselineTime = timing(Paths.get(arguments[4]), limit);
        Timing contractTime = timing(Paths.get(arguments[5]), limit);
        print("baseline", baseline); print("contract", contract);
        System.out.println("geometry.matches=" + geometry.matches
                + " mismatches=" + geometry.mismatches);
        print("baseline", baselineTime); print("contract", contractTime);
        require(baseline.forced == 0 && baseline.falseReturns == baseline.calls
                && baseline.trueReturns == 0 && baseline.calls > baseline.frames,
                "vanilla retry contract drifted");
        require(contract.forced == 0 && contract.calls == contract.frames
                && contract.trueReturns == contract.calls && contract.falseReturns == 0,
                "explicit contract did not end each frame once");
        require(contract.accepted == contract.calls * 2 && contract.rebuilds == contract.accepted
                && contract.deferred == contract.calls && contract.completed == 0
                && contract.stalled == 0, "accepted/deferred work contract drifted");
        require(baseline.firstQueue > 1_000 && contract.firstQueue > 1_000
                && baseline.lastQueue < baseline.firstQueue && contract.lastQueue < contract.firstQueue,
                "dirty queues did not drain");
        require(contract.firstVisibleDirty > 0 && contract.bestReady > 0 && contract.maxOldest > 0,
                "visible readiness or dirty age was not observed");
        require(geometry.matches >= 100 && geometry.mismatches > 0
                && geometry.matches * 4 > (geometry.matches + geometry.mismatches) * 3,
                "chunk geometry comparison lacked stable exact matches and temporal divergence");
        String report = "contract.result=ACCEPTED_DEFERRED\nresume.point=NEXT_FRAME\n"
                + "readiness.telemetry=DIRTY_AGE_AND_VISIBLE_STATE\n"
                + "readiness.comparison=OBSERVED_NOT_FROZEN\n"
                + "geometry.result=TEMPORAL_DIVERGENCE_OBSERVED\n"
                + "shipping.status=EXPERIMENTAL_NOT_PROMOTED\n";
        System.out.println("WORLDLINE_M15_CHUNK_CONTRACT=PASS");
        System.out.print(report); System.out.println("evidence.sha256=" + sha256(report));
    }

    private static Metrics metrics(Path path, int limit) throws Exception {
        List<AeroChunkReadiness.Sample> all = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (row.startsWith("[WorldlineChunkProbe]")) all.add(AeroChunkReadiness.parse(row));
        require(all.size() >= limit, "too few readiness frames in " + path);
        Metrics value = new Metrics(limit);
        for (AeroChunkReadiness.Sample sample : all.subList(0, limit)) {
            value.calls += sample.calls; value.falseReturns += sample.falseReturns;
            value.trueReturns += sample.trueReturns; value.forced += sample.forced;
            value.rebuilds += sample.rebuilds; value.accepted += sample.accepted;
            value.deferred += sample.deferred; value.completed += sample.completed;
            value.stalled += sample.stalled; value.maxOldest = Math.max(value.maxOldest, sample.oldest);
            value.maxVisibleReady = Math.max(value.maxVisibleReady, sample.visibleReady);
            if (sample.visible > 0 && sample.visibleReady * value.bestVisible
                    > value.bestReady * sample.visible) {
                value.bestReady = sample.visibleReady; value.bestVisible = sample.visible;
            }
            if (sample.queue >= 0) { if (value.firstQueue < 0) value.firstQueue = sample.queue; value.lastQueue = sample.queue; }
            if (sample.visibleDirty >= 0) {
                if (value.firstVisibleDirty < 0 && sample.visibleDirty > 0)
                    value.firstVisibleDirty = sample.visibleDirty;
                value.minVisibleDirty = Math.min(value.minVisibleDirty, sample.visibleDirty);
            }
        }
        return value;
    }

    private static Geometry geometry(Path left, Path right) throws Exception {
        Map<String, Set<String>> first = geometries(left), second = geometries(right);
        int matches = 0, mismatches = 0;
        for (String key : first.keySet()) if (second.containsKey(key)) {
            Set<String> overlap = new HashSet<>(first.get(key)); overlap.retainAll(second.get(key));
            if (overlap.isEmpty()) mismatches++; else matches++;
        }
        return new Geometry(matches, mismatches);
    }

    private static Map<String, Set<String>> geometries(Path path) throws Exception {
        Map<String, Set<String>> result = new HashMap<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (!row.startsWith("[WorldlineChunkGeometry]")) continue;
            AeroChunkGeometry.Sample sample = AeroChunkGeometry.parse(row);
            if (sample.vertices == 0) continue;
            result.computeIfAbsent(sample.key, key -> new HashSet<>()).add(sample.signature());
        }
        return result;
    }

    private static Timing timing(Path path, int limit) throws Exception {
        List<Long> frames = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (row.startsWith("[Aero_")) frames.add(AeroDiagnostics.parse(row).frameUs);
        require(frames.size() >= limit, "too few Aero frames in " + path);
        frames = new ArrayList<>(frames.subList(0, limit)); Collections.sort(frames);
        return new Timing(frames.get(limit - 1), frames.get((int) ((limit - 1) * 0.95D)));
    }

    private static void print(String name, Metrics value) {
        System.out.println(name + ".frames=" + value.frames + " calls=" + value.calls
                + " rebuilds=" + value.rebuilds + " queue=" + value.firstQueue + "->" + value.lastQueue
                + " visibleDirty=" + value.firstVisibleDirty + "->" + value.minVisibleDirty
                + " maxReady=" + value.maxVisibleReady + " readyRatio="
                + value.bestReady + "/" + value.bestVisible + " oldest=" + value.maxOldest);
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
        final long frames; long calls, falseReturns, trueReturns, forced, rebuilds, accepted;
        long deferred, completed, stalled, firstQueue = -1, lastQueue = -1;
        long firstVisibleDirty = -1, minVisibleDirty = Long.MAX_VALUE, maxVisibleReady, maxOldest;
        long bestReady, bestVisible = 1;
        Metrics(long frames) { this.frames = frames; }
    }
    private static final class Geometry { final int matches, mismatches;
        Geometry(int matches, int mismatches) { this.matches = matches; this.mismatches = mismatches; } }
    private static final class Timing { final long max, p95;
        Timing(long max, long p95) { this.max = max; this.p95 = p95; } }
}
