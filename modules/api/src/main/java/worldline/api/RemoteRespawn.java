package worldline.api;

import java.util.Objects;

/** One accepted death respawn with exact dimension transition and restored health. */
public final class RemoteRespawn {
    private final int dimensionBefore, dimensionAfter, healthBefore, healthAfter;
    public RemoteRespawn(int dimension, int healthBefore, int healthAfter) {
        this(dimension, dimension, healthBefore, healthAfter);
    }
    public RemoteRespawn(int dimensionBefore, int dimensionAfter, int healthBefore, int healthAfter) {
        if ((dimensionBefore != 0 && dimensionBefore != -1) || dimensionAfter != 0
                || healthBefore != 0 || healthAfter != 20)
            throw new IllegalArgumentException("invalid remote respawn");
        this.dimensionBefore = dimensionBefore; this.dimensionAfter = dimensionAfter;
        this.healthBefore = healthBefore; this.healthAfter = healthAfter;
    }
    public int dimension() { return dimensionAfter; }
    public int dimensionBefore() { return dimensionBefore; }
    public int dimensionAfter() { return dimensionAfter; }
    public int healthBefore() { return healthBefore; }
    public int healthAfter() { return healthAfter; }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteRespawn)) return false;
        RemoteRespawn value = (RemoteRespawn) other; return dimensionBefore == value.dimensionBefore
                && dimensionAfter == value.dimensionAfter
                && healthBefore == value.healthBefore && healthAfter == value.healthAfter; }
    @Override public int hashCode() { return Objects.hash(dimensionBefore, dimensionAfter, healthBefore, healthAfter); }
}
