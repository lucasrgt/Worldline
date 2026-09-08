package worldline.testkit;

import java.util.List;
import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Rejects missing controls, invented expiry, reordered observations and unconstrained motion. */
final class NativeEntityDynamicsFixtureTest {
    private NativeEntityDynamicsFixtureTest() { }
    static void execute() {
        immersion();
        NativeEntityDynamicsCase motion = new NativeEntityDynamicsCase(1, "EntityItem", "ARCHETYPE",
                NativeEntityDynamicsCase.Rule.MOTION);
        NativeEntityDynamicsCase expiry = new NativeEntityDynamicsCase(1, "EntityItem", "ARCHETYPE",
                NativeEntityDynamicsCase.Rule.ITEM_EXPIRY);
        NativeEntityDynamicsObservation moved = observation(motion, Map.of("open-delta", 5000L,
                "blocked-delta", 1500L, "collision", 1L, "blocked-motion", 0L));
        NativeEntityDynamicsObservation expired = observation(expiry, Map.of("active-before", 5999L,
                "active-after", 6000L, "active-dead", 1L, "control-before", 5998L,
                "control-after", 5999L, "control-dead", 0L));
        List<NativeEntityDynamicsCase> cases = List.of(motion, expiry);
        NativeEntityDynamicsEvidence evidence = NativeEntityDynamicsFixture.verify(cases, List.of(moved, expired));
        require(evidence.equals(NativeEntityDynamicsFixture.verify(cases, List.of(moved, expired))), "evidence equality");
        require(evidence.canonical().contains("b1.7.3:entity/001#tick-lifecycle|ARCHETYPE"), "claim route");
        reject(() -> NativeEntityDynamicsFixture.verify(cases, List.of(moved)));
        reject(() -> NativeEntityDynamicsFixture.verify(cases, List.of(expired, moved)));
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(motion, motion), List.of(moved, moved)));
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(motion), List.of(observation(motion,
                Map.of("open-delta", 5000L, "blocked-delta", 5000L, "collision", 1L, "blocked-motion", 0L)))));
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(expiry), List.of(observation(expiry,
                Map.of("active-before", 5999L, "active-after", 6000L, "active-dead", 1L,
                        "control-before", 5998L, "control-after", 5999L, "control-dead", 1L)))));
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(expiry), List.of(observation(expiry,
                Map.of("active-before", 5999L, "active-after", 6000L, "active-dead", 1L)))));
    }
    private static NativeEntityDynamicsObservation observation(NativeEntityDynamicsCase scene, Map<String, Long> data) {
        return new NativeEntityDynamicsObservation(scene, data);
    }
    private static void immersion() {
        NativeEntityDynamicsCase scene = new NativeEntityDynamicsCase(93, "EntityChicken", "ARCHETYPE",
                NativeEntityDynamicsCase.Rule.SHORT_IMMERSION);
        Map<String, Long> values = Map.of("dry-before", 0L, "dry-after", 0L, "wet-before", 0L,
                "wet-after", 0L, "dry-inside", 0L, "wet-inside", 1L);
        NativeEntityDynamicsFixture.verify(List.of(scene), List.of(observation(scene, values)));
        Map<String, Long> wrong = new java.util.HashMap<>(values);
        wrong.put("wet-inside", 0L);
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(scene), List.of(observation(scene, wrong))));
        wrong.put("wet-inside", 1L); wrong.put("wet-after", 1L);
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(scene), List.of(observation(scene, wrong))));
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid native dynamics proof was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
