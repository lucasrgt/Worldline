package worldline.invariants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Closed aliases from Worldline trace field names to conservation rules.
 * Unknown or structural fields stay empty so diffs do not invent a cause.
 */
public final class InvariantFields {
    private static final Map<String, String> ALIAS = table();

    private InvariantFields() {}

    public static Map<String, String> aliases() { return ALIAS; }

    public static String rule(String field) {
        if (field == null || field.isEmpty()) return "";
        String rule = ALIAS.get(field);
        return rule == null ? "" : rule;
    }

    private static Map<String, String> table() {
        Map<String, String> aliases = new LinkedHashMap<String, String>();
        aliases.put("block64", BlockConservation.NAME);
        aliases.put("block65", BlockConservation.NAME);
        aliases.put("blockID", BlockConservation.NAME);
        aliases.put("health", HealthConservation.NAME);
        aliases.put("time", TimeMonotonic.NAME);
        aliases.put("worldTime", TimeMonotonic.NAME);
        aliases.put("entities", EntitySpawn.NAME);
        aliases.put("wear", DurabilityConservation.NAME);
        aliases.put("items", ItemConservation.NAME);
        return Collections.unmodifiableMap(aliases);
    }
}
