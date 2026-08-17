package worldline.invariants;

import worldline.api.EntityCensus;
import worldline.api.InvariantViolation;
import worldline.api.ItemCensus;

/**
 * Forbids unexplained living-entity creation. Chunk imports, thrown eggs,
 * one baby per two remaining parents, and host spawn rules hold. Extra loss
 * holds.
 */
public final class EntitySpawn implements Invariant {
    public static final String NAME = "entity-spawn";
    private static final int EGG = 344;
    private final SpawnBook spawns;
    private EntityCensus previous;
    private ItemCensus previousItems, previousBlocks;

    public EntitySpawn() {
        this(SpawnBook.none());
    }

    public EntitySpawn(SpawnBook spawns) {
        if (spawns == null) throw new NullPointerException("spawns");
        this.spawns = spawns;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        EntityCensus current = observation.entities();
        ItemCensus items = observation.items();
        if (previous == null) {
            previous = current;
            previousItems = items;
            previousBlocks = observation.blocks();
            return;
        }
        EntityCensus gain = current.decrease(previous).decrease(observation.importedEntities());
        int hatched = Math.min(previousItems.decrease(items).count(EGG), gain.count("minecraft:chicken"));
        EntityCensus leftover = EntityCensus.empty();
        for (String type : gain.types()) {
            int extra = gain.count(type);
            if ("minecraft:chicken".equals(type)) extra -= hatched;
            extra -= Math.max(0, (current.count(type) - gain.count(type)) / 2);
            if (extra > 0) leftover = leftover.plus(type, extra);
        }
        if (!spawns.explains(leftover, hosts(observation, items))) {
            throw new InvariantViolation(NAME, "unexplained entity spawn");
        }
        previous = current;
        previousItems = items;
        previousBlocks = observation.blocks();
    }

    private EntityCensus hosts(InvariantObservation observation, ItemCensus items) {
        EntityCensus hosts = previous;
        hosts = add(hosts, "block:", previousBlocks);
        hosts = add(hosts, "block:", observation.blocks());
        hosts = add(hosts, "item:", previousItems.decrease(items));
        return hosts;
    }

    private static EntityCensus add(EntityCensus hosts, String prefix, ItemCensus census) {
        for (int id : census.itemIds()) hosts = hosts.plus(prefix + id, census.count(id));
        return hosts;
    }
}
