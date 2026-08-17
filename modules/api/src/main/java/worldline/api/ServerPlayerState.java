package worldline.api;

import java.util.Objects;

/** Immutable persisted observation of one multiplayer player. */
public final class ServerPlayerState {
    private final String username;
    private final int dimension, health, inventoryItems;
    private final double x, y, z;

    public ServerPlayerState(String username, int dimension, double x, double y, double z,
            int health, int inventoryItems) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid player username");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("non-finite player position");
        if (health < 0 || inventoryItems < 0) throw new IllegalArgumentException("negative player field");
        this.username = username; this.dimension = dimension;
        this.x = x; this.y = y; this.z = z;
        this.health = health; this.inventoryItems = inventoryItems;
    }

    public String username() { return username; }
    public int dimension() { return dimension; }
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public int health() { return health; }
    public int inventoryItems() { return inventoryItems; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ServerPlayerState)) return false;
        ServerPlayerState state = (ServerPlayerState) other;
        return username.equals(state.username) && dimension == state.dimension
                && Double.compare(x, state.x) == 0 && Double.compare(y, state.y) == 0
                && Double.compare(z, state.z) == 0 && health == state.health
                && inventoryItems == state.inventoryItems;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, dimension, x, y, z, health, inventoryItems);
    }
}
