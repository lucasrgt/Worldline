package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.CauseDrop;
import worldline.api.ItemCensus;

/** Presence and hook causes: chicken eggs and caught fish. */
final class B173Causes {
    private B173Causes() {}

    static List<CauseDrop> snapshot() {
        List<CauseDrop> drops = new ArrayList<CauseDrop>();
        drops.add(CauseDrop.presence("minecraft:chicken", ItemCensus.of(344, 1)));
        drops.add(CauseDrop.presence("minecraft:sheep", ItemCensus.of(35, 4)));
        drops.add(CauseDrop.death("minecraft:fish-hook", ItemCensus.of(349, 1)));
        return Collections.unmodifiableList(drops);
    }

    static List<CauseDrop> withMobs() {
        List<CauseDrop> drops = new ArrayList<CauseDrop>(B173Mobs.snapshot());
        drops.addAll(snapshot());
        return drops;
    }
}
