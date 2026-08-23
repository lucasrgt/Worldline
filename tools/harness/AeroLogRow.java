import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Strict parser for the common non-negative Aero frame vocabulary. */
final class AeroLogRow {
    private static final List<String> TIMINGS = List.of(
            "frameMs", "compileChunksMs", "compileChunksMaxMs", "gcTimeDeltaMs");
    private static final List<String> COUNTERS = List.of("compileChunksCalls",
            "compileChunksSkipped", "compileBudgetSkipped", "batchQueued", "cellQueued",
            "beViewCulled");
    private final Map<String, String> fields;

    private AeroLogRow(Map<String, String> fields) { this.fields = fields; }

    static AeroLogRow parse(String row) {
        require(row.startsWith("[Aero_") && row.indexOf(']') > 6, "invalid Aero row type");
        Map<String, String> fields = new HashMap<>();
        for (String token : row.substring(row.indexOf(']') + 1).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                require(fields.put(token.substring(0, equals), token.substring(equals + 1)) == null,
                        "duplicate Aero field");
        }
        AeroLogRow parsed = new AeroLogRow(Map.copyOf(fields)); parsed.validate(); return parsed;
    }

    long whole(String name) {
        try { return Long.parseLong(required(name)); }
        catch (NumberFormatException error) {
            throw new IllegalStateException("invalid Aero counter " + name, error);
        }
    }

    private void validate() {
        for (String name : TIMINGS) require(decimal(name).signum() >= 0,
                "negative Aero timing " + name);
        for (String name : COUNTERS) require(whole(name) >= 0, "negative Aero counter " + name);
        require(whole("visibleChunks") > 0, "Aero row has no visible chunks");
    }

    private BigDecimal decimal(String name) {
        try { return new BigDecimal(required(name)); }
        catch (NumberFormatException error) {
            throw new IllegalStateException("invalid Aero timing " + name, error);
        }
    }

    private String required(String name) {
        String value = fields.get(name); require(value != null, "missing Aero field " + name);
        return value;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
