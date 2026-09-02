package worldline.testapi;

import java.util.List;

/** Public observation boundary for a deterministic EntityList capture. */
public interface EntityRegistryScenario {
    List<EntityRegistryObservation> observe();
}
