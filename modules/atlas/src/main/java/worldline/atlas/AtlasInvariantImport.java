package worldline.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.invariants.BlockConservation;
import worldline.invariants.DurabilityConservation;
import worldline.invariants.EntitySpawn;
import worldline.invariants.HealthConservation;
import worldline.invariants.ItemConservation;
import worldline.invariants.TimeMonotonic;

/** Indexes the six conservation rules. */
final class AtlasInvariantImport {
    private AtlasInvariantImport() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.add(invariant(ItemConservation.NAME));
        records.add(invariant(EntitySpawn.NAME));
        records.add(invariant(BlockConservation.NAME));
        records.add(invariant(HealthConservation.NAME));
        records.add(invariant(DurabilityConservation.NAME));
        records.add(invariant(TimeMonotonic.NAME));
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord invariant(String name) {
        List<String> evidence = Collections.unmodifiableList(Arrays.asList("invariant-engine", name));
        List<String> refs = new ArrayList<String>();
        String subsystem = AtlasSubsystems.forInvariant(name);
        if (!subsystem.isEmpty()) refs.add("atlas.subsystem." + subsystem);
        return AtlasRecord.of("atlas.invariant." + name, AtlasKind.INVARIANT, AtlasStatus.VERIFIED,
                AtlasSchema.WORLDLINE, AtlasSchema.SCOPE, name, "", 0, evidence, refs);
    }
}
