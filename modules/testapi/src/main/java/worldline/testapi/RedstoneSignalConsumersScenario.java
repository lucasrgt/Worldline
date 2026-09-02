package worldline.testapi;

/** Supplies one deterministic redstone signal-consumer observation. */
@FunctionalInterface
public interface RedstoneSignalConsumersScenario {
    RedstoneSignalConsumersObservation observe();
}
