package worldline.testapi;

/** Supplies one deterministic built-environment material observation. */
@FunctionalInterface
public interface BuiltEnvironmentMaterialsScenario {
    BuiltEnvironmentMaterialsObservation observe();
}
