package worldline.api;

/** Presence of {@code host} may create up to {@code max} entities of {@code entity}. */
public final class SpawnRule {
    private final String host, entity;
    private final int max;

    public SpawnRule(String host, String entity, int max) {
        if (host == null || host.isEmpty() || entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("spawn");
        }
        if (max <= 0) throw new IllegalArgumentException("spawn");
        this.host = host;
        this.entity = entity;
        this.max = max;
    }

    public String host() {
        return host;
    }

    public String entity() {
        return entity;
    }

    public int max() {
        return max;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof SpawnRule)) return false;
        SpawnRule rule = (SpawnRule) other;
        return max == rule.max && host.equals(rule.host) && entity.equals(rule.entity);
    }

    @Override public int hashCode() {
        return 31 * (31 * host.hashCode() + entity.hashCode()) + max;
    }
}
