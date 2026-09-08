package worldline.testkit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Public acceptance for bounded native environmental, death-context and AI contrasts. */
public final class EntityStateFixture {
    private EntityStateFixture() { }
    public static EntityStateEvidence verify(Map<String, Long> values) {
        require(values != null, "missing entity state observations");
        Set<String> keys = new HashSet<>();
        for (int id : new int[] {90, 57}) for (String cause : new String[] {
                "control", "fire", "lava", "fall", "drown", "suffocate"}) {
            String p = id + "-" + cause;
            long before = take(values, keys, p + "-before"), after = take(values, keys, p + "-after");
            long fire = take(values, keys, p + "-fire"), air = take(values, keys, p + "-air");
            int damage = cause.equals("control") ? 0 : cause.equals("fall") ? 3
                    : cause.equals("drown") ? 2 : cause.equals("lava") ? 4 : 1;
            if (id == 57 && (cause.equals("fire") || cause.equals("lava"))) damage = 0;
            require(before > 0 && before - after == damage, p + " native damage differs");
            require(cause.equals("lava") && id == 90 ? fire > 0 : fire <= 0, p + " ignition differs");
            require(cause.equals("drown") ? air == 0 : air == 300, p + " air differs");
        }
        long pigYield = 0;
        for (int id : new int[] {90, 91}) for (int seed = 0; seed < 8; seed++) {
            long paired = -1;
            for (int active = 0; active < 2; active++) {
                String p = "drops-" + id + '-' + seed + '-' + active;
                long count = take(values, keys, p + "-count"), item = take(values, keys, p + "-id");
                long damage = take(values, keys, p + "-damage"), health = take(values, keys, p + "-health");
                require(health <= 0 && count >= 0 && count <= 2, p + " invalid death yield");
                if (id == 90) {
                    require(damage == 0 && item == (count == 0 ? 0 : active == 0 ? 319 : 320),
                            p + " pork context differs");
                    if (active == 0) paired = count; else require(count == paired, "matched pork seeds differ");
                    pigYield += count;
                } else require(count == (active == 0 ? 1 : 0) && item == (active == 0 ? 35 : 0)
                        && damage == (active == 0 ? 5 : 0), p + " fleece context differs");
            }
        }
        require(pigYield > 0, "vacuous pork matrix");
        for (int id : new int[] {54, 52, 57}) for (int active = 0; active < 2; active++) {
            String p = "target-" + id + '-' + active;
            require(take(values, keys, p) == active, p + " AI branch differs");
            if (id == 57) {
                long anger = take(values, keys, p + "-anger");
                require(active == 0 ? anger == 0 : anger >= 400 && anger < 800, "native anger differs");
            }
        }
        require(keys.equals(values.keySet()), "unexpected entity state observations");
        return new EntityStateEvidence("gameplay", values);
    }
    private static long take(Map<String, Long> values, Set<String> keys, String key) {
        require(keys.add(key) && values.get(key) != null, "missing or repeated observation: " + key);
        return values.get(key);
    }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
