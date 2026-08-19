package worldline.api;

import java.util.Objects;

/** One accepted same-dimension death respawn and restored local health. */
public final class RemoteRespawn {
    private final int dimension, healthBefore, healthAfter;
    public RemoteRespawn(int dimension, int healthBefore, int healthAfter) {
        if ((dimension != 0 && dimension != -1) || healthBefore != 0 || healthAfter != 20)
            throw new IllegalArgumentException("invalid remote respawn");
        this.dimension = dimension; this.healthBefore = healthBefore; this.healthAfter = healthAfter;
    }
    public int dimension() { return dimension; }
    public int healthBefore() { return healthBefore; }
    public int healthAfter() { return healthAfter; }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteRespawn)) return false;
        RemoteRespawn value = (RemoteRespawn) other; return dimension == value.dimension
                && healthBefore == value.healthBefore && healthAfter == value.healthAfter; }
    @Override public int hashCode() { return Objects.hash(dimension, healthBefore, healthAfter); }
}
