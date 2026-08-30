package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable route observed for one declared terrain archetype. */
public final class PathfindingRouteObservation {
    private final String terrain;
    private final List<PathfindingNode> nodes;

    public PathfindingRouteObservation(String terrain, List<PathfindingNode> nodes) {
        if (!"open".equals(terrain) && !"detour".equals(terrain)
                && !"sealed".equals(terrain)) {
            throw new IllegalArgumentException("unknown pathfinding terrain " + terrain);
        }
        if (nodes == null || nodes.isEmpty() || nodes.size() > 64) {
            throw new IllegalArgumentException("invalid pathfinding route size");
        }
        ArrayList<PathfindingNode> copy = new ArrayList<PathfindingNode>();
        for (PathfindingNode node : nodes) copy.add(Objects.requireNonNull(node, "path node"));
        this.terrain = terrain;
        this.nodes = Collections.unmodifiableList(copy);
    }

    public String terrain() { return terrain; }
    public List<PathfindingNode> nodes() { return nodes; }
    public PathfindingNode endpoint() { return nodes.get(nodes.size() - 1); }

    int maximumZ() {
        int maximum = Integer.MIN_VALUE;
        for (PathfindingNode node : nodes) maximum = Math.max(maximum, node.zMilli());
        return maximum;
    }

    String canonical() {
        StringBuilder value = new StringBuilder(terrain).append("|nodes=").append(nodes.size());
        for (PathfindingNode node : nodes) value.append('|').append(node.token());
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof PathfindingRouteObservation
                && terrain.equals(((PathfindingRouteObservation) other).terrain)
                && nodes.equals(((PathfindingRouteObservation) other).nodes);
    }

    @Override public int hashCode() { return Objects.hash(terrain, nodes); }
}
