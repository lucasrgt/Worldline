package worldline.invariants;

import java.util.Set;
import java.util.TreeSet;
import worldline.api.EntityCensus;
import worldline.api.InvariantViolation;
import worldline.api.ItemCensus;

/**
 * Forbids unexplained item creation between consecutive observations. Loss,
 * transfers, imports, recipes, and cause drops may explain a gain.
 */
public final class ItemConservation implements Invariant {
    public static final String NAME = "item-conservation";
    private final RecipeBook recipes;
    private final DropBook drops;
    private ItemCensus previousItems;
    private ItemCensus previousBlocks;
    private EntityCensus previousEntities;

    public ItemConservation() {
        this(RecipeBook.none(), DropBook.none());
    }

    public ItemConservation(RecipeBook recipes) {
        this(recipes, DropBook.none());
    }

    public ItemConservation(RecipeBook recipes, DropBook drops) {
        if (recipes == null || drops == null) throw new NullPointerException("book");
        this.recipes = recipes;
        this.drops = drops;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        ItemCensus currentItems = observation.items();
        ItemCensus currentBlocks = observation.blocks();
        EntityCensus currentEntities = observation.entities();
        if (previousItems == null) {
            previousItems = currentItems;
            previousBlocks = currentBlocks;
            previousEntities = currentEntities;
            return;
        }
        ItemCensus gain = currentItems.decrease(previousItems).decrease(observation.imported());
        ItemCensus loss = previousItems.decrease(currentItems)
                .plus(previousBlocks.decrease(currentBlocks));
        gain = recipes.remainder(gain, loss);
        if (gain.total() > 0
                && !drops.explains(gain, previousEntities.decrease(currentEntities), currentEntities)) {
            throw new InvariantViolation(NAME, gains(previousItems, currentItems));
        }
        previousItems = currentItems;
        previousBlocks = currentBlocks;
        previousEntities = currentEntities;
    }

    private static String gains(ItemCensus baseline, ItemCensus current) {
        StringBuilder text = new StringBuilder();
        Set<Integer> ids = new TreeSet<Integer>(current.itemIds());
        for (int itemId : ids) {
            int before = baseline.count(itemId);
            int after = current.count(itemId);
            if (after <= before) continue;
            if (text.length() > 0) text.append("; ");
            text.append("item ").append(itemId).append(" grew from ").append(before)
                    .append(" to ").append(after);
        }
        return text.toString();
    }
}
