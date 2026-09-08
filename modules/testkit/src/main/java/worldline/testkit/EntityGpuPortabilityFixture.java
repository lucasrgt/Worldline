package worldline.testkit;

import java.util.Map;
import java.util.TreeMap;
import worldline.api.RuntimeSnapshot;

/** Bounded cross-device RGBA acceptance; the caller supplies captures from two distinct GL devices. */
public final class EntityGpuPortabilityFixture {
    private EntityGpuPortabilityFixture() { }
    public static EntityStateEvidence verify(String firstDevice, Map<String, byte[]> first,
            String secondDevice, Map<String, byte[]> second) {
        require(device(firstDevice) && device(secondDevice) && !identity(firstDevice).equals(identity(secondDevice)),
                "two distinct GL vendor/renderer identities are required");
        Map<String, String> a = describe(first), b = describe(second);
        EntityVisualStateFixture.verify(a); EntityVisualStateFixture.verify(b);
        Map<String, String> evidence = new TreeMap<>();
        evidence.put("first-device", firstDevice); evidence.put("second-device", secondDevice);
        for (String key : first.keySet()) {
            byte[] left = first.get(key), right = second.get(key);
            long union = 0, intersection = 0, wide = 0, absolute = 0;
            for (int i = 0; i < left.length; i += 4) {
                boolean l = foreground(left, i), r = foreground(right, i);
                if (l || r) union++;
                if (l && r) intersection++;
                for (int channel = 0; channel < 4; channel++) {
                    int difference = Math.abs((left[i + channel] & 255) - (right[i + channel] & 255));
                    if (difference > 4) wide++;
                    absolute += difference;
                }
            }
            require(union == 0 || intersection * 100 >= union * 99, key + " geometry overlap below 99 percent");
            require(wide * 200 <= left.length && absolute <= left.length, key + " color tolerance exceeded");
            evidence.put(key + "-first-sha256", a.get(key + "-sha256"));
            evidence.put(key + "-second-sha256", b.get(key + "-sha256"));
            evidence.put(key + "-intersection", Long.toString(intersection));
            evidence.put(key + "-union", Long.toString(union));
            evidence.put(key + "-wide-channels", Long.toString(wide));
            evidence.put(key + "-absolute-channel-delta", Long.toString(absolute));
        }
        return new EntityStateEvidence("cross-device-rgba", evidence);
    }
    private static Map<String, String> describe(Map<String, byte[]> frames) {
        require(frames != null && frames.size() == 14, "incomplete GPU frame family");
        Map<String, String> values = new TreeMap<>();
        for (int id : new int[] {90, 54}) for (String state : new String[] {
                "idle", "hurt", "walk", "bare", "shadow", "fire", "combined"}) {
            String key = id + "-" + state; byte[] rgba = frames.get(key);
            require(rgba != null && rgba.length == 128 * 128 * 4, "invalid RGBA frame: " + key);
            int pixels = 0;
            for (int i = 0; i < rgba.length; i += 4) if (foreground(rgba, i)) pixels++;
            values.put(key + "-pixels", Integer.toString(pixels));
            values.put(key + "-sha256", RuntimeSnapshot.of(rgba).sha256());
        }
        return values;
    }
    private static boolean foreground(byte[] rgba, int i) {
        return (rgba[i] & 255) != 17 || (rgba[i + 1] & 255) != 34 || (rgba[i + 2] & 255) != 51;
    }
    private static boolean device(String value) {
        return value != null && value.matches("[^|\\r\\n]+[|][^|\\r\\n]+[|][^|\\r\\n]+");
    }
    private static String identity(String device) { return device.substring(0, device.lastIndexOf('|')); }
    private static void require(boolean value, String message) { EntityStateFixture.require(value, message); }
}
