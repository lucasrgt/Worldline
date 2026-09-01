package worldline.testkit;

import java.util.List;

/** Supplies native entity NBT round-trip observations. */
@FunctionalInterface
public interface EntityPersistenceScenario {
    List<EntityPersistenceObservation> observe();
}
