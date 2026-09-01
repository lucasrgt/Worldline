package worldline.testkit;

/** Driver-neutral execution of the complete qualified chicken-and-egg family scene. */
@FunctionalInterface
public interface ChickenEggFamilyScenario {
    ChickenEggFamilyObservation observe();
}
