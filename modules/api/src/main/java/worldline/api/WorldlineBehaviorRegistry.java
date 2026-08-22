package worldline.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Deterministic construction and lookup mechanics for the public behavior catalog. */
final class WorldlineBehaviorRegistry {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Map<String, WorldlineBehavior> DEFINITIONS =
            new LinkedHashMap<String, WorldlineBehavior>();

    private WorldlineBehaviorRegistry() {}

    static WorldlineBehavior define(String token, String family, String subject) {
        if (token == null || !TOKEN.matcher(token).matches() || looksLikeProgress(token))
            throw new IllegalArgumentException("invalid behavior token");
        if (subject == null || subject.isEmpty() || subject.indexOf('\n') >= 0 || subject.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid behavior subject");
        WorldlineBehavior value = new WorldlineBehavior(token, WorldlineFamily.parse(family), subject);
        if (DEFINITIONS.put(token, value) != null) throw new IllegalStateException("duplicate behavior " + token);
        return value;
    }

    static Map<String, WorldlineBehavior> freeze() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, WorldlineBehavior>(DEFINITIONS));
    }

    static WorldlineBehavior require(Map<String, WorldlineBehavior> values, String tokenOrAtlasOrProgress) {
        if (tokenOrAtlasOrProgress == null || tokenOrAtlasOrProgress.trim().isEmpty())
            throw new IllegalArgumentException("unknown behavior");
        String raw = tokenOrAtlasOrProgress.trim();
        if (raw.startsWith("atlas.scenario.")) raw = raw.substring("atlas.scenario.".length());
        else if (looksLikeProgress(raw)) raw = tokenOfProgress(raw);
        WorldlineBehavior value = values.get(raw);
        if (value == null) throw new IllegalArgumentException("unknown behavior " + tokenOrAtlasOrProgress);
        return value;
    }

    static String tokenOfProgress(String progressId) {
        String id = progressId.trim();
        if (looksLikeProgress(id)) id = id.substring(id.indexOf('-') + 1);
        if (id.endsWith("-set")) id = id.substring(0, id.length() - 4);
        if (!TOKEN.matcher(id).matches()) throw new IllegalArgumentException("invalid progress id");
        return id;
    }

    private static boolean looksLikeProgress(String raw) {
        if (raw.length() < 4 || raw.charAt(0) != 'm') return false;
        int dash = raw.indexOf('-');
        if (dash < 2) return false;
        for (int i = 1; i < dash; i++) if (raw.charAt(i) < '0' || raw.charAt(i) > '9') return false;
        return true;
    }
}
