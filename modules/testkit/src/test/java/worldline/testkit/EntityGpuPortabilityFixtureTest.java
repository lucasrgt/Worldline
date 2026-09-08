package worldline.testkit;

import java.util.Map;
import java.util.TreeMap;

/** Synthetic raster mutations exercise tolerance and identity checks without claiming a GPU capture. */
final class EntityGpuPortabilityFixtureTest {
    private EntityGpuPortabilityFixtureTest() { }
    static void execute() {
        Map<String, byte[]> first = frames(), second = frames();
        EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first, "synthetic|device-b|v1", second);
        second.get("90-idle")[(40 * 128 + 40) * 4]++;
        EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first, "synthetic|device-b|v1", second);
        EntityStateFixtureTest.reject(() -> EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first,
                "synthetic|device-a|v2", second));
        byte[] idle = second.get("90-idle");
        for (int y = 35; y < 90; y++) for (int x = 35; x < 90; x++) idle[(y * 128 + x) * 4] = (byte) 255;
        EntityStateFixtureTest.reject(() -> EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first,
                "synthetic|device-b|v1", second));
        Map<String, byte[]> missing = frames(); missing.remove("90-fire");
        EntityStateFixtureTest.reject(() -> EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first,
                "synthetic|device-b|v1", missing));
        Map<String, byte[]> duplicate = frames(); duplicate.put("90-hurt", duplicate.get("90-idle"));
        EntityStateFixtureTest.reject(() -> EntityGpuPortabilityFixture.verify("synthetic|device-a|v1", first,
                "synthetic|device-b|v1", duplicate));
    }
    private static Map<String, byte[]> frames() {
        Map<String, byte[]> frames = new TreeMap<>();
        String[] states = {"idle", "hurt", "walk", "bare", "shadow", "fire", "combined"};
        for (int id : new int[] {90, 54}) for (int state = 0; state < states.length; state++) {
            byte[] rgba = new byte[128 * 128 * 4];
            for (int i = 0; i < rgba.length; i += 4) {
                rgba[i] = 17; rgba[i + 1] = 34; rgba[i + 2] = 51; rgba[i + 3] = (byte) 255;
            }
            if (state != 3) for (int y = 32; y < 96; y++) for (int x = 32; x < 96; x++) {
                rgba[(y * 128 + x) * 4] = (byte) (80 + state * 20);
            }
            frames.put(id + "-" + states[state], rgba);
        }
        return frames;
    }
}
