package worldline.api;

import java.util.Collections;
import java.util.List;

/**
 * Controlled lifecycle that can watch player item totals after every tick.
 * Watching is opt-in and experimental; it is not part of the M3 inventory
 * non-claim.
 */
public interface InvariantMinecraftRuntime extends AutomatedMinecraftRuntime {
    void watch(ItemCensusObserver observer);

    /** Known crafting and smelting transforms. Empty when the backend has none. */
    default List<ItemRecipe> recipes() {
        return Collections.emptyList();
    }

    /** Known death and presence drops. Empty when the backend has none. */
    default List<CauseDrop> drops() {
        return Collections.emptyList();
    }

    /** Block-ID swaps such as dirt to grass. Empty when the backend has none. */
    default List<ItemRecipe> transforms() {
        return Collections.emptyList();
    }

    /** Fluid and fire presence causes. Empty when the backend has none. */
    default List<CauseDrop> fluids() {
        return Collections.emptyList();
    }

    /** Food item IDs and heal amounts. Empty when the backend has none. */
    default List<FoodHeal> foods() {
        return Collections.emptyList();
    }

    /** Host-to-entity spawn rules. Empty when the backend has none. */
    default List<SpawnRule> spawns() {
        return Collections.emptyList();
    }
}
