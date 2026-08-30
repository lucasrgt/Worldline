package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the public pathfinding matrix contract. */
public final class PathfindingMatrixEvidence {
    private final PathfindingMatrixObservation observation;

    PathfindingMatrixEvidence(PathfindingMatrixObservation observation) {
        this.observation = Objects.requireNonNull(observation, "pathfinding observation");
    }

    public PathfindingMatrixObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.pathfinding-matrix-evidence.v1\n"
                + "entity=pig|start=4500:65000:8500|target=12000:65000:8000|range=32\n"
                + observation.open().canonical() + '\n'
                + observation.detour().canonical() + '\n'
                + observation.sealed().canonical() + '\n';
    }

    @Override public boolean equals(Object other) {
        return other instanceof PathfindingMatrixEvidence
                && observation.equals(((PathfindingMatrixEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
