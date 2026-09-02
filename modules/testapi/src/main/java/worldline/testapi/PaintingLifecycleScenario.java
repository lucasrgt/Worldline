package worldline.testapi;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePaintingSpawn;

/** Driver-neutral actions and observations for the complete painting mini-subsystem. */
public interface PaintingLifecycleScenario {
    RemotePaintingSpawn materialize(PaintingSpawnExpectation expectation);
    void removeSupport(RemotePaintingSpawn painting);
    int awaitDestroy(int entityId);
    RemoteDroppedItem awaitDrop(RemoteItemStack expected);
}
