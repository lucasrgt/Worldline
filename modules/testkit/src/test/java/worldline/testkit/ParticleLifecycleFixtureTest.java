package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.testapi.ParticleLifecycleObservation;

/** Public boundary mutations: omitted ticks, wrong order, premature death and visible residue. */
public final class ParticleLifecycleFixtureTest {
    private ParticleLifecycleFixtureTest() { }
    public static void execute() {
        List<ParticleLifecycleObservation> rows = rows();
        ParticleLifecycleEvidence evidence = ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), rows);
        require(evidence.equals(ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), rows())), "equality");
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), rows.subList(1, 10)));
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntityFlameFX"), rows));
        List<ParticleLifecycleObservation> altered = rows();
        altered.set(4, observation(4, true, 0, 100));
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), altered));
        altered.set(4, observation(4, false, 1, 100)); altered.set(9, observation(9, true, 0, 1));
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), altered));
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntitySmokeFX", "EntitySmokeFX"),
                java.util.stream.Stream.concat(rows.stream(), rows.stream()).toList()));
        List<ParticleLifecycleObservation> reordered = rows();
        java.util.Collections.swap(reordered, 2, 3);
        reject(() -> ParticleLifecycleFixture.verify(List.of("EntitySmokeFX"), reordered));
    }
    private static List<ParticleLifecycleObservation> rows() {
        List<ParticleLifecycleObservation> rows = new ArrayList<>();
        for (int tick = 0; tick <= 9; tick++) rows.add(observation(tick, tick == 9,
                tick == 9 ? 0 : 1, tick == 0 || tick == 9 ? 0 : 100));
        return rows;
    }
    private static ParticleLifecycleObservation observation(int tick, boolean dead, int count, int pixels) {
        return new ParticleLifecycleObservation("EntitySmokeFX", tick, tick, dead, count,
                "0000000000000000,4050000000000000,0000000000000000", pixels, "a".repeat(64));
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid particle evidence accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
