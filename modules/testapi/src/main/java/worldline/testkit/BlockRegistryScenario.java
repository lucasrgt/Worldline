package worldline.testkit;

import java.util.List;

/** Public observation boundary for a deterministic block registry capture. */
public interface BlockRegistryScenario {
    List<BlockRegistryObservation> observe();
}
