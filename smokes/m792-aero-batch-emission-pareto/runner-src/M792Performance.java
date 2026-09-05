import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Predeclared per-order scene guardrails; a failed arm cannot be averaged away. */
final class M792Performance {
    private final boolean baselineFirst;
    private final Properties[][] values = new Properties[2][3];

    private M792Performance(boolean baselineFirst) { this.baselineFirst = baselineFirst; }

    static M792Performance read(Path game, boolean baselineFirst, Properties config) throws Exception {
        M792Performance result = new M792Performance(baselineFirst);
        for (int arm = 0; arm < 2; arm++) for (int scene = 0; scene < 3; scene++) {
            String stem = "profile-" + (arm == 0 ? "replay-off" : "replay-on") + "-" + scene;
            Properties properties = new Properties();
            SmokeSupport.load(game.resolve(stem + ".properties"), properties);
            SmokeSupport.require(HexDigest.sha256Hex(Files.readAllBytes(game.resolve(stem + ".wlpr")))
                .equals(properties.getProperty("artifact.sha256")), "M792 profiler checksum drift");
            SmokeSupport.require("480".equals(properties.getProperty("frames")), "M792 profile incomplete");
            result.values[arm][scene] = properties;
        }
        return result;
    }

    static boolean evaluate(List<M792Performance> runs, StringBuilder evidence) {
        boolean passes = true;
        for (boolean first : new boolean[] {true, false}) {
            double allOff = 0, allOn = 0;
            for (int scene = 0; scene < 3; scene++) {
                double off = total(runs, first, scene, 0, "render.mean");
                double on = total(runs, first, scene, 1, "render.mean");
                double ratio = on / off;
                double p99 = ratio(total(runs, first, scene, 1, "render.p99"),
                    total(runs, first, scene, 0, "render.p99"));
                double allocation = ratio(total(runs, first, scene, 1, "allocation.mean"),
                    total(runs, first, scene, 0, "allocation.mean"));
                long hitchesOff = total(runs, first, scene, 0, "render.hitches");
                long hitchesOn = total(runs, first, scene, 1, "render.hitches");
                boolean scenePass = ratio <= (scene == 0 ? 0.90 : 1.03)
                    && p99 <= 1.05 && allocation <= 1.02 && hitchesOn <= hitchesOff;
                passes &= scenePass;
                allOff += off; allOn += on;
                evidence.append("order=").append(first ? "off-on" : "on-off").append(",scene=")
                    .append(scene).append(",render.ratio=").append(ratio).append(",p99.ratio=")
                    .append(p99).append(",allocation.ratio=").append(allocation)
                    .append(",hitches=").append(hitchesOff).append("->").append(hitchesOn)
                    .append(",passes=").append(scenePass).append('\n');
            }
            passes &= allOn / allOff <= 0.95;
        }
        for (M792Performance run : runs) {
            for (int scene = 0; scene < 3; scene++) {
                passes &= run.number(0, scene, "matches.total") == 0;
                passes &= run.number(1, scene, "bytes.maximum") <= 16L * 1024 * 1024;
            }
            passes &= run.number(1, 0, "matches.total") > 0;
        }
        return passes;
    }

    private static long total(List<M792Performance> runs, boolean first, int scene, int arm, String key) {
        long total = 0;
        for (M792Performance run : runs) if (run.baselineFirst == first) total += run.number(arm, scene, key);
        return total;
    }
    private long number(int arm, int scene, String key) {
        long value = Long.parseLong(SmokeSupport.value(values[arm][scene], key));
        SmokeSupport.require(value >= 0, "negative M792 observation: " + key);
        return value;
    }

    private static double ratio(long candidate, long baseline) {
        return candidate / (double) Math.max(1L, baseline);
    }

    static void selfTest() {
        SmokeSupport.require(ratio(110, 100) > 1.05 && ratio(103, 100) > 1.02
            && ratio(99, 100) == 0.99 && ratio(0, 0) == 0.0,
            "M792 fractional ratio or empty allocation regression");
    }
}
