package worldline.testkit;

/** Supplies one complete deterministic pathfinding terrain matrix. */
@FunctionalInterface
public interface PathfindingMatrixScenario {
    PathfindingMatrixObservation observe();
}
