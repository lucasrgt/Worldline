package worldline.aero;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.analysis.FrameAttribution;

/** Maps Aero's opt-in frame-spike line to adapter-neutral work counters. */
public final class AeroFrameLog {
    private static final String[][] COUNTERS = {
        {"animAccepted", "animation.accepted"}, {"animRejected", "animation.rejected"},
        {"batchQueued", "batch.queued"}, {"batchFlushed", "batch.flushed"},
        {"batchBatches", "batch.calls"}, {"batchImmediate", "batch.immediate"},
        {"atRestRenders", "rest.renders"}, {"atRestListCalls", "rest.listcalls"},
        {"cellQueued", "cell.queued"}, {"cellCalls", "cell.calls"},
        {"cellRebuilds", "cell.rebuilds"}, {"beViewCulled", "view.culled"},
        {"compileChunksCalls", "chunks.compiled"}, {"renderChunksCalls", "chunks.rendered"},
        {"dlLive", "displaylists.live"}, {"prewarmDrained", "prewarm.drained"},
        {"visibleChunks", "chunks.visible"}
    };

    private AeroFrameLog() {}

    public static FrameAttribution.Frame parse(String line) {
        if (line == null || !line.startsWith("[Aero_"))
            throw new IllegalArgumentException("not an Aero frame log line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(line.indexOf(']') + 1).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        Map<String, Long> work = new LinkedHashMap<>();
        for (String[] mapping : COUNTERS)
            work.put(mapping[1], whole(fields, mapping[0]));
        return FrameAttribution.Frame.of(micros(fields, "frameMs"),
                micros(fields, "gcTimeDeltaMs"), work);
    }

    public static FrameAttribution.Result compare(String baseline, String observed) {
        return FrameAttribution.compare(parse(baseline), parse(observed));
    }

    private static long whole(Map<String, String> fields, String name) {
        String raw = required(fields, name);
        try {
            long value = Long.parseLong(raw);
            if (value < 0) throw new NumberFormatException("negative");
            return value;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid Aero counter " + name, error);
        }
    }

    private static long micros(Map<String, String> fields, String name) {
        try {
            long value = new BigDecimal(required(fields, name)).movePointRight(3).longValueExact();
            if (value < 0) throw new ArithmeticException("negative");
            return value;
        } catch (ArithmeticException | NumberFormatException error) {
            throw new IllegalArgumentException("invalid Aero timing " + name, error);
        }
    }

    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("missing Aero field " + name);
        return value;
    }
}
