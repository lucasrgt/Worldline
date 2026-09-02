package worldline.testapi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Public expected transition for one routed tick-policy claim. */
public final class BlockTickPolicyScenario {
    private static final BlockConformanceTemplate TEMPLATE =
            new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE);
    private final String id;
    private final BlockConformanceCase claim;
    private final BlockTickPolicyMechanism mechanism;
    private final String initial;
    private final String effect;
    private final boolean persisted;

    public BlockTickPolicyScenario(String id, BlockConformanceCase claim,
            BlockTickPolicyMechanism mechanism, String initial, String effect,
            boolean persisted) {
        this.id = BlockTickPolicyObservation.token(id, "scenario id");
        this.claim = Objects.requireNonNull(claim, "claim");
        if (!"tick-policy".equals(claim.template().id())) {
            throw new IllegalArgumentException("claim does not target tick-policy");
        }
        this.mechanism = Objects.requireNonNull(mechanism, "mechanism");
        this.initial = BlockTickPolicyObservation.token(initial, "initial");
        this.effect = BlockTickPolicyObservation.token(effect, "effect");
        this.persisted = persisted;
    }

    public BlockTickPolicyScenario(String id, String subject, List<String> archetypes,
            boolean singular, BlockTickPolicyMechanism mechanism, String initial,
            String effect, boolean persisted) {
        this(id, claim(subject, archetypes, singular), mechanism, initial, effect, persisted);
    }

    public String id() { return id; }
    public BlockConformanceCase claim() { return claim; }
    public BlockTickPolicyMechanism mechanism() { return mechanism; }
    public String initial() { return initial; }
    public String effect() { return effect; }
    public boolean persisted() { return persisted; }

    private static BlockConformanceCase claim(String subject, List<String> archetypes,
            boolean singular) {
        BlockConformanceProfile profile = new BlockConformanceProfile(subject, archetypes,
                singular, Collections.<String, ConformanceLayer>emptyMap());
        return new BlockConformancePlan(Collections.singletonList(profile),
                Collections.singletonList(TEMPLATE)).caseFor(subject, "tick-policy");
    }
}
