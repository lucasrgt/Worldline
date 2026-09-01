package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds a bounded agent context from ranked matches and their record graph. */
public final class AtlasContext {
    private AtlasContext() {}

    public static List<AtlasHit> build(AtlasStore store, String query, int budget, int depth) {
        if (budget < 1 || budget > 1000) throw new IllegalArgumentException("budget");
        if (depth < 0 || depth > 3) throw new IllegalArgumentException("depth");
        int directBudget = depth == 0 ? budget : Math.max(1, budget / 2);
        List<AtlasHit> direct = AtlasIndex.search(store, query, Math.min(directBudget, 100));
        Map<String, AtlasHit> selected = new LinkedHashMap<String, AtlasHit>();
        for (AtlasHit hit : direct) add(selected, hit, budget);
        List<AtlasHit> frontier = new ArrayList<AtlasHit>(direct);
        for (int level = 1; level <= depth && selected.size() < budget; level++) {
            List<AtlasHit> next = new ArrayList<AtlasHit>();
            for (AtlasHit source : frontier) {
                for (AtlasRecord related : related(store, source.record())) {
                    AtlasHit hit = new AtlasHit(related,
                            Math.max(1, source.score() - level * 25), "GRAPH_DEPTH_" + level);
                    if (!selected.containsKey(related.id())) next.add(hit);
                    add(selected, hit, budget);
                }
            }
            frontier = next;
        }
        return Collections.unmodifiableList(new ArrayList<AtlasHit>(selected.values()));
    }

    private static List<AtlasRecord> related(AtlasStore store, AtlasRecord record) {
        List<AtlasRecord> result = new ArrayList<AtlasRecord>();
        for (String ref : record.refs()) result.add(store.get(ref));
        for (AtlasRecord candidate : store.records()) {
            if (candidate.refs().contains(record.id())) result.add(candidate);
        }
        Collections.sort(result, new java.util.Comparator<AtlasRecord>() {
            @Override public int compare(AtlasRecord left, AtlasRecord right) {
                return left.id().compareTo(right.id());
            }
        });
        return result;
    }

    private static void add(Map<String, AtlasHit> selected, AtlasHit hit, int budget) {
        if (selected.size() < budget && !selected.containsKey(hit.record().id())) {
            selected.put(hit.record().id(), hit);
        }
    }
}
