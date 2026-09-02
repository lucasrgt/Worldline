package worldline.testapi;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteObjectSpawn;

/** One qualified support-removal, Packet23 fall, landing and reload sequence. */
public final class FallingSandLifecycleObservation {
    private final RemoteObjectSpawn falling;
    private final int actorEntityId;
    private final BlockPosition lower;
    private final BlockPosition upper;
    private final BlockState stableLower;
    private final BlockState stableUpper;
    private final BlockState openedLower;
    private final BlockState landedLower;
    private final BlockState clearedUpper;
    private final BlockState persistedLower;
    private final BlockState persistedUpper;
    private final int fixtureTicks;
    private final int gravityTicks;

    public FallingSandLifecycleObservation(RemoteObjectSpawn falling, int actorEntityId,
            BlockPosition lower, BlockPosition upper, BlockState stableLower,
            BlockState stableUpper, BlockState openedLower, BlockState landedLower,
            BlockState clearedUpper, BlockState persistedLower, BlockState persistedUpper,
            int fixtureTicks, int gravityTicks) {
        this.falling = falling;
        this.actorEntityId = actorEntityId;
        this.lower = lower;
        this.upper = upper;
        this.stableLower = stableLower;
        this.stableUpper = stableUpper;
        this.openedLower = openedLower;
        this.landedLower = landedLower;
        this.clearedUpper = clearedUpper;
        this.persistedLower = persistedLower;
        this.persistedUpper = persistedUpper;
        this.fixtureTicks = fixtureTicks;
        this.gravityTicks = gravityTicks;
    }

    public RemoteObjectSpawn falling() { return falling; }
    public int actorEntityId() { return actorEntityId; }
    public BlockPosition lower() { return lower; }
    public BlockPosition upper() { return upper; }
    public BlockState stableLower() { return stableLower; }
    public BlockState stableUpper() { return stableUpper; }
    public BlockState openedLower() { return openedLower; }
    public BlockState landedLower() { return landedLower; }
    public BlockState clearedUpper() { return clearedUpper; }
    public BlockState persistedLower() { return persistedLower; }
    public BlockState persistedUpper() { return persistedUpper; }
    public int fixtureTicks() { return fixtureTicks; }
    public int gravityTicks() { return gravityTicks; }
}
