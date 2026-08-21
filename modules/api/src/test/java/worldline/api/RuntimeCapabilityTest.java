package worldline.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeCapabilityTest {
    private RuntimeCapabilityTest() {}
    public static void main(String[] arguments) {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        fields.put("generation", "41");
        TileObservation tile = new TileObservation(
                new BlockPosition(8, 65, 8), "betaenergistics:storage_cell", fields);
        fields.clear();
        require(tile.field("generation").equals("41") && tile.fields().size() == 1,
                "tile observation changed");
        Map<String, Long> counters = new LinkedHashMap<String, Long>();
        counters.put("catalog.idle_ticks", Long.valueOf(100));
        RuntimeWorkSnapshot before = new RuntimeWorkSnapshot(1, counters);
        counters.put("catalog.idle_ticks", Long.valueOf(250));
        RuntimeWorkSnapshot after = new RuntimeWorkSnapshot(2, counters);
        require(after.increaseFrom(before, "catalog.idle_ticks") == 150, "work delta");
        failure(() -> new TileObservation(new BlockPosition(0, 0, 0), "guess", Collections.emptyMap()));
        failure(() -> after.counter("missing"));
        System.out.println("RuntimeCapabilityTest passed");
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("invalid capability value accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
