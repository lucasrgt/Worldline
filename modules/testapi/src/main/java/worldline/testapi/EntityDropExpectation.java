package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;

/** One legacy item/drop range accepted by a bounded entity lifecycle. */
public final class EntityDropExpectation {
    private final int legacyId;
    private final int minimumCount;
    private final int maximumCount;
    private final int damage;

    public EntityDropExpectation(int legacyId, int minimumCount, int maximumCount, int damage) {
        if (legacyId < 0 || minimumCount < 1 || maximumCount < minimumCount
                || maximumCount > 64 || damage < 0) {
            throw new IllegalArgumentException("entity drop expectation");
        }
        this.legacyId = legacyId;
        this.minimumCount = minimumCount;
        this.maximumCount = maximumCount;
        this.damage = damage;
    }

    public static EntityDropExpectation exact(RemoteItemStack item) {
        if (item == null) throw new NullPointerException("item");
        return new EntityDropExpectation(item.legacyId(), item.count(), item.count(), item.damage());
    }

    public int legacyId() { return legacyId; }
    public int minimumCount() { return minimumCount; }
    public int maximumCount() { return maximumCount; }
    public int damage() { return damage; }

    public List<RemoteItemStack> candidates() {
        List<RemoteItemStack> values = new ArrayList<RemoteItemStack>();
        for (int count = minimumCount; count <= maximumCount; count++) {
            values.add(new RemoteItemStack(legacyId, count, damage));
        }
        return Collections.unmodifiableList(values);
    }

    public boolean matches(RemoteDroppedItem drop) {
        if (drop == null) return false;
        RemoteItemStack item = drop.item();
        return item.legacyId() == legacyId && item.damage() == damage
                && item.count() >= minimumCount && item.count() <= maximumCount;
    }
}
