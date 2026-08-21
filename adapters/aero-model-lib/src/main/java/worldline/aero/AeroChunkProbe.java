package worldline.aero;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict parser for the smoke-only chunk compiler probe. */
public final class AeroChunkProbe {
    private static final String PREFIX = "[WorldlineChunkProbe]";

    private AeroChunkProbe() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith(PREFIX))
            throw new IllegalArgumentException("not a Worldline chunk probe line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(PREFIX.length()).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        return new Sample(value(fields, "calls"), value(fields, "false"), value(fields, "true"),
                value(fields, "forced"), value(fields, "rebuilds"), value(fields, "queueStart"),
                value(fields, "queueEnd"), value(fields, "queueMax"), value(fields, "invalidates"),
                value(fields, "marks"), value(fields, "sorts"), value(fields, "policyBatches"),
                value(fields, "policyRebuilds"), value(fields, "policyRemaining"),
                value(fields, "compileUs"));
    }

    private static long value(Map<String, String> fields, String name) {
        String raw = fields.get(name);
        if (raw == null) throw new IllegalArgumentException("missing chunk probe field " + name);
        try { return Long.parseLong(raw); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid chunk probe field " + name, error);
        }
    }

    public static final class Sample {
        public final long calls, falseReturns, trueReturns, forced, rebuilds;
        public final long queueStart, queueEnd, queueMax, invalidates, marks, sorts;
        public final long policyBatches, policyRebuilds, policyRemaining, compileUs;

        Sample(long calls, long falseReturns, long trueReturns, long forced, long rebuilds,
                long queueStart, long queueEnd, long queueMax, long invalidates, long marks,
                long sorts, long policyBatches, long policyRebuilds, long policyRemaining,
                long compileUs) {
            this.calls = calls; this.falseReturns = falseReturns; this.trueReturns = trueReturns;
            this.forced = forced; this.rebuilds = rebuilds; this.queueStart = queueStart;
            this.queueEnd = queueEnd; this.queueMax = queueMax; this.invalidates = invalidates;
            this.marks = marks; this.sorts = sorts; this.policyBatches = policyBatches;
            this.policyRebuilds = policyRebuilds; this.policyRemaining = policyRemaining;
            this.compileUs = compileUs;
        }
    }
}
