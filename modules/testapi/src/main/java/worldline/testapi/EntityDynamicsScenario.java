package worldline.testapi;

/** Driver-neutral executor for the eight-scene controlled entity-dynamics matrix. */
@FunctionalInterface
public interface EntityDynamicsScenario {
    EntityDynamicsObservation observe(EntityDynamicsScene scene);
}
