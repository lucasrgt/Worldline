package worldline.invariants;

import worldline.api.InvariantViolation;
import worldline.api.ItemCensus;

/**
 * Health may fall. A rise holds when peaceful regen heals at most one point,
 * consumed food covers the gain, or a present cake block covers one bite.
 */
public final class HealthConservation implements Invariant {
    public static final String NAME = "health-conservation";
    private final FoodBook foods;
    private Integer previousHealth;
    private ItemCensus previousItems;

    public HealthConservation() {
        this(FoodBook.none());
    }

    public HealthConservation(FoodBook foods) {
        if (foods == null) throw new NullPointerException("foods");
        this.foods = foods;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        int health = observation.health();
        ItemCensus items = observation.items();
        if (previousHealth == null) {
            previousHealth = Integer.valueOf(health);
            previousItems = items;
            return;
        }
        int rise = health - previousHealth.intValue();
        int allowed = foods.heal(previousItems.decrease(items)) + foods.presence(observation.blocks());
        if (observation.peaceful() && rise > 0) allowed += 1;
        if (rise > allowed) {
            throw new InvariantViolation(NAME, "health grew from " + previousHealth + " to " + health);
        }
        previousHealth = Integer.valueOf(health);
        previousItems = items;
    }
}
