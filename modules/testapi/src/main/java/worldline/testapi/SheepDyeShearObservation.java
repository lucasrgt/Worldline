package worldline.testapi;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteMobSpawn;

/** Two living sheep, their dye inputs, and the first qualified wool drops. */
public final class SheepDyeShearObservation {
    private final RemoteMobSpawn first;
    private final RemoteMobSpawn second;
    private final int firstDyeDamage;
    private final int secondDyeDamage;
    private final RemoteDroppedItem firstWool;
    private final RemoteDroppedItem secondWool;
    private final boolean deathObserved;

    public SheepDyeShearObservation(RemoteMobSpawn first, RemoteMobSpawn second,
            int firstDyeDamage, int secondDyeDamage, RemoteDroppedItem firstWool,
            RemoteDroppedItem secondWool, boolean deathObserved) {
        this.first = first;
        this.second = second;
        this.firstDyeDamage = firstDyeDamage;
        this.secondDyeDamage = secondDyeDamage;
        this.firstWool = firstWool;
        this.secondWool = secondWool;
        this.deathObserved = deathObserved;
    }

    public RemoteMobSpawn first() { return first; }
    public RemoteMobSpawn second() { return second; }
    public int firstDyeDamage() { return firstDyeDamage; }
    public int secondDyeDamage() { return secondDyeDamage; }
    public RemoteDroppedItem firstWool() { return firstWool; }
    public RemoteDroppedItem secondWool() { return secondWool; }
    public boolean deathObserved() { return deathObserved; }
}
