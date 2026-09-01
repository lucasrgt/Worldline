package worldline.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import worldline.api.WorldlineBehavior;

/** Immutable binding from extension providers to one executable comparison contract. */
public final class ExtensionContract {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern HEX = Pattern.compile("[a-f0-9]{64}");
    private final String id, subjectId, fixtureId, oracleId, vanillaBehavior;
    private final List<String> actionIds, observationIds;
    private final Set<ExtensionMode> modes;
    private final String conformanceSignature, customSignature;

    private ExtensionContract(Builder builder) {
        id = token(builder.id, "contract id");
        subjectId = named(builder.subjectId, "subject id");
        fixtureId = token(builder.fixtureId, "fixture id");
        oracleId = token(builder.oracleId, "oracle id");
        actionIds = tokens(builder.actionIds, "actions");
        observationIds = tokens(builder.observationIds, "observations");
        if (builder.modes.isEmpty()) throw new IllegalArgumentException("contract modes");
        modes = Collections.unmodifiableSet(EnumSet.copyOf(builder.modes));
        vanillaBehavior = builder.vanillaBehavior == null ? null
                : WorldlineBehavior.require(optionalToken(builder.vanillaBehavior,
                        "vanilla behavior")).token();
        conformanceSignature = optionalHex(builder.conformanceSignature, "conformance signature");
        customSignature = optionalHex(builder.customSignature, "custom signature");
        if ((modes.contains(ExtensionMode.CONFORMANCE) || modes.contains(ExtensionMode.DIFFERENTIAL))
                && (vanillaBehavior == null || conformanceSignature == null))
            throw new IllegalArgumentException("vanilla baseline required");
        if (modes.contains(ExtensionMode.CUSTOM_CONTRACT) && customSignature == null)
            throw new IllegalArgumentException("custom baseline required");
    }

    public static Builder builder(String id, String subjectId) { return new Builder(id, subjectId); }
    public String id() { return id; }
    public String subjectId() { return subjectId; }
    public String fixtureId() { return fixtureId; }
    public String oracleId() { return oracleId; }
    public List<String> actionIds() { return actionIds; }
    public List<String> observationIds() { return observationIds; }
    public Set<ExtensionMode> modes() { return modes; }
    public String vanillaBehavior() { return vanillaBehavior; }
    public String expectedSignature(ExtensionMode mode) {
        return mode == ExtensionMode.CUSTOM_CONTRACT ? customSignature : conformanceSignature;
    }

    public static final class Builder {
        private final String id, subjectId;
        private String fixtureId, oracleId, vanillaBehavior, conformanceSignature, customSignature;
        private final List<String> actionIds = new ArrayList<String>();
        private final List<String> observationIds = new ArrayList<String>();
        private final Set<ExtensionMode> modes = EnumSet.noneOf(ExtensionMode.class);
        private Builder(String id, String subjectId) { this.id = id; this.subjectId = subjectId; }
        public Builder fixture(String value) { fixtureId = value; return this; }
        public Builder action(String value) { actionIds.add(value); return this; }
        public Builder observation(String value) { observationIds.add(value); return this; }
        public Builder oracle(String value) { oracleId = value; return this; }
        public Builder mode(ExtensionMode value) { modes.add(value); return this; }
        public Builder vanilla(String behavior, String signature) {
            vanillaBehavior = behavior; conformanceSignature = signature; return this;
        }
        public Builder custom(String signature) { customSignature = signature; return this; }
        public ExtensionContract build() { return new ExtensionContract(this); }
    }

    private static String token(String value, String label) {
        if (value == null || !TOKEN.matcher(value).matches()) throw new IllegalArgumentException(label);
        return value;
    }
    private static String named(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label);
        return value.trim();
    }
    private static String optionalToken(String value, String label) {
        return value == null ? null : token(value, label);
    }
    private static String optionalHex(String value, String label) {
        if (value != null && !HEX.matcher(value).matches()) throw new IllegalArgumentException(label);
        return value;
    }
    private static List<String> tokens(List<String> source, String label) {
        if (source.isEmpty()) throw new IllegalArgumentException(label);
        List<String> copy = new ArrayList<String>();
        for (String value : source) {
            String clean = token(value, label); if (copy.contains(clean))
                throw new IllegalArgumentException("duplicate " + label); copy.add(clean);
        }
        return Collections.unmodifiableList(copy);
    }
}
