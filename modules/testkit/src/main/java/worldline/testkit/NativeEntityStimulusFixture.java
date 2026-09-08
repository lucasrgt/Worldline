package worldline.testkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Public paired native spawn, damage, drop and contact acceptance. */
public final class NativeEntityStimulusFixture {
    private NativeEntityStimulusFixture() { }
    public static NativeEntityStimulusEvidence verify(List<NativeEntityDynamicsCase> expected,
            List<NativeEntityDynamicsObservation> observed) {
        require(expected != null && !expected.isEmpty() && observed != null
                && expected.size() == observed.size(), "stimulus family is incomplete");
        Set<String> claims = new HashSet<>();
        for (int i = 0; i < expected.size(); i++) {
            NativeEntityDynamicsCase c = expected.get(i);
            NativeEntityDynamicsObservation v = observed.get(i);
            require(c != null && v != null && claims.add(c.claimId())
                    && c.canonical().equals(v.scene().canonical()), "stimulus route differs");
            switch (c.rule()) {
                case SPAWN: spawn(v); break;
                case DAMAGE: NativeEntityDamageDropChecks.damage(v); break;
                case DROPS: NativeEntityDamageDropChecks.drops(v); break;
                case INTERACTION: NativeEntityInteractionChecks.verify(v); break;
                default: throw new IllegalArgumentException("unsupported stimulus rule: " + c.rule());
            }
        }
        return new NativeEntityStimulusEvidence(observed);
    }
    private static void spawn(NativeEntityDynamicsObservation v) {
        int id = v.scene().legacyId();
        require(id == 1 || id == 49 || id == 53 || id == 56 || id == 92, "unsupported spawn subject");
        require(v.scene().layer().equals("UNIVERSAL"), "spawn route must be universal");
        if (id == 1) {
            keys(v, "before-member", "after-member", "joined", "packet", "identity", "coordinates",
                    "roundtrip", "item-id", "count", "item-damage");
            equal(v, "packet", 21); equal(v, "item-id", 1); equal(v, "count", 2); equal(v, "item-damage", 0);
        } else {
            keys(v, "before-member", "after-member", "joined", "packet", "identity", "coordinates",
                    "roundtrip", "type", "yaw", "pitch");
            equal(v, "packet", 24); equal(v, "type", id); equal(v, "yaw", 32); equal(v, "pitch", 0);
        }
        equal(v, "before-member", 0); equal(v, "after-member", 1); equal(v, "joined", 1);
        equal(v, "identity", 1); equal(v, "coordinates", 1); equal(v, "roundtrip", 1);
    }
    static void keys(NativeEntityDynamicsObservation v, String... keys) {
        require(v.measurements().keySet().equals(new HashSet<>(Arrays.asList(keys))),
                "native stimulus measurements differ from the contract");
    }
    static void equal(NativeEntityDynamicsObservation v, String key, long expected) {
        require(v.value(key) == expected, v.scene().canonical() + ':' + key + " differs: " + v.value(key));
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
