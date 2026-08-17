package worldline.api;

/** One tick sample for invariant observers. */
public final class InvariantSample {
    private final ItemCensus items, blocks, imported, importedBlocks;
    private final EntityCensus entities, importedEntities;
    private final WearCensus wear;
    private final long time;
    private final int health;
    private final boolean peaceful;

    private InvariantSample(ItemCensus items, ItemCensus blocks, EntityCensus entities,
            ItemCensus imported, ItemCensus importedBlocks, EntityCensus importedEntities,
            WearCensus wear, long time, int health, boolean peaceful) {
        this.items = items;
        this.blocks = blocks;
        this.entities = entities;
        this.imported = imported;
        this.importedBlocks = importedBlocks;
        this.importedEntities = importedEntities;
        this.wear = wear;
        this.time = time;
        this.health = health;
        this.peaceful = peaceful;
    }

    public static InvariantSample of(ItemCensus items) {
        return of(items, ItemCensus.empty());
    }

    public static InvariantSample of(ItemCensus items, ItemCensus blocks) {
        return of(items, blocks, EntityCensus.empty(), ItemCensus.empty(), 0L);
    }

    public static InvariantSample of(ItemCensus items, ItemCensus blocks, EntityCensus entities,
            ItemCensus imported, long time) {
        return of(items, blocks, entities, imported, ItemCensus.empty(), EntityCensus.empty(),
                WearCensus.empty(), time, 20, true);
    }

    public static InvariantSample of(ItemCensus items, ItemCensus blocks, EntityCensus entities,
            ItemCensus imported, ItemCensus importedBlocks, EntityCensus importedEntities,
            WearCensus wear, long time, int health, boolean peaceful) {
        if (items == null || blocks == null || entities == null || imported == null
                || importedBlocks == null || importedEntities == null || wear == null) {
            throw new NullPointerException("sample");
        }
        if (time < 0L || health < 0) throw new IllegalArgumentException("sample");
        return new InvariantSample(items, blocks, entities, imported, importedBlocks, importedEntities,
                wear, time, health, peaceful);
    }

    public ItemCensus items() { return items; }
    public ItemCensus blocks() { return blocks; }
    public EntityCensus entities() { return entities; }
    public ItemCensus imported() { return imported; }
    public ItemCensus importedBlocks() { return importedBlocks; }
    public EntityCensus importedEntities() { return importedEntities; }
    public WearCensus wear() { return wear; }
    public long time() { return time; }
    public int health() { return health; }
    public boolean peaceful() { return peaceful; }
}
