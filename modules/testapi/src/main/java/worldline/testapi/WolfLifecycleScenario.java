package worldline.testapi;

/** Driver-neutral execution of the qualified wolf owner-state mini-subsystem. */
@FunctionalInterface
public interface WolfLifecycleScenario {
    WolfOwnerStateObservation observe();
}
