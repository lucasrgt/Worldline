package worldline.testkit;

/** Supplies one deterministic observation of the four native redstone input controls. */
@FunctionalInterface
public interface RedstoneInputControlsSubsystemScenario {
    RedstoneInputControlsSubsystemObservation observe();
}
