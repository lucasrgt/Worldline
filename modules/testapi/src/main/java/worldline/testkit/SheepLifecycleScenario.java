package worldline.testkit;

/** Driver-neutral lethal, interaction, and persistence scenes for the complete sheep subsystem. */
public interface SheepLifecycleScenario {
    EntityLifecycleScenario lethal();
    SheepDyeShearObservation observeDyeAndShear();
    SheepPersistenceObservation observePersistence();
}
