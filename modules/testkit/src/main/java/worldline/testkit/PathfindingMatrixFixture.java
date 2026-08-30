package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineHostileBehaviors;

/** Validates direct, obstacle-detour, and sealed-target pathfinder boundaries. */
public final class PathfindingMatrixFixture {
    private PathfindingMatrixFixture() { }

    public static PathfindingMatrixEvidence execute(PathfindingMatrixScenario scenario) {
        PathfindingMatrixObservation observed = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "pathfinding scenario").observe(),
                "pathfinding observation");
        require(observed.open().endpoint().xMilli() >= 12_000
                && observed.open().maximumZ() <= 9_000, "open route drifted");
        require(observed.detour().endpoint().xMilli() >= 12_000
                && observed.detour().maximumZ() >= 12_000,
                "wall detour omitted its gap");
        require(observed.sealed().endpoint().xMilli() <= 10_500,
                "sealed route entered the target ring");
        require(WorldlineBehavior.require("pathfinding-matrix")
                == WorldlineHostileBehaviors.PATHFINDING_MATRIX,
                "pathfinding behavior registration drifted");
        return new PathfindingMatrixEvidence(observed);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
