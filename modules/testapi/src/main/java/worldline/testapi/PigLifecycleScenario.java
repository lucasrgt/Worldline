package worldline.testapi;

/** Driver-neutral execution of the complete qualified pig lifecycle subsystem. */
public interface PigLifecycleScenario {
    EntityLifecycleScenario lifecycle();
    PigSaddleMountObservation observeSaddleAndMount();
}
