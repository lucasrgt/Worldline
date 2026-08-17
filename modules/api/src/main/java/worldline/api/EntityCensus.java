package worldline.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Immutable living-entity type totals. Players and item entities are omitted. */
public final class EntityCensus {
    private final Map<String, Integer> counts;

    private EntityCensus(Map<String, Integer> counts) {
        this.counts = Collections.unmodifiableMap(new TreeMap<String, Integer>(counts));
    }

    public static EntityCensus empty() {
        return new EntityCensus(new TreeMap<String, Integer>());
    }

    public static EntityCensus of(String type, int count) {
        return empty().plus(type, count);
    }

    public static EntityCensus from(List<GameEntity> entities) {
        if (entities == null) throw new NullPointerException("entities");
        EntityCensus census = empty();
        for (GameEntity entity : entities) {
            if (entity == null) throw new NullPointerException("entity");
            String type = entity.type();
            if ("minecraft:player".equals(type) || "minecraft:item".equals(type)) continue;
            census = census.plus(type, 1);
        }
        return census;
    }

    public static EntityCensus inChunks(List<GameEntity> entities, Set<Long> chunks) {
        if (entities == null || chunks == null) throw new NullPointerException("entities");
        if (chunks.isEmpty()) return empty();
        EntityCensus census = empty();
        for (GameEntity entity : entities) {
            if (entity == null) throw new NullPointerException("entity");
            String type = entity.type();
            if ("minecraft:player".equals(type) || "minecraft:item".equals(type)) continue;
            GamePosition position = entity.position();
            int cx = ((int) Math.floor(position.x())) >> 4;
            int cz = ((int) Math.floor(position.z())) >> 4;
            long key = ((long) cx << 32) | ((long) cz & 0xffffffffL);
            if (chunks.contains(Long.valueOf(key))) census = census.plus(type, 1);
        }
        return census;
    }

    public EntityCensus plus(String type, int count) {
        if (type == null || type.isEmpty()) throw new IllegalArgumentException("entity type");
        if (count < 0) throw new IllegalArgumentException("entity count");
        if (count == 0) return this;
        Map<String, Integer> next = new TreeMap<String, Integer>(counts);
        long total = (long) countOf(next, type) + count;
        if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("entity count overflow");
        next.put(type, (int) total);
        return new EntityCensus(next);
    }

    public EntityCensus plus(EntityCensus other) {
        if (other == null) throw new NullPointerException("census");
        EntityCensus result = this;
        for (String type : other.types()) result = result.plus(type, other.count(type));
        return result;
    }

    public int count(String type) {
        if (type == null || type.isEmpty()) throw new IllegalArgumentException("entity type");
        return countOf(counts, type);
    }

    public Set<String> types() {
        return counts.keySet();
    }

    public EntityCensus decrease(EntityCensus later) {
        if (later == null) throw new NullPointerException("census");
        EntityCensus result = empty();
        for (String type : types()) {
            int drop = count(type) - later.count(type);
            if (drop > 0) result = result.plus(type, drop);
        }
        return result;
    }

    @Override public boolean equals(Object other) {
        return other instanceof EntityCensus && counts.equals(((EntityCensus) other).counts);
    }

    @Override public int hashCode() {
        return counts.hashCode();
    }

    private static int countOf(Map<String, Integer> counts, String type) {
        Integer value = counts.get(type);
        return value == null ? 0 : value.intValue();
    }
}
