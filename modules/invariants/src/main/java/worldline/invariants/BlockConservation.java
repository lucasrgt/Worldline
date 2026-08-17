package worldline.invariants;

import worldline.api.EntityCensus;
import worldline.api.InvariantViolation;
import worldline.api.ItemCensus;

/**
 * Forbids unexplained block-ID creation. Transforms, fluid/fire presence, and
 * newly loaded chunks hold. Extra loss holds.
 */
public final class BlockConservation implements Invariant {
    public static final String NAME = "block-conservation";
    private final RecipeBook transforms;
    private final DropBook presence;
    private ItemCensus previous;
    private EntityCensus previousEntities;

    public BlockConservation() {
        this(RecipeBook.none(), DropBook.none());
    }

    public BlockConservation(RecipeBook transforms, DropBook presence) {
        if (transforms == null || presence == null) throw new NullPointerException("book");
        this.transforms = transforms;
        this.presence = presence;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        ItemCensus current = observation.blocks();
        if (previous == null) {
            previous = current;
            previousEntities = observation.entities();
            return;
        }
        ItemCensus lost = previous.decrease(current);
        ItemCensus gain = transforms.remainder(
                current.decrease(previous).decrease(observation.importedBlocks()), lost);
        EntityCensus spent = presenceOf(lost).plus(previousEntities.decrease(observation.entities()));
        if (gain.total() > 0 && !presence.explains(gain, spent, presenceOf(current))) {
            throw new InvariantViolation(NAME, "unexplained block creation");
        }
        previous = current;
        previousEntities = observation.entities();
    }

    private static EntityCensus presenceOf(ItemCensus blocks) {
        EntityCensus census = EntityCensus.empty();
        for (int id : blocks.itemIds()) {
            census = census.plus("block:" + id, blocks.count(id));
        }
        return census;
    }
}
