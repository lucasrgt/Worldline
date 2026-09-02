package worldline.testapi;

import worldline.api.BlockPosition;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** One qualified zombie, skeleton, spider and creeper behavior matrix. */
public final class HostileBehaviorObservation {
    private final int actorEntityId;
    private final int nightTime;
    private final RemoteMobSpawn zombie;
    private final RemoteMobSpawn skeleton;
    private final RemoteObjectSpawn firstArrow;
    private final RemoteObjectSpawn secondArrow;
    private final boolean diamondArmorObserved;
    private final RemoteMobSpawn spider;
    private final boolean cobblestonePositiveY;
    private final boolean planksPositiveY;
    private final int cobblestoneBlockId;
    private final int planksBlockId;
    private final RemoteMobSpawn creeper;
    private final boolean proximityFuseObserved;
    private final RemoteExplosion creeperExplosion;
    private final BlockPosition dirtCell;
    private final BlockPosition woolCell;
    private final boolean dirtPersistedAir;
    private final boolean woolPersistedAir;

    public HostileBehaviorObservation(int actorEntityId, int nightTime,
            RemoteMobSpawn zombie, RemoteMobSpawn skeleton,
            RemoteObjectSpawn firstArrow, RemoteObjectSpawn secondArrow,
            boolean diamondArmorObserved, RemoteMobSpawn spider,
            boolean cobblestonePositiveY, boolean planksPositiveY,
            int cobblestoneBlockId, int planksBlockId, RemoteMobSpawn creeper,
            boolean proximityFuseObserved, RemoteExplosion creeperExplosion,
            BlockPosition dirtCell, BlockPosition woolCell,
            boolean dirtPersistedAir, boolean woolPersistedAir) {
        this.actorEntityId = actorEntityId;
        this.nightTime = nightTime;
        this.zombie = zombie;
        this.skeleton = skeleton;
        this.firstArrow = firstArrow;
        this.secondArrow = secondArrow;
        this.diamondArmorObserved = diamondArmorObserved;
        this.spider = spider;
        this.cobblestonePositiveY = cobblestonePositiveY;
        this.planksPositiveY = planksPositiveY;
        this.cobblestoneBlockId = cobblestoneBlockId;
        this.planksBlockId = planksBlockId;
        this.creeper = creeper;
        this.proximityFuseObserved = proximityFuseObserved;
        this.creeperExplosion = creeperExplosion;
        this.dirtCell = dirtCell;
        this.woolCell = woolCell;
        this.dirtPersistedAir = dirtPersistedAir;
        this.woolPersistedAir = woolPersistedAir;
    }

    public int actorEntityId() { return actorEntityId; }
    public int nightTime() { return nightTime; }
    public RemoteMobSpawn zombie() { return zombie; }
    public RemoteMobSpawn skeleton() { return skeleton; }
    public RemoteObjectSpawn firstArrow() { return firstArrow; }
    public RemoteObjectSpawn secondArrow() { return secondArrow; }
    public boolean diamondArmorObserved() { return diamondArmorObserved; }
    public RemoteMobSpawn spider() { return spider; }
    public boolean cobblestonePositiveY() { return cobblestonePositiveY; }
    public boolean planksPositiveY() { return planksPositiveY; }
    public int cobblestoneBlockId() { return cobblestoneBlockId; }
    public int planksBlockId() { return planksBlockId; }
    public RemoteMobSpawn creeper() { return creeper; }
    public boolean proximityFuseObserved() { return proximityFuseObserved; }
    public RemoteExplosion creeperExplosion() { return creeperExplosion; }
    public BlockPosition dirtCell() { return dirtCell; }
    public BlockPosition woolCell() { return woolCell; }
    public boolean dirtPersistedAir() { return dirtPersistedAir; }
    public boolean woolPersistedAir() { return woolPersistedAir; }
}
