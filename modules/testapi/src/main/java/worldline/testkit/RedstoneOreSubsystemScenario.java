package worldline.testkit;

/** Supplies one complete Beta 1.7.3 redstone-ore subsystem observation. */
@FunctionalInterface
public interface RedstoneOreSubsystemScenario {
    RedstoneOreSubsystemObservation observe();
}
