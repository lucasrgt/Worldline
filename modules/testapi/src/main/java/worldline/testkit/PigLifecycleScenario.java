package worldline.testkit;

/** Driver-neutral execution of the complete qualified pig lifecycle subsystem. */
public interface PigLifecycleScenario {
    EntityLifecycleScenario lifecycle();
    PigSaddleMountObservation observeSaddleAndMount();
}
