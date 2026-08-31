package worldline.testkit;

/** Driver-neutral execution of the qualified hostile behavior matrix. */
@FunctionalInterface
public interface HostileBehaviorScenario {
    HostileBehaviorObservation observe();
}
