package worldline.invariants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.EntityCensus;
import worldline.api.SpawnRule;

/**
 * Explains leftover entity gain from a present host. One observation may
 * create at most {@code max} of that type when the host count is positive.
 */
public final class SpawnBook {
    private final List<SpawnRule> rules;

    private SpawnBook(List<SpawnRule> rules) {
        this.rules = rules;
    }

    public static SpawnBook none() {
        return new SpawnBook(Collections.<SpawnRule>emptyList());
    }

    public static SpawnBook of(List<SpawnRule> rules) {
        if (rules == null) throw new NullPointerException("spawns");
        List<SpawnRule> copy = new ArrayList<SpawnRule>();
        for (SpawnRule rule : rules) {
            if (rule == null) throw new NullPointerException("spawn");
            copy.add(rule);
        }
        return new SpawnBook(Collections.unmodifiableList(copy));
    }

    public boolean explains(EntityCensus leftover, EntityCensus hosts) {
        if (leftover == null || hosts == null) throw new NullPointerException("census");
        for (String type : leftover.types()) {
            int extra = leftover.count(type);
            for (SpawnRule rule : rules) {
                if (rule.entity().equals(type) && hosts.count(rule.host()) > 0) extra -= rule.max();
            }
            if (extra > 0) return false;
        }
        return true;
    }

    public List<SpawnRule> rules() {
        return rules;
    }
}
