package worldline.test;

import java.util.HashMap;
import java.util.Map;
import worldline.api.BlockState;

/** Attempt-local provenance guard for semantic block writes. */
public final class TestMappingAccess {
    private static final ThreadLocal<Map<Integer, Mapping>> BLOCKS = new ThreadLocal<>();
    private TestMappingAccess() {}
    public static void record(BlockState state, String key, String evidence, boolean writable) {
        Map<Integer, Mapping> values = BLOCKS.get();
        if (values == null) { values = new HashMap<>(); BLOCKS.set(values); }
        values.put(id(state), new Mapping(key, evidence, writable));
    }
    public static void requireWrite(BlockState state) {
        Map<Integer, Mapping> values = BLOCKS.get(); Mapping value = values == null ? null : values.get(id(state));
        if (value == null) throw new IllegalStateException("WLTEST E2103: block state " + state
                + " has no promoted semantic mapping");
        if (!value.writable) throw new IllegalStateException("WLTEST E2104: " + value.key
                + " has no promoted write mapping; evidence available: read-only " + value.evidence);
    }
    public static void clear() { BLOCKS.remove(); }
    private static int id(BlockState state) {
        if (state == null) throw new NullPointerException("block state");
        return state.legacyId() << 4 | state.metadata();
    }
    private static final class Mapping {
        final String key, evidence; final boolean writable;
        Mapping(String key, String evidence, boolean writable) {
            this.key = key; this.evidence = evidence; this.writable = writable;
        }
    }
}
