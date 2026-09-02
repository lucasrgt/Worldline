package worldline.testapi;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** One qualified chicken plus bounded optional laying and a player-thrown egg. */
public final class ChickenEggFamilyObservation {
    private final RemoteMobSpawn chicken;
    private final RemoteDroppedItem laidEgg;
    private final RemoteObjectSpawn thrownEgg;
    private final int actorEntityId;
    private final double platformX;
    private final double platformZ;

    public ChickenEggFamilyObservation(RemoteMobSpawn chicken, RemoteDroppedItem laidEgg,
            RemoteObjectSpawn thrownEgg, int actorEntityId, double platformX,
            double platformZ) {
        this.chicken = chicken;
        this.laidEgg = laidEgg;
        this.thrownEgg = thrownEgg;
        this.actorEntityId = actorEntityId;
        this.platformX = platformX;
        this.platformZ = platformZ;
    }

    public RemoteMobSpawn chicken() { return chicken; }
    public RemoteDroppedItem laidEgg() { return laidEgg; }
    public RemoteObjectSpawn thrownEgg() { return thrownEgg; }
    public int actorEntityId() { return actorEntityId; }
    public double platformX() { return platformX; }
    public double platformZ() { return platformZ; }
}
