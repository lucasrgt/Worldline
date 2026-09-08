package worldline.testkit;

import java.util.HashSet;
import java.util.Set;
import worldline.testapi.NativeEntityDynamicsObservation;
import static worldline.testkit.NativeEntityStimulusFixture.equal;
import static worldline.testkit.NativeEntityStimulusFixture.require;

/** Entity-specific causal contacts share one public family without erasing distinct native results. */
final class NativeEntityInteractionChecks {
    private NativeEntityInteractionChecks() { }
    static void verify(NativeEntityDynamicsObservation v) {
        int id = v.scene().legacyId();
        if (id == 1 || id == 10 || id == 92) inventory(v, id);
        else if (id == 40 || id == 41) mount(v);
        else if (id == 20 || id == 21) unsupported(v);
        else if (id == 11) snowball(v);
        else if (id == 93 || id == 94) body(v);
        else combat(v, id);
    }
    private static void inventory(NativeEntityDynamicsObservation v, int id) {
        if (id == 92) pairedKeys(v, "accepted", "inventory-id", "inventory-count", "dead");
        else pairedKeys(v, "inventory-id", "inventory-count", "dead");
        equal(v, "control-dead", 0); equal(v, "active-dead", id == 92 ? 0 : 1);
        equal(v, "control-inventory-id", id == 92 ? 1 : 0);
        equal(v, "control-inventory-count", id == 92 ? 1 : 0);
        equal(v, "active-inventory-id", id == 1 ? 1 : id == 10 ? 262 : 335);
        equal(v, "active-inventory-count", id == 1 ? 2 : 1);
        if (id == 92) { equal(v, "control-accepted", 0); equal(v, "active-accepted", 1); }
    }
    private static void mount(NativeEntityDynamicsObservation v) {
        pairedKeys(v, "accepted", "mounted", "rider", "dismounted", "dead");
        equal(v, "control-accepted", 0); equal(v, "active-accepted", 1);
        equal(v, "control-mounted", 0); equal(v, "active-mounted", 1);
        equal(v, "control-rider", 0); equal(v, "active-rider", 1);
        equal(v, "control-dismounted", 1); equal(v, "active-dismounted", 1);
        equal(v, "control-dead", 0); equal(v, "active-dead", 0);
    }
    private static void unsupported(NativeEntityDynamicsObservation v) {
        pairedKeys(v, "accepted", "position", "player-health", "dead");
        for (String p : new String[] {"control-", "active-"}) {
            equal(v, p + "accepted", 0); equal(v, p + "position", 8500);
            equal(v, p + "player-health", 20); equal(v, p + "dead", 0);
        }
    }
    private static void snowball(NativeEntityDynamicsObservation v) {
        pairedKeys(v, "health-before", "health-after", "dead");
        equal(v, "control-dead", 0); equal(v, "active-dead", 1);
        for (String p : new String[] {"control-", "active-"}) {
            require(v.value(p + "health-before") > 0, "snowball target is not alive");
            equal(v, p + "health-after", v.value(p + "health-before"));
        }
    }
    private static void body(NativeEntityDynamicsObservation v) {
        pairedKeys(v, "pushable", "motion", "target-motion");
        equal(v, "control-pushable", 1); equal(v, "active-pushable", 1);
        equal(v, "control-motion", 0); equal(v, "control-target-motion", 0);
        require(v.value("active-motion") < 0 && v.value("active-target-motion") > 0,
                "native body contact produced no opposing motion");
        equal(v, "active-motion", -v.value("active-target-motion"));
    }
    private static void combat(NativeEntityDynamicsObservation v, int id) {
        require(id == 49 || id == 50 || id == 52 || id == 53 || id == 54 || id == 56 || id == 57,
                "unsupported combat subject");
        if (id == 50) pairedKeys(v, "health-before", "health-after", "state");
        else if (id == 56) pairedKeys(v, "health-before", "health-after", "counter", "fireballs");
        else pairedKeys(v, "health-before", "health-after", "cooldown");
        equal(v, "control-health-before", 20); equal(v, "active-health-before", 20);
        equal(v, "control-health-after", 20);
        if (id == 50) {
            equal(v, "control-state", -1); equal(v, "active-state", 1); equal(v, "active-health-after", 20);
        } else if (id == 56) {
            equal(v, "control-counter", 19); equal(v, "active-counter", -40);
            equal(v, "control-fireballs", 0); equal(v, "active-fireballs", 1); equal(v, "active-health-after", 20);
        } else {
            require(v.value("active-health-after") < 20, "native melee did not damage the target");
            equal(v, "control-cooldown", 2); equal(v, "active-cooldown", 20);
        }
    }
    private static void pairedKeys(NativeEntityDynamicsObservation v, String... keys) {
        Set<String> expected = new HashSet<>();
        for (String p : new String[] {"control-", "active-"}) for (String key : keys) expected.add(p + key);
        require(v.measurements().keySet().equals(expected), "native interaction measurements differ");
    }
}
