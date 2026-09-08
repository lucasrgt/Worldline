import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
/** Per-order, per-case guardrails plus immutable baseline regression checks. */
final class M793Performance {
    final boolean current, offFirst;
    private final Properties[][] values = new Properties[2][12];
    private M793Performance(boolean current, boolean offFirst) { this.current = current; this.offFirst = offFirst; }
    static M793Performance read(Path game, boolean current, boolean offFirst) throws Exception {
        M793Performance result = new M793Performance(current, offFirst);
        for (int arm = 0; arm < 2; arm++) for (int scene = 0; scene < 12; scene++) {
            String stem = "profile-" + (arm == 0 ? "replay-off" : "replay-on") + "-" + scene;
            Properties properties = new Properties();
            SmokeSupport.load(game.resolve(stem + ".properties"), properties);
            SmokeSupport.require(M793Digest.sha256Hex(Files.readAllBytes(game.resolve(stem + ".wlpr")))
                .equals(properties.getProperty("artifact.sha256")), "M793 profiler checksum drift");
            SmokeSupport.require(Integer.toString(scene == 0 ? 180 : 120).equals(properties.getProperty("frames")),
                "M793 profile incomplete");
            result.values[arm][scene] = properties;
        }
        return result;
    }
    static boolean evaluate(List<M793Performance> runs, StringBuilder evidence) {
        boolean passes = true;
        for (boolean first : new boolean[] {true, false}) for (int scene = 0; scene < 12; scene++) {
            passes &= compare(runs, evidence, first, scene, "default", false, 0, true, 0, 1.03);
            if (scene == 0) {
                // Only the first arm in a fresh JVM is cold. Never compare it with its warmed second arm.
                int coldArm = first ? 0 : 1;
                passes &= compare(runs, evidence, first, scene, "cold", false, coldArm, true, coldArm, 1.03);
            } else passes &= compare(runs, evidence, first, scene, "candidate", true, 0, true, 1,
                scene == 2 || scene == 3 ? 0.90 : 1.03);
        }
        for (M793Performance run : runs) for (int scene = 0; scene < 12; scene++) {
            passes &= run.number(0, scene, "matches.total") == 0;
            passes &= run.number(1, scene, "bytes.maximum") <= 16L * 1024 * 1024;
            if (!run.current || scene >= 5 && scene <= 8) passes &= run.number(1, scene, "matches.total") == 0;
            if (run.current && (scene == 2 || scene == 3)) passes &= run.number(1, scene, "matches.total") > 0;
            if (scene == 10) passes &= run.number(0, scene, "rebuilds.total") > 0 && run.number(1, scene, "rebuilds.total") > 0;
        }
        return passes;
    }
    private static boolean compare(List<M793Performance> runs, StringBuilder evidence, boolean first, int scene,
            String label, boolean baseCurrent, int baseArm, boolean testCurrent, int testArm, double limit) {
        double render = ratio(total(runs, testCurrent, first, scene, testArm, "render.mean"),
            total(runs, baseCurrent, first, scene, baseArm, "render.mean"));
        double p99 = ratio(total(runs, testCurrent, first, scene, testArm, "render.p99"),
            total(runs, baseCurrent, first, scene, baseArm, "render.p99"));
        double allocation = ratio(total(runs, testCurrent, first, scene, testArm, "allocation.mean"),
            total(runs, baseCurrent, first, scene, baseArm, "allocation.mean"));
        long before = total(runs, baseCurrent, first, scene, baseArm, "render.hitches");
        long after = total(runs, testCurrent, first, scene, testArm, "render.hitches");
        boolean passes = render <= limit && p99 <= 1.05 && allocation <= 1.02 && after <= before;
        evidence.append(label).append(",order=").append(first ? "off-on" : "on-off").append(",case=")
            .append(scene).append(",render=").append(render).append(",p99=").append(p99)
            .append(",allocation=").append(allocation).append(",hitches=").append(before).append("->")
            .append(after).append(",passes=").append(passes).append('\n');
        return passes;
    }
    private static long total(List<M793Performance> runs, boolean current, boolean first, int scene, int arm, String key) {
        long total = 0; int count = 0;
        for (M793Performance run : runs) if (run.current == current && run.offFirst == first) {
            total += run.number(arm, scene, key); count++;
        }
        SmokeSupport.require(count == 2, "M793 counterbalance incomplete");
        return total;
    }
    private long number(int arm, int scene, String key) {
        long value = Long.parseLong(SmokeSupport.value(values[arm][scene], key));
        SmokeSupport.require(value >= 0, "negative M793 observation: " + key);
        return value;
    }
    private static double ratio(long candidate, long baseline) { return candidate / (double) Math.max(1L, baseline); }
    static void selfTest() {
        SmokeSupport.require(ratio(110, 100) > 1.05 && ratio(103, 100) > 1.02
            && ratio(99, 100) == 0.99 && ratio(0, 0) == 0.0, "M793 ratio regression");
    }
}
