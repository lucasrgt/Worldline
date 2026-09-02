package worldline.testapi;

import java.util.Objects;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobSpawn;

/** One causally killed slime with its decoded size and optional Packet21 result. */
public final class SlimeDropObservation {
    private final RemoteMobSpawn slime;
    private final int size;
    private final RemoteMobDeath death;
    private final RemoteDroppedItem drop;

    public SlimeDropObservation(RemoteMobSpawn slime, int size,
            RemoteMobDeath death, RemoteDroppedItem drop) {
        if (size < 1) throw new IllegalArgumentException("slime size");
        this.slime = Objects.requireNonNull(slime, "slime");
        this.size = size;
        this.death = Objects.requireNonNull(death, "death");
        this.drop = drop;
    }

    public RemoteMobSpawn slime() { return slime; }
    public int size() { return size; }
    public RemoteMobDeath death() { return death; }
    public RemoteDroppedItem drop() { return drop; }
}
