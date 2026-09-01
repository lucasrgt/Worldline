package worldline.testkit;

/** Supplies one deterministic vegetation ecology observation. */
@FunctionalInterface
public interface VegetationEcologyScenario {
    VegetationEcologyObservation observe();
}
