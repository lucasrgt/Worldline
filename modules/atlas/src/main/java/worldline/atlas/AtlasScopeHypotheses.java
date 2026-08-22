package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Surfaces that are not Beta 1.7.3, so Atlas must not treat them as gaps. */
final class AtlasScopeHypotheses {
    private AtlasScopeHypotheses() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.add(out("hunger"));
        records.add(out("villages"));
        records.add(out("the-end"));
        records.add(out("creative-mode"));
        records.add(out("xp-orbs"));
        records.add(out("animal-breeding"));
        records.add(out("bonemeal-growth"));
        records.add(item("shift-click-merge", AtlasStatus.UNKNOWN, "shift-click-and-merge",
                "OUT_OF_CONTRACT", "ROADMAP", "atlas.subsystem.inventory"));
        records.add(item("generic-sync-persistence", AtlasStatus.UNKNOWN, "generic-sync-and-persistence",
                "OUT_OF_CONTRACT", "ROADMAP", "atlas.subsystem.protocol"));
        records.add(item("multiple-clients-merge", AtlasStatus.UNKNOWN, "multiple-clients-and-merging",
                "OUT_OF_CONTRACT", "ROADMAP", "atlas.subsystem.protocol"));
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord out(String token) {
        return AtlasHypothesisImport.item(token, AtlasStatus.REJECTED, token, "OUT_OF_VERSION",
                "not-in-b1.7.3");
    }

    private static AtlasRecord item(String token, String status, String subject, String control,
            String evidence, String... refs) {
        return AtlasHypothesisImport.item(token, status, subject, control, evidence, refs);
    }
}
