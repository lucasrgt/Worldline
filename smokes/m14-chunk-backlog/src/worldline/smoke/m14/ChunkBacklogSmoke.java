package worldline.smoke.m14;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.aero.AeroChunkProbe;
import worldline.aero.AeroChunkProbe.Sample;
import worldline.aero.AeroDiagnostics;

/** Freezes the M14 caller, backlog, and bounded-policy invariants. */
public final class ChunkBacklogSmoke {
    private static final long SPIKE_US = 25_000L;
    private ChunkBacklogSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 5, "expected baseline/bounded probe and Aero logs plus limit");
        int limit = Integer.parseInt(arguments[4]);
        Metrics baseline = probes(Paths.get(arguments[0]), limit);
        Metrics bounded = probes(Paths.get(arguments[1]), limit);
        Timing baselineTime = timings(Paths.get(arguments[2]), limit);
        Timing boundedTime = timings(Paths.get(arguments[3]), limit);
        print("baseline", baseline); print("bounded", bounded);
        print("baseline", baselineTime); print("bounded", boundedTime);
        require(baseline.firstQueue > 1_000 && baseline.lastQueue > 1_000
                && baseline.rebuilds > 0, "baseline lacks persistent initial backlog work");
        require(baseline.forced == 0 && baseline.trueReturns == 0
                && baseline.falseReturns == baseline.calls && baseline.rebuilds >= baseline.calls
                && baseline.rebuilds <= baseline.calls + baseline.frames,
                "vanilla caller/return behavior drifted");
        require(baseline.calls > baseline.frames && baseline.quiet * 2 > baseline.frames,
                "backlog is not compiling independently of new dirtiness");
        require(bounded.calls > 0 && bounded.maxCalls == 1 && bounded.forced == 0
                && bounded.falseReturns == 0 && bounded.trueReturns == bounded.calls,
                "bounded policy did not end the retry loop");
        require(bounded.batches == bounded.calls && bounded.policyRebuilds == bounded.calls * 2
                && bounded.rebuilds == bounded.policyRebuilds, "bounded work contract drifted");
        require(bounded.firstQueue > 1_000 && bounded.lastQueue > 1_000
                && baseline.calls > bounded.calls, "bounded queue/call tradeoff absent");
        String report = "caller.flag=NON_FORCED\nbacklog.source=INITIAL_DIRTY_QUEUE\n"
                + "retry.loop=FALSE_UNTIL_DEADLINE\n"
                + "bounded.policy=ONE_CALL_TWO_REBUILDS_TRUE\nshipping.status=EXPERIMENTAL\n";
        System.out.println("WORLDLINE_M14_CHUNK_BACKLOG=PASS");
        System.out.print(report);
        System.out.println("evidence.sha256=" + sha256(report));
    }

    private static Metrics probes(Path path, int limit) throws Exception {
        List<Sample> all = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (row.startsWith("[WorldlineChunkProbe]")) all.add(AeroChunkProbe.parse(row));
        require(all.size() >= limit, "too few probe frames in " + path);
        List<Sample> samples = all.subList(all.size() - limit, all.size());
        Metrics value = new Metrics(samples.size(), samples.get(0).queueStart);
        List<Long> compile = new ArrayList<>();
        for (Sample sample : samples) {
            value.calls += sample.calls; value.falseReturns += sample.falseReturns;
            value.trueReturns += sample.trueReturns; value.forced += sample.forced;
            value.rebuilds += sample.rebuilds; value.batches += sample.policyBatches;
            value.invalidates += sample.invalidates;
            value.policyRebuilds += sample.policyRebuilds; value.maxCalls = Math.max(value.maxCalls, sample.calls);
            if (sample.invalidates == 0 && sample.marks == 0) value.quiet++;
            if (sample.queueEnd >= 0) value.lastQueue = sample.queueEnd;
            compile.add(sample.compileUs); value.compileMax = Math.max(value.compileMax, sample.compileUs);
        }
        Collections.sort(compile); value.compileP95 = compile.get((int) ((limit - 1) * 0.95D));
        return value;
    }

    private static Timing timings(Path path, int limit) throws Exception {
        List<Long> all = new ArrayList<>();
        for (String row : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (!row.startsWith("[Aero_")) continue;
            AeroDiagnostics.Sample sample = AeroDiagnostics.parse(row);
            if (sample.visibleChunks <= 0) continue;
            all.add(sample.frameUs);
        }
        require(all.size() >= limit, "too few Aero frames in " + path);
        List<Long> frames = new ArrayList<>(all.subList(all.size() - limit, all.size()));
        long spikes = frames.stream().filter(value -> value >= SPIKE_US).count();
        Collections.sort(frames);
        return new Timing(limit, spikes, frames.get(limit - 1), frames.get((int) ((limit - 1) * 0.95D)));
    }

    private static void print(String name, Metrics value) {
        System.out.println(name + ".probeFrames=" + value.frames + " calls=" + value.calls
                + " false=" + value.falseReturns + " true=" + value.trueReturns
                + " rebuilds=" + value.rebuilds + " invalidates=" + value.invalidates
                + " queue=" + value.firstQueue + "->" + value.lastQueue
                + " quiet=" + value.quiet + " policyBatches=" + value.batches
                + " compileMaxUs=" + value.compileMax + " compileP95Us=" + value.compileP95);
    }
    private static void print(String name, Timing value) {
        System.out.println(name + ".aeroFrames=" + value.frames + " spikes=" + value.spikes
                + " frameMaxUs=" + value.max + " frameP95Us=" + value.p95);
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
        final long frames, firstQueue; long lastQueue = -1, calls, falseReturns, trueReturns, forced;
        long rebuilds, batches, policyRebuilds, invalidates, maxCalls, quiet, compileMax, compileP95;
        Metrics(long frames, long firstQueue) { this.frames = frames; this.firstQueue = firstQueue; }
    }
    private static final class Timing {
        final long frames, spikes, max, p95;
        Timing(long frames, long spikes, long max, long p95) {
            this.frames = frames; this.spikes = spikes; this.max = max; this.p95 = p95;
        }
    }
}
