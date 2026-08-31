package worldline.api;

import java.util.regex.Pattern;

/**
 * Closed public family of a vanilla behavior. Progress milestones are not
 * families and must not appear here.
 */
public final class WorldlineFamily {
    public static final String HOSTILE = "hostile";
    public static final String PLAYER = "player";
    public static final String ENVIRONMENT = "environment";
    public static final String ITEM = "item";
    public static final String REDSTONE = "redstone";
    public static final String WORLD = "world";
    public static final String VEHICLE = "vehicle";
    public static final String ENTITY = "entity";
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,31}");
    private static final String[] ALL = {
        HOSTILE, PLAYER, ENVIRONMENT, ITEM, REDSTONE, WORLD, VEHICLE, ENTITY
    };

    private WorldlineFamily() {}

    public static String parse(String value) {
        String token = value == null ? "" : value.trim();
        if (!TOKEN.matcher(token).matches()) throw new IllegalArgumentException("invalid family");
        for (int i = 0; i < ALL.length; i++) if (ALL[i].equals(token)) return ALL[i];
        throw new IllegalArgumentException("unknown family " + token);
    }

    public static String[] values() { return ALL.clone(); }
}
