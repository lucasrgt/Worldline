package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet25 painting identity, title, block pose and facing. */
public final class RemotePaintingSpawn {
    private static final String[] TITLES = {
        "Kebab", "Aztec", "Alban", "Aztec2", "Bomb", "Plant", "Wasteland",
        "Pool", "Courbet", "Sea", "Sunset", "Creebet", "Wanderer", "Graham",
        "Match", "Bust", "Stage", "Void", "SkullAndRoses", "Fighters",
        "Pointer", "Pigscene", "BurningSkull", "Skeleton", "DonkeyKong"
    };
    private final int entityId, x, y, z, direction;
    private final String title;

    public RemotePaintingSpawn(int entityId, String title, int x, int y, int z, int direction) {
        if (entityId < 0) throw new IllegalArgumentException("invalid painting entity id");
        if (!known(title)) throw new IllegalArgumentException("invalid painting title");
        if (y < 0 || y > 127) throw new IllegalArgumentException("invalid painting y");
        if (direction < 0 || direction > 3) throw new IllegalArgumentException("invalid painting direction");
        this.entityId = entityId; this.title = title; this.x = x; this.y = y; this.z = z;
        this.direction = direction;
    }

    public int entityId() { return entityId; }
    public String title() { return title; }
    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public int direction() { return direction; }
    public int packet() { return 25; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemotePaintingSpawn)) return false;
        RemotePaintingSpawn value = (RemotePaintingSpawn) other;
        return entityId == value.entityId && x == value.x && y == value.y && z == value.z
                && direction == value.direction && title.equals(value.title);
    }

    @Override public int hashCode() {
        return Objects.hash(entityId, title, x, y, z, direction);
    }

    private static boolean known(String title) {
        if (title == null) return false;
        for (int index = 0; index < TITLES.length; index++)
            if (TITLES[index].equals(title)) return true;
        return false;
    }
}
