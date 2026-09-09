package worldline.testkit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Accepts visible native animation and independently isolated shadow/fire contrasts. */
public final class EntityVisualStateFixture {
    private EntityVisualStateFixture() { }
    public static EntityStateEvidence verify(Map<String, String> values) {
        EntityStateFixture.require(values != null, "missing native render states");
        Set<String> keys = new HashSet<>();
        for (int id : new int[] {90, 54}) {
            Set<String> hashes = new HashSet<>();
            for (String state : new String[] {"idle", "hurt", "walk", "bare", "shadow", "fire", "combined"}) {
                String p = id + "-" + state, hash = values.get(p + "-sha256");
                keys.add(p + "-pixels"); keys.add(p + "-sha256");
                EntityStateFixture.require(hash != null && hash.matches("[a-f0-9]{64}") && hashes.add(hash),
                        p + " native render contrast absent");
                String raw = values.get(p + "-pixels");
                EntityStateFixture.require(raw != null && raw.matches("[0-9]+"), "missing pixel observation");
                int pixels = Integer.parseInt(raw);
                EntityStateFixture.require(state.equals("bare") ? pixels == 0 : pixels > 0 && pixels < 16384,
                        p + " native geometry differs");
            }
        }
        EntityStateFixture.require(keys.equals(values.keySet()), "unexpected native render observations");
        return new EntityStateEvidence("render", values);
    }
}
