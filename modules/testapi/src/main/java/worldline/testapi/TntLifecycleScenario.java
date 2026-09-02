package worldline.testapi;

/** Driver-neutral composition of official Packet23 and internal TNT-fuse observations. */
@FunctionalInterface
public interface TntLifecycleScenario {
    TntFuseLifecycleObservation observe();
}
