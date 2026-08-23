package worldline.stationapi;

import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.GamePosition;

/** Strict state message received at the boundary before each controlled tick. */
final class StationApiSnapshot {
    final String session, username;
    final long tick, time;
    final int entityId, health, selected;
    final GamePosition position;

    private StationApiSnapshot(String session, String username, long tick, long time,
            int entityId, int health, int selected, GamePosition position) {
        this.session = session; this.username = username; this.tick = tick; this.time = time;
        this.entityId = entityId; this.health = health; this.selected = selected;
        this.position = position;
    }

    static StationApiSnapshot parse(String line, String expectedKind, String expectedSession) {
        if (line == null) throw new IllegalStateException("StationAPI control channel closed");
        String[] tokens = line.trim().split(" +");
        require(tokens.length == 11 && tokens[0].equals(expectedKind),
                "invalid StationAPI state message: " + line);
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (int index = 1; index < tokens.length; index++) {
            int split = tokens[index].indexOf('=');
            require(split > 0 && split < tokens[index].length() - 1, "invalid state token");
            require(values.put(tokens[index].substring(0, split),
                    tokens[index].substring(split + 1)) == null, "duplicate state token");
        }
        require(values.size() == 10 && expectedSession.equals(required(values, "session")),
                "StationAPI session identity drifted");
        return new StationApiSnapshot(expectedSession, required(values, "username"),
                number(values, "tick"), number(values, "time"), integer(values, "entity"),
                integer(values, "health"), integer(values, "selected"), new GamePosition(
                        decimal(values, "x"), decimal(values, "y"), decimal(values, "z")));
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
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
