package worldline.aero;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict parser for the M16 frozen-tick framebuffer evidence. */
public final class AeroFrameOracle {
    private static final String PREFIX = "[WorldlineFrameOracle]";
    private AeroFrameOracle() {}

    public static Sample parse(String line) {
        if (line == null || !line.startsWith(PREFIX) || line.contains(" timeout "))
            throw new IllegalArgumentException("not a completed frame oracle line");
        Map<String, String> fields = new LinkedHashMap<>();
        for (String token : line.substring(PREFIX.length()).trim().split(" +")) {
            int equals = token.indexOf('=');
            if (equals > 0 && equals < token.length() - 1)
                fields.put(token.substring(0, equals), token.substring(equals + 1));
        }
        int tick = integer(fields, "tick"), frames = integer(fields, "frames");
        int stable = integer(fields, "stable"), width = integer(fields, "width");
        int height = integer(fields, "height");
        if (!"true".equals(required(fields, "globalReady")))
            throw new IllegalArgumentException("frame was not globally ready");
        if (!"true".equals(required(fields, "visibleReady")))
            throw new IllegalArgumentException("frame was not visibly ready");
        String hash = required(fields, "sha256");
        if (!hash.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("invalid frame hash");
        return new Sample(tick, frames, stable, width, height, hash);
    }

    private static int integer(Map<String, String> fields, String name) {
        try { return Integer.parseInt(required(fields, name)); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException("invalid frame field " + name, error);
        }
    }
    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("missing frame field " + name);
        return value;
    }

    public static final class Sample {
        public final int tick, frames, stable, width, height;
        public final String hash;
        Sample(int tick, int frames, int stable, int width, int height, String hash) {
            this.tick = tick; this.frames = frames; this.stable = stable;
            this.width = width; this.height = height; this.hash = hash;
        }
    }
}
