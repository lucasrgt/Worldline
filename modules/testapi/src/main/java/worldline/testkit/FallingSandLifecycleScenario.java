package worldline.testkit;

/** Driver-neutral execution of the qualified unsupported-sand lifecycle. */
@FunctionalInterface
public interface FallingSandLifecycleScenario {
    FallingSandLifecycleObservation observe();
}
