package worldline.aero;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict M15 view of caller outcomes, dirty age, and visible readiness. */
public final class AeroChunkReadiness {
    private static final String PREFIX = "[WorldlineChunkProbe]";
    private AeroChunkReadiness() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith(PREFIX))
            throw new IllegalArgumentException("not a Worldline chunk probe line");
        Map<String, String> fields = fields(line.substring(PREFIX.length()));
        return new Sample(value(fields, "calls"), value(fields, "false"), value(fields, "true"),
                value(fields, "forced"), value(fields, "rebuilds"), value(fields, "queueEnd"),
                value(fields, "accepted"), value(fields, "deferred"), value(fields, "completed"),
                value(fields, "stalled"), value(fields, "dirty"), value(fields, "visible"),
                value(fields, "visibleDirty"), value(fields, "visibleReady"), value(fields, "oldest"),
                value(fields, "oldestVisible"), value(fields, "compileUs"));
    }

    private static Map<String, String> fields(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String token : text.trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                result.put(token.substring(0, equals), token.substring(equals + 1));
        }
        return result;
    }

    private static long value(Map<String, String> fields, String name) {
        String raw = fields.get(name);
        if (raw == null) throw new IllegalArgumentException("missing readiness field " + name);
        try { return Long.parseLong(raw); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid readiness field " + name, error);
        }
    }

    public static final class Sample {
        public final long calls, falseReturns, trueReturns, forced, rebuilds, queue;
        public final long accepted, deferred, completed, stalled, dirty;
        public final long visible, visibleDirty, visibleReady, oldest, oldestVisible, compileUs;

        Sample(long calls, long falseReturns, long trueReturns, long forced, long rebuilds,
                long queue, long accepted, long deferred, long completed, long stalled, long dirty,
                long visible, long visibleDirty, long visibleReady, long oldest,
                long oldestVisible, long compileUs) {
            this.calls = calls; this.falseReturns = falseReturns; this.trueReturns = trueReturns;
            this.forced = forced; this.rebuilds = rebuilds; this.queue = queue;
            this.accepted = accepted; this.deferred = deferred; this.completed = completed;
            this.stalled = stalled; this.dirty = dirty; this.visible = visible;
            this.visibleDirty = visibleDirty; this.visibleReady = visibleReady;
            this.oldest = oldest; this.oldestVisible = oldestVisible; this.compileUs = compileUs;
        }
    }
}
