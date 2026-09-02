package worldline.testapi;

import worldline.api.RemotePaintingSpawn;

/** Expected Packet25 anchor and facing; fresh entity identity and motive remain normalized. */
public final class PaintingSpawnExpectation {
    private final int x;
    private final int y;
    private final int z;
    private final int direction;

    public PaintingSpawnExpectation(int x, int y, int z, int direction) {
        if (y < 0 || y > 127 || direction < 0 || direction > 3) {
            throw new IllegalArgumentException("painting spawn expectation");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public int direction() { return direction; }

    public boolean matches(RemotePaintingSpawn spawn) {
        return spawn != null && spawn.entityId() > 0 && spawn.packet() == 25
                && spawn.x() == x && spawn.y() == y && spawn.z() == z
                && spawn.direction() == direction;
    }

    String canonical() {
        return x + ":" + y + ":" + z + ":dir" + direction;
    }
}
