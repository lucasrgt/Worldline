package worldline.testapi;

/** Caller-owned native dynamics case with an explicit causal contrast and Census route. */
public final class NativeEntityDynamicsCase {
    public enum Rule {
        MOTION("movement-policy"), HANGING_MOTION("movement-policy"), IMMERSION("environment-response"),
        SHORT_IMMERSION("environment-response"),
        SUPPORT_ARREST("environment-response"), SUPPORT_SETTLE("environment-response"),
        RAIL_DRAG("environment-response"), COOLDOWN("tick-lifecycle"),
        ITEM_EXPIRY("tick-lifecycle"), PROJECTILE_EXPIRY("tick-lifecycle"),
        SUPPORT_RECHECK("tick-lifecycle");
        private final String template;
        Rule(String template) { this.template = template; }
        public String template() { return template; }
    }

    private final String subject, runtimeClass, layer;
    private final int legacyId;
    private final Rule rule;

    public NativeEntityDynamicsCase(int legacyId, String runtimeClass, String layer, Rule rule) {
        if (legacyId < 1 || legacyId > 999 || runtimeClass == null
                || !runtimeClass.matches("Entity[A-Za-z0-9]+") || rule == null
                || !("ARCHETYPE".equals(layer) || "SINGULAR".equals(layer))) {
            throw new IllegalArgumentException("native entity dynamics case");
        }
        this.legacyId = legacyId;
        subject = "b1.7.3:entity/" + String.format(java.util.Locale.ROOT, "%03d", legacyId);
        this.runtimeClass = runtimeClass; this.layer = layer; this.rule = rule;
    }
    public int legacyId() { return legacyId; }
    public String subject() { return subject; }
    public String runtimeClass() { return runtimeClass; }
    public String layer() { return layer; }
    public Rule rule() { return rule; }
    public String claimId() { return subject + '#' + rule.template(); }
    public String canonical() { return claimId() + '|' + layer + '|' + runtimeClass + '|' + rule; }
}
