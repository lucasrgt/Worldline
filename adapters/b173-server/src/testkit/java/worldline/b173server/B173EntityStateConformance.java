package worldline.b173server;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/** Reusable official-native damage, contextual drops and target selection captures. */
public final class B173EntityStateConformance {
    private B173EntityStateConformance() { }
    public static Map<String, Long> observe(Path mappings, boolean official) throws Exception {
        B173NativeServerSymbols n = new B173NativeServerSymbols(mappings, official);
        Map<String, Long> out = new TreeMap<>();
        try (B173NativePlayerFactory players = new B173NativePlayerFactory(n)) {
            B173EntityStateCauses.observe(n, out);
            B173EntityContextDrops.observe(n, players, out);
            B173EntityTargetContext.observe(n, players, out);
        }
        return out;
    }
}
