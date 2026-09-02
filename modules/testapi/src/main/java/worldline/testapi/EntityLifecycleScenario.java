package worldline.testapi;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Driver-neutral actions used by the public persistent-entity lifecycle fixture. */
public interface EntityLifecycleScenario {
    RemoteMobSpawn materialize(int expectedLegacyType);
    RemoteMobMovement awaitMovement(int entityId);
    void kill(int entityId);
    RemoteMobDeath awaitDeath(int entityId);
    RemoteDroppedItem awaitDrop(RemoteItemStack expected);
}
