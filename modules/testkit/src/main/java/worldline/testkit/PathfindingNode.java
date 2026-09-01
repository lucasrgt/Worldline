package worldline.testkit;

import java.util.Objects;

/** One deterministic path node expressed in milliblocks. */
public final class PathfindingNode {
    private final int x, y, z;

    public PathfindingNode(int xMilli, int yMilli, int zMilli) {
        this.x = xMilli; this.y = yMilli; this.z = zMilli;
    }

    public int xMilli() { return x; }
    public int yMilli() { return y; }
    public int zMilli() { return z; }

    String token() { return x + ":" + y + ":" + z; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof PathfindingNode)) return false;
        PathfindingNode value = (PathfindingNode) other;
        return x == value.x && y == value.y && z == value.z;
    }

    @Override public int hashCode() { return Objects.hash(x, y, z); }
}
