package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-version vanilla clusters with no official GO. These are Atlas unknowns. */
final class AtlasClusterHypotheses {
    private AtlasClusterHypotheses() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.add(gap("worldgen", "worldgen"));
        records.add(gap("biomes", "worldgen"));
        records.add(gap("caves-heightmap", "worldgen"));
        records.add(gap("lighting-propagation", "lighting"));
        records.add(gap("weather", "weather"));
        records.add(gap("block-ticks", "block-ticks"));
        records.add(gap("fluids", "fluids"));
        records.add(gap("fire-spread", "fluids"));
        records.add(gap("explosions", "fluids"));
        records.add(gap("mob-spawn", "entities"));
        records.add(gap("paintings", "entities"));
        records.add(gap("boats-minecarts", "entities"));
        records.add(gap("projectiles", "entities"));
        records.add(gap("falling-sand-tnt", "entities"));
        records.add(gap("passive-mobs", "entities"));
        records.add(gap("mob-ai", "mob-ai"));
        records.add(gap("double-chest", "inventory"));
        records.add(gap("dispenser", "inventory"));
        records.add(gap("redstone", "redstone"));
        records.add(gap("pistons", "redstone"));
        records.add(gap("doors-levers", "redstone"));
        records.add(gap("rails", "entities"));
        records.add(gap("nether-dimension", "dimensions"));
        records.add(gap("beds-sleep", "dimensions"));
        records.add(gap("player-env-damage", "player"));
        records.add(gap("handshake-packets", "protocol"));
        records.add(gap("aero-unpromoted-intercepts", "aero"));
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord gap(String token, String subsystem) {
        return AtlasHypothesisImport.item(token, AtlasStatus.UNKNOWN, token, "CLUSTER_GAP",
                "vanilla-cluster-inventory", "atlas.subsystem." + subsystem);
    }
}
