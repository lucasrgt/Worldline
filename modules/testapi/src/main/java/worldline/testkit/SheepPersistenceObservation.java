package worldline.testkit;

import worldline.api.RemoteDroppedItem;

/** Exact metadata and NBT boundaries from the qualified sheared-sheep restart sequence. */
public final class SheepPersistenceObservation {
    private final int dyedMetadata;
    private final int shearedMetadata;
    private final int persistedMetadata;
    private final int controlMetadata;
    private final boolean repeatWoolObserved;
    private final boolean nbtShearedBefore;
    private final boolean nbtShearedAfter;
    private final int mutatedMetadata;
    private final int reshearedMetadata;
    private final int changedSheepCount;
    private final int restarts;
    private final RemoteDroppedItem recoveredWool;

    public SheepPersistenceObservation(int dyedMetadata, int shearedMetadata,
            int persistedMetadata, int controlMetadata, boolean repeatWoolObserved,
            boolean nbtShearedBefore, boolean nbtShearedAfter, int mutatedMetadata,
            int reshearedMetadata, int changedSheepCount, int restarts,
            RemoteDroppedItem recoveredWool) {
        this.dyedMetadata = dyedMetadata;
        this.shearedMetadata = shearedMetadata;
        this.persistedMetadata = persistedMetadata;
        this.controlMetadata = controlMetadata;
        this.repeatWoolObserved = repeatWoolObserved;
        this.nbtShearedBefore = nbtShearedBefore;
        this.nbtShearedAfter = nbtShearedAfter;
        this.mutatedMetadata = mutatedMetadata;
        this.reshearedMetadata = reshearedMetadata;
        this.changedSheepCount = changedSheepCount;
        this.restarts = restarts;
        this.recoveredWool = recoveredWool;
    }

    public int dyedMetadata() { return dyedMetadata; }
    public int shearedMetadata() { return shearedMetadata; }
    public int persistedMetadata() { return persistedMetadata; }
    public int controlMetadata() { return controlMetadata; }
    public boolean repeatWoolObserved() { return repeatWoolObserved; }
    public boolean nbtShearedBefore() { return nbtShearedBefore; }
    public boolean nbtShearedAfter() { return nbtShearedAfter; }
    public int mutatedMetadata() { return mutatedMetadata; }
    public int reshearedMetadata() { return reshearedMetadata; }
    public int changedSheepCount() { return changedSheepCount; }
    public int restarts() { return restarts; }
    public RemoteDroppedItem recoveredWool() { return recoveredWool; }
}
