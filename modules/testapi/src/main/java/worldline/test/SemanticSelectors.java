package worldline.test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.test.SemanticSelector.Access;
import worldline.test.SemanticSelector.Kind;
import worldline.test.SemanticSelector.Stability;

/** Small 0.x catalog containing only identifiers backed by promoted evidence. */
final class SemanticSelectors {
    private static final Map<String, SemanticSelector> VALUES;
    static {
        Map<String, SemanticSelector> values = new LinkedHashMap<>();
        add(values, "b1.7.3:air", Kind.BLOCK, 0, 0, "M308", Access.READ_WRITE);
        add(values, "b1.7.3:glass", Kind.BLOCK, 20, 0, "M434", Access.READ_WRITE);
        add(values, "b1.7.3:diamond_sword", Kind.ITEM, 276, 0, "M318", Access.READ_ONLY);
        add(values, "b1.7.3:pig", Kind.ENTITY, 90, -1, "M141", Access.READ_ONLY);
        add(values, "b1.7.3:health", Kind.PACKET, 8, -1, "M469", Access.READ_ONLY);
        VALUES = Collections.unmodifiableMap(values);
    }
    private SemanticSelectors() {}
    static SemanticSelector require(String key, Kind kind) {
        SemanticSelector value = VALUES.get(key);
        if (value == null) throw new IllegalArgumentException("WLTEST E2101: no promoted mapping for " + key);
        if (value.kind() != kind) throw new IllegalArgumentException("WLTEST E2102: " + key
                + " is " + value.kind().name().toLowerCase() + ", not " + kind.name().toLowerCase());
        return value;
    }
    static String describe(Object input) {
        if (!(input instanceof worldline.api.BlockState)) return String.valueOf(input);
        worldline.api.BlockState state = (worldline.api.BlockState) input;
        for (SemanticSelector value : VALUES.values()) if (value.kind() == Kind.BLOCK
                && value.legacyId() == state.legacyId() && value.metadata() == state.metadata())
            return value.toString();
        return String.valueOf(input);
    }
    private static void add(Map<String, SemanticSelector> values, String key, Kind kind,
            int id, int metadata, String evidence, Access access) {
        values.put(key, new SemanticSelector(key, kind, id, metadata, evidence, access, Stability.STABLE));
    }
}
