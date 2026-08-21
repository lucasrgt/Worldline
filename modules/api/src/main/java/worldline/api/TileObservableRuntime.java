package worldline.api;

import java.util.List;
import java.util.Set;

/** Optional controlled capability for tile observations in selected chunks. */
public interface TileObservableRuntime extends AutomatedMinecraftRuntime {
    List<TileObservation> tilesInChunks(Set<Long> chunks);
}
