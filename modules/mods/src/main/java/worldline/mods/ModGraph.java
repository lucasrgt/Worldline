package worldline.mods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resolves a deterministic load order over inspected, compatible mod artifacts. */
public final class ModGraph {
    private ModGraph() {}

    /**
     * Orders artifacts so every dependency precedes its dependent. Ties break by
     * mod id. Rejects duplicate ids, unknown dependencies, unmet minimum
     * versions, self dependencies, and cycles.
     */
    public static List<ModArtifact> order(List<ModArtifact> artifacts) {
        if (artifacts == null) throw new NullPointerException("artifacts");
        Map<String, ModArtifact> byId = new HashMap<>();
        for (ModArtifact artifact : artifacts) {
            require(artifact != null && artifact.compatible(), "ordering requires compatible artifacts");
            require(byId.put(artifact.descriptor().id(), artifact) == null,
                    "duplicate mod id " + artifact.descriptor().id());
        }
        for (ModArtifact artifact : artifacts) {
            String id = artifact.descriptor().id();
            for (ModDependency dependency : artifact.descriptor().requires()) {
                require(!dependency.id().equals(id), "mod " + id + " depends on itself");
                ModArtifact required = byId.get(dependency.id());
                require(required != null, "mod " + id + " requires missing mod " + dependency.id());
                require(dependency.satisfiedBy(required.descriptor().version()),
                        "mod " + id + " requires " + dependency.id() + ">=" + dependency.minVersion()
                                + " but found " + required.descriptor().version());
            }
        }
        List<String> pending = new ArrayList<>(byId.keySet());
        List<ModArtifact> ordered = new ArrayList<>(artifacts.size());
        while (!pending.isEmpty()) {
            Collections.sort(pending);
            String ready = null;
            for (String candidate : pending) {
                if (satisfied(candidate, pending, byId)) { ready = candidate; break; }
            }
            require(ready != null, "dependency cycle among " + pending);
            pending.remove(ready);
            ordered.add(byId.get(ready));
        }
        return ordered;
    }

    private static boolean satisfied(String candidate, List<String> pending,
            Map<String, ModArtifact> byId) {
        for (ModDependency dependency : byId.get(candidate).descriptor().requires()) {
            if (pending.contains(dependency.id())) return false;
        }
        return true;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
