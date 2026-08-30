package worldline.testkit;

import java.util.Objects;

/** Normalized oracle observation for one tick-policy case. */
public final class BlockTickPolicyObservation {
    private final String id;
    private final BlockTickPolicyMechanism mechanism;
    private final String initial;
    private final String effect;
    private final boolean persisted;

    public BlockTickPolicyObservation(String id, BlockTickPolicyMechanism mechanism,
            String initial, String effect, boolean persisted) {
        this.id = token(id, "id");
        this.mechanism = Objects.requireNonNull(mechanism, "mechanism");
        this.initial = token(initial, "initial");
        this.effect = token(effect, "effect");
        this.persisted = persisted;
    }

    public String id() { return id; }
    public BlockTickPolicyMechanism mechanism() { return mechanism; }
    public String initial() { return initial; }
    public String effect() { return effect; }
    public boolean persisted() { return persisted; }

    static String token(String value, String role) {
        if (value == null || !value.matches("[a-z0-9][a-z0-9:+.>@=_-]{0,191}")) {
            throw new IllegalArgumentException("invalid tick-policy " + role);
        }
        return value;
    }
}
