package example.worldline;

/** External mod domain fixture with no Worldline implementation dependency. */
public final class ExternalModState {
    private boolean blockPlaced, itemRegistered, entitySpawned;

    public void reset() {
        blockPlaced = false;
        itemRegistered = false;
        entitySpawned = false;
    }
    public void placeBlock() { blockPlaced = true; }
    public void registerItem() { itemRegistered = true; }
    public void spawnEntity() { entitySpawned = true; }
    public String blockState() { return blockPlaced ? "placed" : "absent"; }
    public String itemCount() { return itemRegistered ? "1" : "0"; }
    public String entityCount() { return entitySpawned ? "1" : "0"; }
}
