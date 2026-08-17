package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Clock-category mappings for the b1.7.3 semantic catalog. Roles cover the
 * client millisecond source, the virtual accumulator, world time, and the
 * client system-time field.
 */
final class ClockSemantics {
    private ClockSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("clock", "CLIENT_CLOCK_SOURCE",
                "worldline/b173/B173ClockHooks", "method", "currentTimeMillis", "()J",
                "", "", "CLOCK", "lab-cycle,controlled-client-tick", "", 9998));
        mappings.add(SemanticMapping.of("clock", "CLIENT_CLOCK_ACCUMULATOR",
                "worldline/b173/B173VirtualClock", "class", "B173VirtualClock", "-",
                "CLOCK", "CLOCK", "CLOCK", "lab-cycle", "", 9990));
        mappings.add(SemanticMapping.of("clock", "WORLD_TIME",
                "net/minecraft/src/World", "method", "getWorldTime", "()J",
                "WORLD", "", "CLOCK", "controlled-client-tick,symbols.map,m3-domain-api",
                "t", 9998));
        mappings.add(SemanticMapping.of("clock", "CLIENT_SYSTEM_TIME",
                "net/minecraft/client/Minecraft", "field", "systemTime", "J",
                "CLOCK", "CLOCK", "CLOCK", "lab-cycle,controlled-client-tick", "", 9920));
        return Collections.unmodifiableList(mappings);
    }
}
