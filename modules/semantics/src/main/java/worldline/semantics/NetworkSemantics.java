package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Network-category mappings for the b1.7.3 semantic catalog. Roles cover the
 * offline session type and the disabled network-connected probe.
 */
final class NetworkSemantics {
    private NetworkSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("network", "OFFLINE_SESSION",
                "net/minecraft/src/Session", "class", "Session", "-",
                "", "", "NETWORK", "controlled-client-tick,lab-cycle,gui-tree", "gr", 9990));
        mappings.add(SemanticMapping.of("network", "NETWORK_DISABLED",
                "worldline/b173/B173Runtime", "method", "networkConnected", "()Z",
                "", "", "NETWORK", "lab-cycle", "", 9990));
        return Collections.unmodifiableList(mappings);
    }
}
