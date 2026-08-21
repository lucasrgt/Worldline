package worldline.api;

import java.util.Objects;

/** Ordered local hurt status and health transition without inferred attacker identity. */
public final class RemoteIncomingHit {
    private final String victim; private final int victimEntityId;
    private final int healthBefore, healthAfter;
    public RemoteIncomingHit(String victim, int victimEntityId, int healthBefore, int healthAfter) {
        if (!name(victim) || victimEntityId < 0 || healthBefore < 1
                || healthBefore > 20 || healthAfter < 0 || healthAfter >= healthBefore)
            throw new IllegalArgumentException("invalid incoming hit");
        this.victim = victim;
        this.victimEntityId = victimEntityId; this.healthBefore = healthBefore; this.healthAfter = healthAfter;
    }
    public String victim() { return victim; } public int victimEntityId() { return victimEntityId; }
    public int healthBefore() { return healthBefore; } public int healthAfter() { return healthAfter; }
    public int damage() { return healthBefore - healthAfter; }
    private static boolean name(String value) { return value != null && value.matches("[A-Za-z0-9_]{1,16}"); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteIncomingHit)) return false;
        RemoteIncomingHit value = (RemoteIncomingHit) other; return victim.equals(value.victim)
                && victimEntityId == value.victimEntityId && healthBefore == value.healthBefore
                && healthAfter == value.healthAfter; }
    @Override public int hashCode() { return Objects.hash(victim, victimEntityId, healthBefore, healthAfter); }
}
