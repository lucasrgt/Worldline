package worldline.aero;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict view of the Aero fields used by the M13 differential. */
public final class AeroDiagnostics {
    private AeroDiagnostics() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith("[Aero_"))
            throw new IllegalArgumentException("not an Aero frame log line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(line.indexOf(']') + 1).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        return new Sample(micros(fields, "frameMs"), micros(fields, "compileChunksMs"),
                micros(fields, "compileChunksMaxMs"), micros(fields, "gcTimeDeltaMs"),
                whole(fields, "compileChunksCalls"), whole(fields, "compileChunksSkipped"),
                whole(fields, "compileBudgetSkipped"), whole(fields, "batchQueued"),
                whole(fields, "cellQueued"), whole(fields, "beViewCulled"),
                whole(fields, "visibleChunks"));
    }

    private static long whole(Map<String, String> fields, String name) {
        try { return Long.parseLong(required(fields, name)); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid Aero counter " + name, error);
        }
    }

    private static long micros(Map<String, String> fields, String name) {
        try { return new BigDecimal(required(fields, name)).movePointRight(3).longValueExact(); }
        catch (ArithmeticException | NumberFormatException error) {
            throw new IllegalArgumentException("invalid Aero timing " + name, error);
        }
    }

    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("missing Aero field " + name);
        return value;
    }

    public static final class Sample {
        public final long frameUs, compileUs, compileMaxUs, gcUs;
        public final long compileCalls, compileSkipped, budgetSkipped;
        public final long batchQueued, cellQueued, viewCulled, visibleChunks;

        Sample(long frameUs, long compileUs, long compileMaxUs, long gcUs,
                long compileCalls, long compileSkipped, long budgetSkipped,
                long batchQueued, long cellQueued, long viewCulled, long visibleChunks) {
            this.frameUs = frameUs; this.compileUs = compileUs; this.compileMaxUs = compileMaxUs;
            this.gcUs = gcUs; this.compileCalls = compileCalls;
            this.compileSkipped = compileSkipped; this.budgetSkipped = budgetSkipped;
            this.batchQueued = batchQueued; this.cellQueued = cellQueued;
            this.viewCulled = viewCulled; this.visibleChunks = visibleChunks;
        }

        public long sceneWork() { return batchQueued + cellQueued + viewCulled; }
    }
}
