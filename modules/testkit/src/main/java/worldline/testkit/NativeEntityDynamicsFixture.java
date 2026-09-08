package worldline.testkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Requires causal native contrasts instead of accepting manually assigned outcomes. */
public final class NativeEntityDynamicsFixture {
    private NativeEntityDynamicsFixture() { }
    public static NativeEntityDynamicsEvidence verify(List<NativeEntityDynamicsCase> expected,
            List<NativeEntityDynamicsObservation> observed) {
        require(expected != null && !expected.isEmpty() && observed != null
                && expected.size() == observed.size(), "dynamics family is incomplete");
        Set<String> claims = new HashSet<>();
        for (int index = 0; index < expected.size(); index++) {
            NativeEntityDynamicsCase scene = expected.get(index);
            NativeEntityDynamicsObservation value = observed.get(index);
            require(scene != null && value != null && claims.add(scene.claimId())
                    && scene.canonical().equals(value.scene().canonical()), "dynamics route differs");
            verify(value);
        }
        return new NativeEntityDynamicsEvidence(observed);
    }
    private static void verify(NativeEntityDynamicsObservation v) {
        switch (v.scene().rule()) {
            case MOTION:
                keys(v, "open-delta", "blocked-delta", "collision", "blocked-motion");
                require(v.value("open-delta") == 5000 && v.value("blocked-delta") > 0
                        && v.value("blocked-delta") < 5000 && v.value("collision") == 1
                        && v.value("blocked-motion") == 0, "native displacement was not constrained");
                break;
            case HANGING_MOTION:
                keys(v, "moved-dead", "control-dead", "moved-delta", "control-delta", "moved-drops", "control-drops");
                require(v.value("moved-dead") == 1 && v.value("control-dead") == 0
                        && v.value("moved-delta") == 0 && v.value("control-delta") == 0
                        && v.value("moved-drops") == 1 && v.value("control-drops") == 0,
                        "native hanging entity did not break only on nonzero movement");
                break;
            case IMMERSION:
            case SHORT_IMMERSION:
                keys(v, "dry-before", "dry-after", "wet-before", "wet-after", "dry-inside", "wet-inside");
                require(v.value("dry-before") == 0 && v.value("dry-after") == 0
                        && v.value("wet-before") == 0 && v.value("dry-inside") == 0 && v.value("wet-inside") == 1
                        && v.value("wet-after") == (v.scene().rule() == NativeEntityDynamicsCase.Rule.IMMERSION ? 1 : 0),
                        "native immersion query or tick state differs");
                break;
            case SUPPORT_ARREST:
                keys(v, "air-ground", "floor-ground", "air-vertical", "floor-vertical");
                require(v.value("air-ground") == 0 && v.value("floor-ground") == 1
                        && v.value("air-vertical") < 0 && v.value("floor-vertical") >= 0,
                        "native support did not arrest downward motion");
                break;
            case SUPPORT_SETTLE:
                keys(v, "air-dead", "floor-dead", "air-block", "floor-block");
                require(v.value("air-dead") == 0 && v.value("floor-dead") == 1
                        && v.value("air-block") == 0 && v.value("floor-block") == 12,
                        "native support did not materialize falling sand");
                break;
            case RAIL_DRAG:
                keys(v, "bare-speed", "rail-speed", "bare-displacement", "rail-displacement");
                require(v.value("bare-speed") >= 0 && v.value("rail-speed") > v.value("bare-speed")
                        && v.value("bare-displacement") > 0 && v.value("rail-displacement") > 0,
                        "native rail condition did not change vehicle motion");
                break;
            case COOLDOWN:
                keys(v, "active-before", "active-one", "active-two", "control-before", "control-two");
                require(v.value("active-before") == 2 && v.value("active-one") == 1
                        && v.value("active-two") == 0 && v.value("control-before") == 0
                        && v.value("control-two") == 0, "native timed recovery differs");
                break;
            default:
                expiry(v);
        }
    }
    private static void expiry(NativeEntityDynamicsObservation v) {
        keys(v, "active-before", "active-after", "active-dead", "control-before", "control-after",
                "control-dead");
        long boundary = v.scene().rule() == NativeEntityDynamicsCase.Rule.ITEM_EXPIRY ? 6000
                : v.scene().rule() == NativeEntityDynamicsCase.Rule.PROJECTILE_EXPIRY ? 1200 : 101;
        require(v.value("active-before") == boundary - 1 && v.value("control-before") == boundary - 2
                && v.value("control-after") == boundary - 1 && v.value("active-dead") == 1
                && v.value("control-dead") == 0, "native expiry boundary was not causal");
        require(v.value("active-after") == (boundary == 101 ? 0 : boundary), "native expiry counter differs");
    }
    private static void keys(NativeEntityDynamicsObservation value, String... keys) {
        require(value.measurements().keySet().equals(new HashSet<>(Arrays.asList(keys))),
                "native dynamics measurements differ from the contract");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
