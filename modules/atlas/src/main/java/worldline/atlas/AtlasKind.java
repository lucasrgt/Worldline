package worldline.atlas;

/** Closed Atlas record kinds. Longer names are matched first when parsing IDs. */
public final class AtlasKind {
    public static final String ROLE = "role";
    public static final String BOUNDARY = "boundary";
    public static final String INVARIANT = "invariant";
    public static final String EXPERIMENT = "experiment";
    public static final String SCENARIO = "scenario";
    public static final String CLAIM = "claim";
    public static final String SUBSYSTEM = "subsystem";
    public static final String COVERAGE_UNIT = "coverage-unit";
    public static final String HYPOTHESIS = "hypothesis";
    public static final String FIELD = "field";
    public static final String LOADER = "loader";
    public static final String API = "api";
    public static final String MAPPING_SET = "mapping-set";
    public static final String NAMESPACE = "namespace";
    public static final String ECOSYSTEM_CLAIM = "ecosystem-claim";
    private static final String[] ALL = { ECOSYSTEM_CLAIM, COVERAGE_UNIT, MAPPING_SET,
            HYPOTHESIS, EXPERIMENT, INVARIANT, SUBSYSTEM, NAMESPACE, SCENARIO, CLAIM, BOUNDARY,
            LOADER, FIELD, ROLE, API };

    private AtlasKind() {}

    public static String parse(String value) {
        for (String kind : ALL) {
            if (kind.equals(value)) return kind;
        }
        throw new IllegalArgumentException("unsupported kind " + value);
    }

    public static String ofId(String id) {
        if (id == null || !id.startsWith("atlas.")) throw new IllegalArgumentException("id");
        String rest = id.substring(6);
        for (String kind : ALL) {
            if (rest.startsWith(kind + ".")) return kind;
        }
        throw new IllegalArgumentException("unsupported kind in " + id);
    }

    public static String token(String id) {
        String kind = ofId(id);
        return id.substring(6 + kind.length() + 1);
    }

    public static String[] values() { return ALL.clone(); }
}
