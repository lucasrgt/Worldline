package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import worldline.api.RemoteItemStack;

/** Public exact-or-bounded contract for dropped item entities in one lifecycle. */
public final class BlockLifecycleDropMatrix {
    private static final Comparator<RemoteItemStack> STACK_ORDER = Comparator
            .comparingInt(RemoteItemStack::legacyId)
            .thenComparingInt(RemoteItemStack::damage)
            .thenComparingInt(RemoteItemStack::count);

    private final List<RemoteItemStack> exact;
    private final RemoteItemStack repeated;
    private final int minimumEntities, maximumEntities;

    private BlockLifecycleDropMatrix(List<RemoteItemStack> exact,
            RemoteItemStack repeated, int minimumEntities, int maximumEntities) {
        this.exact = exact;
        this.repeated = repeated;
        this.minimumEntities = minimumEntities;
        this.maximumEntities = maximumEntities;
    }

    public static BlockLifecycleDropMatrix exact(List<RemoteItemStack> drops) {
        if (drops == null || drops.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("invalid exact drop matrix");
        }
        return new BlockLifecycleDropMatrix(Collections.unmodifiableList(
                new ArrayList<RemoteItemStack>(drops)), null, drops.size(), drops.size());
    }

    public static BlockLifecycleDropMatrix repeated(RemoteItemStack item,
            int minimumEntities, int maximumEntities) {
        Objects.requireNonNull(item, "item");
        if (minimumEntities < 0 || maximumEntities < minimumEntities) {
            throw new IllegalArgumentException("invalid repeated drop bounds");
        }
        return new BlockLifecycleDropMatrix(null, item, minimumEntities, maximumEntities);
    }

    public boolean exact() { return exact != null; }

    public List<RemoteItemStack> exactDrops() {
        if (!exact()) throw new IllegalStateException("bounded drop matrix has no exact list");
        return exact;
    }

    public boolean accepts(List<RemoteItemStack> observed) {
        if (observed == null || observed.stream().anyMatch(Objects::isNull)) return false;
        if (exact()) return sorted(exact).equals(sorted(observed));
        if (observed.size() < minimumEntities || observed.size() > maximumEntities) return false;
        for (RemoteItemStack item : observed) if (!repeated.equals(item)) return false;
        return true;
    }

    public String canonical() {
        if (exact()) return items(exact);
        return items(Collections.singletonList(repeated)) + "*"
                + minimumEntities + ".." + maximumEntities;
    }

    public String description() {
        return exact() ? exact.toString() : "repeated=" + repeated + ",entities="
                + minimumEntities + ".." + maximumEntities;
    }

    private static List<RemoteItemStack> sorted(List<RemoteItemStack> values) {
        List<RemoteItemStack> result = new ArrayList<RemoteItemStack>(values);
        result.sort(STACK_ORDER);
        return result;
    }

    private static String items(List<RemoteItemStack> values) {
        StringBuilder result = new StringBuilder();
        for (RemoteItemStack item : values) {
            if (result.length() > 0) result.append(',');
            result.append(item.legacyId()).append(':').append(item.count())
                    .append(':').append(item.damage());
        }
        return result.toString();
    }
}
