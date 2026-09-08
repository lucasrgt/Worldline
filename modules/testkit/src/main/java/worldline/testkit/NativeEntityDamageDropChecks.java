package worldline.testkit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import worldline.testapi.NativeEntityDynamicsObservation;
import static worldline.testkit.NativeEntityStimulusFixture.equal;
import static worldline.testkit.NativeEntityStimulusFixture.require;

/** Rejects missing death controls, invented immunity and unrelated native item yields. */
final class NativeEntityDamageDropChecks {
    private NativeEntityDamageDropChecks() { }
    static void damage(NativeEntityDynamicsObservation v) {
        int id = v.scene().legacyId();
        require(supported(id) || id == 55, "unsupported damage subject");
        Set<String> keys = new HashSet<>();
        for (String p : new String[] {"control-", "low-", "lethal-"}) {
            for (String k : new String[] {"before", "after", "accepted", "immediate-dead", "dead"}) keys.add(p + k);
        }
        require(v.measurements().keySet().equals(keys), "damage measurements differ");
        long health = v.value("control-before");
        boolean measured = id == 1 || id >= 49;
        require(measured ? health > 0 : health == -1, "native health precondition differs");
        equal(v, "control-after", health); equal(v, "control-accepted", 0);
        equal(v, "control-immediate-dead", 0); equal(v, "control-dead", 0);
        for (String p : new String[] {"low-", "lethal-"}) {
            boolean lethal = p.equals("lethal-");
            equal(v, p + "before", health);
            equal(v, p + "accepted", id == 1 || immune(id) ? 0 : 1);
            equal(v, p + "after", measured ? health - (lethal ? 1024 : 1) : -1);
            long dead = immune(id) ? 0 : lethal || id == 9 || health == 1 ? 1 : 0;
            equal(v, p + "dead", dead);
            equal(v, p + "immediate-dead", id >= 49 ? 0 : dead);
        }
    }
    static void drops(NativeEntityDynamicsObservation v) {
        int id = v.scene().legacyId();
        require(supported(id), "unsupported drop subject");
        Set<String> expected = new HashSet<>();
        long matrixYield = 0;
        for (int seed = 0; seed < 8; seed++) for (String mode : new String[] {"control", "death"}) {
            String p = "seed" + seed + '-' + mode + '-';
            boolean death = mode.equals("death");
            for (String k : new String[] {"entities", "count", "other", "health", "dead"}) expected.add(p + k);
            long count = 0, stacks = 0;
            for (Map.Entry<String, Long> entry : v.measurements().entrySet()) {
                if (!entry.getKey().startsWith(p + "item-")) continue;
                expected.add(entry.getKey());
                String item = entry.getKey().substring((p + "item-").length());
                require(death && allowed(id, item) && entry.getValue() > 0 && entry.getValue() <= 8,
                        "unexpected native drop: " + entry.getKey());
                count += entry.getValue(); stacks++;
            }
            equal(v, p + "count", count); equal(v, p + "other", 0);
            require(v.value(p + "entities") >= stacks && v.value(p + "entities") <= count,
                    "drop entity accounting differs");
            if (id >= 49) require(death ? v.value(p + "health") <= 0 : v.value(p + "health") > 0,
                    "native death did not precede loot");
            else equal(v, p + "health", -1);
            equal(v, p + "dead", death && id < 49 && !immune(id) ? 1 : 0);
            if (!death) equal(v, p + "entities", 0);
            if (death && id == 9) equal(v, p + "item-321-0", 1);
            if (death && id == 40) equal(v, p + "item-328-0", 1);
            if (death && id == 41) { equal(v, p + "item-5-0", 3); equal(v, p + "item-280-0", 2); }
            matrixYield += count;
        }
        require(v.measurements().keySet().equals(expected), "drop matrix has unexpected measurements");
        boolean yields = id == 9 || id == 40 || id == 41 || id == 54 || id == 56 || id == 92 || id == 93;
        require(yields ? matrixYield > 0 : matrixYield == 0, "drop matrix has no expected causal yield");
    }
    private static boolean allowed(int id, String item) {
        switch (id) {
            case 9: return item.equals("321-0");
            case 40: return item.equals("328-0");
            case 41: return item.equals("5-0") || item.equals("280-0");
            case 54: case 93: return item.equals("288-0");
            case 56: return item.equals("289-0");
            case 92: return item.equals("334-0");
            default: return false;
        }
    }
    private static boolean immune(int id) { return id == 10 || id == 11 || id == 20 || id == 21; }
    private static boolean supported(int id) {
        return id == 1 || id == 9 || immune(id) || id == 40 || id == 41 || id == 49
                || id == 53 || id == 54 || id == 56 || id == 92 || id == 93 || id == 95;
    }
}
