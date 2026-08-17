package worldline.api;

/** One food item ID and the health it restores. */
public final class FoodHeal {
    private final int itemId, heal;

    public FoodHeal(int itemId, int heal) {
        if (itemId < 0 || heal <= 0) throw new IllegalArgumentException("food");
        this.itemId = itemId;
        this.heal = heal;
    }

    public int itemId() {
        return itemId;
    }

    public int heal() {
        return heal;
    }
}
