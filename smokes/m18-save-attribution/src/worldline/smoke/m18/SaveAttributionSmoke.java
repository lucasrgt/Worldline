package worldline.smoke.m18;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import worldline.aero.AeroSaveProbe;
import worldline.aero.AeroSaveProbe.Sample;

/** Qualifies paired skip-versus-live save attribution on one dense scene. */
public final class SaveAttributionSmoke {
  private SaveAttributionSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length == 3, "expected skipped log, live log, and frame limit");
    int limit = Integer.parseInt(arguments[2]);
    Window skipped = Window.load(Paths.get(arguments[0]), limit);
    Window live = Window.load(Paths.get(arguments[1]), limit);
    require(skipped.saveMaxUs == 0L && skipped.saveSkipped > 0L,
        "skipped run did not cancel the injected non-forced save");
    require(live.saveMaxUs >= 100L && live.saveSkipped == 0L,
        "live run did not measure a visible world save");
    Sample save = live.highestSave();
    require(save != null && save.saveUs == live.saveMaxUs,
        "live save frame missing from the measured window");
    System.out.println("skipped.frames=" + skipped.frames + " saveMaxUs=" + skipped.saveMaxUs
        + " saveSkipped=" + skipped.saveSkipped + " compileMaxUs=" + skipped.compileMaxUs
        + " gcMaxUs=" + skipped.gcMaxUs + " heapMaxMb=" + skipped.heapMaxMb);
    System.out.println("live.frames=" + live.frames + " saveMaxUs=" + live.saveMaxUs
        + " saveAllocMilliMb=" + save.saveAllocMilliMb + " compileMaxUs=" + live.compileMaxUs
        + " gcMaxUs=" + live.gcMaxUs + " heapMaxMb=" + live.heapMaxMb
        + " frameMaxUs=" + live.frameMaxUs);
    System.out.println("live.saveFrame.frameUs=" + save.frameUs + " saveUs=" + save.saveUs
        + " compileUs=" + save.compileUs + " gcUs=" + save.gcUs + " heapUsedMb=" + save.heapUsedMb);
    System.out.println("live.worstFrame.class=" + live.classifyWorst());
    String report = "scene=STATIONARY_DENSE\n"
        + "control=PAIRED_SKIP_VS_LIVE_NON_FORCED_SAVE\n"
        + "skipped=SAVE_CANCELLED_ZERO_SAVE_MS\n"
        + "live=SAVE_VISIBLE_ON_TIMELINE\n"
        + "timeline=SAVE_COMPILE_GC_HEAP_ALLOC\n"
        + "historical.spike=NON_CLAIM\n"
        + "scheduler.promotion=NO_GO\n"
        + "shipping.status=LAB_ONLY\n";
    System.out.println("WORLDLINE_M18_SAVE_ATTRIBUTION=PASS");
    System.out.print(report);
    System.out.println("evidence.sha256=" + sha256(report));
  }

  private static String sha256(String value) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : digest)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }

  private static final class Window {
    final int frames;
    final long saveMaxUs, saveSkipped, compileMaxUs, gcMaxUs, heapMaxMb, frameMaxUs;
    private final List<Sample> samples;

    private Window(List<Sample> samples) {
      this.samples = samples;
      this.frames = samples.size();
      long save = 0, skipped = 0, compile = 0, gc = 0, heap = 0, frame = 0;
      for (Sample sample : samples) {
        save = Math.max(save, sample.saveUs);
        skipped += sample.saveSkipped;
        compile = Math.max(compile, Math.max(sample.compileUs, sample.compileMaxUs));
        gc = Math.max(gc, sample.gcUs);
        heap = Math.max(heap, sample.heapUsedMb);
        frame = Math.max(frame, sample.frameUs);
      }
      this.saveMaxUs = save;
      this.saveSkipped = skipped;
      this.compileMaxUs = compile;
      this.gcMaxUs = gc;
      this.heapMaxMb = heap;
      this.frameMaxUs = frame;
    }

    static Window load(Path path, int limit) throws Exception {
      List<Sample> all = new ArrayList<Sample>();
      for (String row : Files.readAllLines(path, StandardCharsets.UTF_8))
        if (row.startsWith("[Aero_"))
          all.add(AeroSaveProbe.parse(row));
      require(all.size() >= limit, "too few measured frames in " + path);
      return new Window(all);
    }

    Sample highestSave() {
      Sample best = null;
      for (Sample sample : samples)
        if (best == null || sample.saveUs > best.saveUs)
          best = sample;
      return best;
    }

    String classifyWorst() {
      Sample worst = null;
      for (Sample sample : samples)
        if (worst == null || sample.frameUs > worst.frameUs)
          worst = sample;
      long compile = Math.max(worst.compileUs, worst.compileMaxUs);
      long top = Math.max(worst.saveUs, Math.max(compile, worst.gcUs));
      int hits = 0;
      if (top > 0L && worst.saveUs == top)
        hits++;
      if (top > 0L && compile == top)
        hits++;
      if (top > 0L && worst.gcUs == top)
        hits++;
      if (hits != 1)
        return "MIXED";
      if (worst.saveUs == top)
        return "SAVE";
      return worst.gcUs == top ? "GC" : "COMPILE";
    }
  }
}
