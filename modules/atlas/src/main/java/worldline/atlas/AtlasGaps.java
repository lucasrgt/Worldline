package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Prioritized unknowns: empty coverage cells, unknown boundaries, experiments without CYCLE. */
public final class AtlasGaps {
    private AtlasGaps() {}

    public static List<AtlasRecord> list(AtlasStore store) {
        List<AtlasRecord> gaps = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : store.kind(AtlasKind.COVERAGE_UNIT)) {
            if (AtlasStatus.UNKNOWN.equals(record.status())) gaps.add(record);
        }
        for (AtlasRecord record : store.kind(AtlasKind.BOUNDARY)) {
            if (AtlasStatus.UNKNOWN.equals(record.status()) || record.control().isEmpty()) {
                gaps.add(record);
            }
        }
        for (AtlasRecord record : store.kind(AtlasKind.EXPERIMENT)) {
            if (!hasCycle(record) && !record.id().contains("symbols-map.")) gaps.add(record);
        }
        for (AtlasRecord record : store.kind(AtlasKind.HYPOTHESIS)) {
            if (AtlasStatus.UNKNOWN.equals(record.status())) gaps.add(record);
        }
        return Collections.unmodifiableList(gaps);
    }

    private static boolean hasCycle(AtlasRecord record) {
        for (String item : record.evidence()) {
            if (item.contains("_CYCLE.md")) return true;
        }
        return false;
    }
}
