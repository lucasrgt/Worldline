package worldline.testkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsCase.Rule;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Negative public acceptance tests independently challenge causal fields and route ownership. */
final class NativeEntityStimulusFixtureTest {
    private NativeEntityStimulusFixtureTest() { }
    static void execute() {
        spawn(); damage(); pickup(); drops(); routes();
    }
    private static void spawn() {
        NativeEntityDynamicsCase c = new NativeEntityDynamicsCase(1, "EntityItem", "UNIVERSAL", Rule.SPAWN);
        Map<String, Long> values = new HashMap<>();
        values.put("before-member", 0L); values.put("after-member", 1L); values.put("joined", 1L);
        values.put("packet", 21L); values.put("identity", 1L); values.put("coordinates", 1L);
        values.put("roundtrip", 1L); values.put("item-id", 1L); values.put("count", 2L); values.put("item-damage", 0L);
        accept(c, values);
        mutate(c, values, "before-member", 1); mutate(c, values, "identity", 0);
        mutate(c, values, "coordinates", 0); mutate(c, values, "roundtrip", 0);
        mutate(c, values, "count", 1);
        reject(() -> accept(new NativeEntityDynamicsCase(1, "EntityItem", "ARCHETYPE", Rule.SPAWN), values));
    }
    private static void damage() {
        NativeEntityDynamicsCase c = new NativeEntityDynamicsCase(1, "EntityItem", "ARCHETYPE", Rule.DAMAGE);
        Map<String, Long> values = new HashMap<>();
        for (String p : new String[] {"control-", "low-", "lethal-"}) {
            boolean lethal = p.equals("lethal-");
            values.put(p + "before", 5L); values.put(p + "accepted", 0L);
            values.put(p + "after", lethal ? -1019L : p.equals("low-") ? 4L : 5L);
            values.put(p + "dead", lethal ? 1L : 0L); values.put(p + "immediate-dead", lethal ? 1L : 0L);
        }
        accept(c, values);
        mutate(c, values, "low-accepted", 1); mutate(c, values, "low-after", 5);
        mutate(c, values, "lethal-dead", 0); mutate(c, values, "control-dead", 1);
    }
    private static void pickup() {
        NativeEntityDynamicsCase c = new NativeEntityDynamicsCase(10, "EntityArrow", "ARCHETYPE", Rule.INTERACTION);
        Map<String, Long> values = Map.of("control-inventory-id", 0L, "control-inventory-count", 0L,
                "control-dead", 0L, "active-inventory-id", 262L, "active-inventory-count", 1L, "active-dead", 1L);
        accept(c, values);
        mutate(c, values, "active-inventory-count", 0); mutate(c, values, "active-inventory-id", 1);
        mutate(c, values, "active-dead", 0); mutate(c, values, "control-inventory-count", 1);
        Map<String, Long> incomplete = new HashMap<>(values);
        incomplete.remove("control-dead");
        reject(() -> accept(c, incomplete));
    }
    private static void drops() {
        NativeEntityDynamicsCase c = new NativeEntityDynamicsCase(9, "EntityPainting", "SINGULAR", Rule.DROPS);
        Map<String, Long> values = new HashMap<>();
        for (int seed = 0; seed < 8; seed++) for (String mode : new String[] {"control", "death"}) {
            String p = "seed" + seed + '-' + mode + '-';
            long count = mode.equals("death") ? 1 : 0;
            values.put(p + "entities", count); values.put(p + "count", count);
            values.put(p + "other", 0L); values.put(p + "health", -1L); values.put(p + "dead", count);
            if (count == 1) values.put(p + "item-321-0", 1L);
        }
        accept(c, values);
        mutate(c, values, "seed3-death-count", 0); mutate(c, values, "seed2-control-entities", 1);
        mutate(c, values, "seed7-death-item-321-0", 2);
        Map<String, Long> unrelated = new HashMap<>(values);
        unrelated.remove("seed1-death-item-321-0"); unrelated.put("seed1-death-item-334-0", 1L);
        reject(() -> accept(c, unrelated));
    }
    private static void routes() {
        NativeEntityDynamicsCase motion = new NativeEntityDynamicsCase(1, "EntityItem", "ARCHETYPE", Rule.MOTION);
        NativeEntityDynamicsObservation moved = new NativeEntityDynamicsObservation(motion, Map.of(
                "open-delta", 5000L, "blocked-delta", 1500L, "collision", 1L, "blocked-motion", 0L));
        reject(() -> NativeEntityStimulusFixture.verify(List.of(motion), List.of(moved)));
        NativeEntityDynamicsCase spawn = new NativeEntityDynamicsCase(1, "EntityItem", "UNIVERSAL", Rule.SPAWN);
        NativeEntityDynamicsObservation wrong = new NativeEntityDynamicsObservation(spawn, Map.of(
                "active-before", 100L, "active-after", 0L, "active-dead", 1L,
                "control-before", 99L, "control-after", 100L, "control-dead", 0L));
        reject(() -> NativeEntityDynamicsFixture.verify(List.of(spawn), List.of(wrong)));
        reject(() -> NativeEntityStimulusFixture.verify(List.of(spawn), List.of(moved)));
        reject(() -> NativeEntityStimulusFixture.verify(List.of(spawn), List.of()));
        reject(() -> NativeEntityStimulusFixture.verify(List.of(), List.of()));
    }
    private static void accept(NativeEntityDynamicsCase c, Map<String, Long> values) {
        NativeEntityDynamicsObservation v = new NativeEntityDynamicsObservation(c, values);
        NativeEntityStimulusEvidence first = NativeEntityStimulusFixture.verify(List.of(c), List.of(v));
        NativeEntityStimulusEvidence second = NativeEntityStimulusFixture.verify(List.of(c), List.of(v));
        if (!first.equals(second) || first.hashCode() != second.hashCode()) {
            throw new AssertionError("evidence differs");
        }
        reject(() -> NativeEntityStimulusFixture.verify(List.of(c, c), List.of(v, v)));
    }
    private static void mutate(NativeEntityDynamicsCase c, Map<String, Long> values, String key, long wrong) {
        Map<String, Long> changed = new HashMap<>(values);
        changed.put(key, wrong);
        reject(() -> accept(c, changed));
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid native stimulus evidence was accepted");
    }
}
