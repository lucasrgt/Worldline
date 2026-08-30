package worldline.testkit;

import java.util.Objects;

/** Open, detour, and sealed route observations from one deterministic matrix. */
public final class PathfindingMatrixObservation {
    private final PathfindingRouteObservation open, detour, sealed;

    public PathfindingMatrixObservation(PathfindingRouteObservation open,
            PathfindingRouteObservation detour, PathfindingRouteObservation sealed) {
        this.open = role(open, "open");
        this.detour = role(detour, "detour");
        this.sealed = role(sealed, "sealed");
    }

    public PathfindingRouteObservation open() { return open; }
    public PathfindingRouteObservation detour() { return detour; }
    public PathfindingRouteObservation sealed() { return sealed; }

    private static PathfindingRouteObservation role(PathfindingRouteObservation value,
            String expected) {
        Objects.requireNonNull(value, expected);
        if (!expected.equals(value.terrain())) {
            throw new IllegalArgumentException("pathfinding terrain order drifted");
        }
        return value;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof PathfindingMatrixObservation)) return false;
        PathfindingMatrixObservation value = (PathfindingMatrixObservation) other;
        return open.equals(value.open) && detour.equals(value.detour)
                && sealed.equals(value.sealed);
    }

    @Override public int hashCode() { return Objects.hash(open, detour, sealed); }
}
