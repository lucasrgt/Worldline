import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
/** Per-order, per-case guardrails plus immutable baseline regression checks. */
final class M794Performance {
  final boolean current, candidate, offFirst;
  private final Properties[] values = new Properties[12];
  private M794Performance(boolean current, boolean candidate, boolean offFirst) {
    this.current = current;
    this.candidate = candidate;
    this.offFirst = offFirst;
  }
  static M794Performance read(Path game, boolean current, boolean candidate, boolean offFirst) throws Exception {
    M794Performance result = new M794Performance(current, candidate, offFirst);
    for (int scene = 0; scene < 12; scene++) {
      String stem = "profile-" + (candidate ? "replay-on" : "replay-off") + "-" + scene;
      Properties properties = new Properties();
      SmokeSupport.load(game.resolve(stem + ".properties"), properties);
      SmokeSupport.require(M794Digest.sha256Hex(Files.readAllBytes(game.resolve(stem + ".wlpr")))
                               .equals(properties.getProperty("artifact.sha256")),
          "M794 profiler checksum drift");
      SmokeSupport.require(
          Integer.toString(scene == 0 ? 180 : 120).equals(properties.getProperty("frames")), "M794 profile incomplete");
      result.values[scene] = properties;
    }
    return result;
  }
  static boolean evaluate(List<M794Performance> runs, StringBuilder evidence) {
    boolean passes = true;
    for (boolean first : new boolean[] {true, false})
      for (int scene = 0; scene < 12; scene++) {
        passes &= compare(runs, evidence, first, scene, "default", false, 0, true, 0, 1.03);
        passes &= compare(
            runs, evidence, first, scene, "candidate", true, 0, true, 1, scene == 2 || scene == 3 ? 0.90 : 1.03);
      }
    for (M794Performance run : runs)
      for (int scene = 0; scene < 12; scene++) {
        passes &= run.number(scene, "bytes.maximum") <= 16L * 1024 * 1024;
        if (!run.candidate || !run.current || scene == 1 || scene >= 4 && scene <= 8)
          passes &= run.number(scene, "matches.total") == 0;
        if (run.current && run.candidate && (scene == 2 || scene == 3))
          passes &= run.number(scene, "matches.total") > 0;
        if (scene == 10)
          passes &= run.number(scene, "rebuilds.total") > 0;
      }
    return passes;
  }
  private static boolean compare(List<M794Performance> runs, StringBuilder evidence, boolean first, int scene,
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
    evidence.append(label)
        .append(",order=")
        .append(first ? "baseline-first" : "candidate-first")
        .append(",case=")
        .append(scene)
        .append(",render=")
        .append(render)
        .append(",p99=")
        .append(p99)
        .append(",allocation=")
        .append(allocation)
        .append(",hitches=")
        .append(before)
        .append("->")
        .append(after)
        .append(",passes=")
        .append(passes)
        .append('\n');
    return passes;
  }
  private static long total(
      List<M794Performance> runs, boolean current, boolean first, int scene, int arm, String key) {
    long total = 0;
    int count = 0;
    for (M794Performance run : runs)
      if (run.current == current && run.candidate == (arm == 1) && run.offFirst == first) {
        total += run.number(scene, key);
        count++;
      }
    SmokeSupport.require(count == 2, "M794 counterbalance incomplete");
    return total;
  }
  private long number(int scene, String key) {
    long value = Long.parseLong(SmokeSupport.value(values[scene], key));
    SmokeSupport.require(value >= 0, "negative M794 observation: " + key);
    return value;
  }
  private static double ratio(long candidate, long baseline) {
    return candidate / (double) Math.max(1L, baseline);
  }
  static void selfTest() {
    SmokeSupport.require(
        ratio(110, 100) > 1.05 && ratio(103, 100) > 1.02 && ratio(99, 100) == 0.99 && ratio(0, 0) == 0.0,
        "M794 ratio regression");
  }
}
