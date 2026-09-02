package worldline.api.query;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.GameEntity;

/** Neutral query over living entities, independent of one-scenario action types. */
public final class EntityQuery {
    private final String type;
    private final List<GameEntity> matches;

    private EntityQuery(String type, List<GameEntity> matches) {
        this.type = type;
        this.matches = Collections.unmodifiableList(matches);
    }

    public static EntityQuery ofType(String type) {
        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("blank entity type");
        return new EntityQuery(type.trim(), Collections.<GameEntity>emptyList());
    }

    public static EntityQuery matching(String type, List<GameEntity> matches) {
        if (matches == null) throw new NullPointerException("matches");
        EntityQuery query = ofType(type);
        for (GameEntity entity : matches) {
            if (entity == null) throw new NullPointerException("entity");
            if (!type.equals(entity.type()))
                throw new IllegalArgumentException("entity type drifted");
        }
        return new EntityQuery(query.type, matches);
    }

    public String type() { return type; }
    public List<GameEntity> matches() { return matches; }
    public int size() { return matches.size(); }
    public boolean isEmpty() { return matches.isEmpty(); }

    @Override public boolean equals(Object other) {
        if (!(other instanceof EntityQuery)) return false;
        EntityQuery value = (EntityQuery) other;
        return type.equals(value.type) && matches.equals(value.matches);
    }

    @Override public int hashCode() { return Objects.hash(type, matches); }
}
