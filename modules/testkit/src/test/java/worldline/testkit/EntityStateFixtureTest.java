package worldline.testkit;

import java.util.Map;
import java.util.TreeMap;

/** Mutation acceptance rejects fabricated immunity, loot contexts and AI target choices. */
public final class EntityStateFixtureTest {
    private EntityStateFixtureTest() { }
    public static void execute() {
        Map<String, Long> good = sample();
        EntityStateEvidence evidence = EntityStateFixture.verify(good);
        if (!evidence.equals(EntityStateFixture.verify(sample()))) throw new AssertionError("state equality");
        for (String key : new String[] {"57-fire-after", "90-drown-after", "drops-90-0-1-id",
                "drops-91-0-1-count", "target-54-0", "target-52-1", "target-57-1-anger"}) {
            Map<String, Long> bad = new TreeMap<>(good); bad.put(key, -1L);
            reject(() -> EntityStateFixture.verify(bad));
        }
        Map<String, Long> missing = new TreeMap<>(good); missing.remove("90-lava-fire");
        reject(() -> EntityStateFixture.verify(missing));
        Map<String, Long> extra = new TreeMap<>(good); extra.put("unrequested", 1L);
        reject(() -> EntityStateFixture.verify(extra));
        EntityGpuPortabilityFixtureTest.execute();
    }
    private static Map<String, Long> sample() {
        Map<String, Long> result = new TreeMap<>();
        for (int id : new int[] {90, 57}) {
            String[] causes = {"control", "fire", "lava", "fall", "drown", "suffocate"};
            int[] losses = id == 90 ? new int[] {0, 1, 4, 3, 2, 1} : new int[] {0, 0, 0, 3, 2, 1};
            for (int i = 0; i < causes.length; i++) {
                String p = id + "-" + causes[i];
                result.put(p + "-before", 20L); result.put(p + "-after", 20L - losses[i]);
                result.put(p + "-fire", id == 90 && i == 2 ? 600L : 0L);
                result.put(p + "-air", i == 4 ? 0L : 300L);
            }
        }
        for (int id : new int[] {90, 91}) for (int seed = 0; seed < 8; seed++) {
            for (int active = 0; active < 2; active++) {
                String p = "drops-" + id + '-' + seed + '-' + active;
                result.put(p + "-count", id == 91 && active == 1 ? 0L : 1L);
                result.put(p + "-id", id == 90 ? 319L + active : active == 0 ? 35L : 0L);
                result.put(p + "-damage", id == 91 && active == 0 ? 5L : 0L);
                result.put(p + "-health", -1000L);
            }
        }
        for (int id : new int[] {54, 52, 57}) for (int active = 0; active < 2; active++) {
            String p = "target-" + id + '-' + active; result.put(p, (long) active);
            if (id == 57) result.put(p + "-anger", active == 0 ? 0L : 500L);
        }
        return result;
    }
    static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid entity context evidence accepted");
    }
}
