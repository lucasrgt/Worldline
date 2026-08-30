package worldline.modloader.testkit;

import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.GamePosition;

/** Strict state row received from one controlled legacy client tick boundary. */
final class LegacySnapshot {
    final String loader, session, username;
    final long tick, time;
    final int entity, health, selected;
    final GamePosition position;

    private LegacySnapshot(Map<String, String> values) {
        loader = required(values, "loader"); session = required(values, "session");
        username = required(values, "username"); tick = number(values, "tick");
        time = number(values, "time"); entity = integer(values, "entity");
        health = integer(values, "health"); selected = integer(values, "selected");
        position = new GamePosition(decimal(values, "x"), decimal(values, "y"), decimal(values, "z"));
    }

    static LegacySnapshot parse(String line, String kind, String loader, String session) {
        require(line != null, "legacy control channel closed");
        String[] tokens = line.trim().split(" +");
        require(tokens.length == 12 && kind.equals(tokens[0]), "invalid legacy state message: " + line);
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (int index = 1; index < tokens.length; index++) {
            int split = tokens[index].indexOf('=');
            require(split > 0 && split < tokens[index].length() - 1, "invalid state token");
            require(values.put(tokens[index].substring(0, split),
                    tokens[index].substring(split + 1)) == null, "duplicate state token");
        }
        require(values.size() == 11 && loader.equals(required(values, "loader"))
                && session.equals(required(values, "session")), "legacy session identity drifted");
        return new LegacySnapshot(values);
    }

    private static int integer(Map<String, String> values, String key) {
        return Integer.parseInt(required(values, key));
    }
    private static long number(Map<String, String> values, String key) {
        return Long.parseLong(required(values, key));
    }
    private static double decimal(Map<String, String> values, String key) {
        return Double.parseDouble(required(values, key));
    }
    private static String required(Map<String, String> values, String key) {
        String value = values.get(key); require(value != null && !value.isEmpty(), "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
