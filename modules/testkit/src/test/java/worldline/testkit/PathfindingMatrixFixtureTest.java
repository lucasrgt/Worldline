package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.WorldlineBehavior;

/** Contract checks for the public pathfinding terrain matrix. */
final class PathfindingMatrixFixtureTest {
    private PathfindingMatrixFixtureTest() { }

    static void execute() {
        PathfindingMatrixEvidence first = PathfindingMatrixFixture.execute(
                PathfindingMatrixFixtureTest::observation);
        PathfindingMatrixEvidence second = PathfindingMatrixFixture.execute(
                PathfindingMatrixFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "pathfinding evidence is unstable");
        require(first.canonical().startsWith(
                        "schema=worldline.pathfinding-matrix-evidence.v1\n"),
                "pathfinding evidence schema drifted");
        require(first.canonical().contains("open|nodes=2|"),
                "open path evidence missing");
        require(WorldlineBehavior.require("pathfinding-matrix")
                        == WorldlineBehavior.PATHFINDING_MATRIX,
                "pathfinding behavior registration drifted");

        rejects(() -> PathfindingMatrixFixture.execute(() -> matrix(
                route("open", node(5_000, 65_000, 8_500), node(12_000, 65_000, 9_001)),
                validDetour(), validSealed())));
        rejects(() -> PathfindingMatrixFixture.execute(() -> matrix(
                validOpen(), route("detour", node(5_000, 65_000, 8_500),
                        node(12_000, 65_000, 11_999)), validSealed())));
        rejects(() -> PathfindingMatrixFixture.execute(() -> matrix(
                validOpen(), validDetour(),
                route("sealed", node(5_000, 65_000, 8_500),
                        node(10_501, 65_000, 8_000)))));
        rejects(() -> PathfindingMatrixFixture.execute(() -> null));
        rejects(() -> new PathfindingMatrixObservation(validDetour(), validOpen(), validSealed()));
        rejects(() -> new PathfindingRouteObservation("unknown",
                Collections.singletonList(node(0, 0, 0))));
    }

    private static PathfindingMatrixObservation observation() {
        return matrix(validOpen(), validDetour(), validSealed());
    }

    private static PathfindingMatrixObservation matrix(PathfindingRouteObservation open,
            PathfindingRouteObservation detour, PathfindingRouteObservation sealed) {
        return new PathfindingMatrixObservation(open, detour, sealed);
    }

    private static PathfindingRouteObservation validOpen() {
        return route("open", node(5_000, 65_000, 8_500), node(12_000, 65_000, 8_000));
    }

    private static PathfindingRouteObservation validDetour() {
        return route("detour", node(5_000, 65_000, 8_500),
                node(8_000, 65_000, 12_000), node(12_000, 65_000, 8_000));
    }

    private static PathfindingRouteObservation validSealed() {
        return route("sealed", node(5_000, 65_000, 8_500), node(10_000, 65_000, 8_000));
    }

    private static PathfindingRouteObservation route(String terrain, PathfindingNode... nodes) {
        return new PathfindingRouteObservation(terrain, Arrays.asList(nodes));
    }

    private static PathfindingNode node(int x, int y, int z) {
        return new PathfindingNode(x, y, z);
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid pathfinding evidence was accepted");
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException expected) {
            // Expected.
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
