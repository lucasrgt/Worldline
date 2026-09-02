package worldline.testapi;

/** Supplies one deterministic observation of the native rail network. */
@FunctionalInterface
public interface RailNetworkSubsystemScenario {
    RailNetworkSubsystemObservation observe();
}
