package worldline.aero;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict parser for the M15 chunk vertex-stream signature. */
public final class AeroChunkGeometry {
    private static final String PREFIX = "[WorldlineChunkGeometry]";
    private AeroChunkGeometry() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith(PREFIX))
            throw new IllegalArgumentException("not a Worldline geometry line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(PREFIX.length()).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        int x = integer(fields, "x"), y = integer(fields, "y"), z = integer(fields, "z");
        int vertices = integer(fields, "vertices");
        String hash = required(fields, "hash");
        boolean layer0 = bool(fields, "layer0"), layer1 = bool(fields, "layer1");
        return new Sample(x + "," + y + "," + z, vertices, hash, layer0, layer1);
    }

    private static int integer(Map<String, String> fields, String name) {
        try { return Integer.parseInt(required(fields, name)); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid geometry field " + name, error);
        }
    }

    private static boolean bool(Map<String, String> fields, String name) {
        String value = required(fields, name);
        if (!value.equals("true") && !value.equals("false"))
            throw new IllegalArgumentException("invalid geometry field " + name);
        return Boolean.parseBoolean(value);
    }

    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("missing geometry field " + name);
        return value;
    }

    public static final class Sample {
        public final String key, hash;
        public final int vertices;
        public final boolean layer0Empty, layer1Empty;

        Sample(String key, int vertices, String hash, boolean layer0Empty, boolean layer1Empty) {
            this.key = key; this.vertices = vertices; this.hash = hash;
            this.layer0Empty = layer0Empty; this.layer1Empty = layer1Empty;
        }

        public String signature() {
            return vertices + ":" + hash + ":" + layer0Empty + ":" + layer1Empty;
        }
    }
}
