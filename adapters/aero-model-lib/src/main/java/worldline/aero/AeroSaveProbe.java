package worldline.aero;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict view of Aero save, compile, GC, heap, and allocation fields. */
public final class AeroSaveProbe {
    private AeroSaveProbe() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith("[Aero_"))
            throw new IllegalArgumentException("not an Aero frame log line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(line.indexOf(']') + 1).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        long[] heap = heap(required(fields, "heap"));
        return new Sample(micros(fields, "frameMs"), micros(fields, "worldSaveMs"),
                micros(fields, "compileChunksMs"), micros(fields, "compileChunksMaxMs"),
                micros(fields, "gcTimeDeltaMs"), micros(fields, "worldSaveAllocMB"),
                micros(fields, "frameAllocMB"), whole(fields, "worldSaveSkipped"),
                heap[0], heap[1], heap[2]);
    }

    private static long[] heap(String raw) {
        String text = raw.endsWith("MB") ? raw.substring(0, raw.length() - 2) : raw;
        String[] parts = text.split("/");
        if (parts.length != 3) throw new IllegalArgumentException("invalid Aero heap " + raw);
        try {
            return new long[] { Long.parseLong(parts[0]), Long.parseLong(parts[1]),
                    Long.parseLong(parts[2]) };
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid Aero heap " + raw, error);
        }
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
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException("missing Aero field " + name);
        return value;
    }

    public static final class Sample {
        public final long frameUs, saveUs, compileUs, compileMaxUs, gcUs;
        public final long saveAllocMilliMb, frameAllocMilliMb, saveSkipped;
        public final long heapUsedMb, heapTotalMb, heapMaxMb;

        Sample(long frameUs, long saveUs, long compileUs, long compileMaxUs, long gcUs,
                long saveAllocMilliMb, long frameAllocMilliMb, long saveSkipped,
                long heapUsedMb, long heapTotalMb, long heapMaxMb) {
            this.frameUs = frameUs; this.saveUs = saveUs; this.compileUs = compileUs;
            this.compileMaxUs = compileMaxUs; this.gcUs = gcUs;
            this.saveAllocMilliMb = saveAllocMilliMb; this.frameAllocMilliMb = frameAllocMilliMb;
            this.saveSkipped = saveSkipped; this.heapUsedMb = heapUsedMb;
            this.heapTotalMb = heapTotalMb; this.heapMaxMb = heapMaxMb;
        }
    }
}
