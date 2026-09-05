import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/** Production matrix and default regression against an immutable previous renderer. */
public final class M794AeroProductionQualification {
  private static final String ID = "m794-aero-production-readiness";
  static final String TRACE =
      "v2|sdk=public|aero=currentc83afe6,baseline88adc8e|versions=B,C,C,B|orders=off-on,on-off|sessions=8|cases=cold,1,"
      + "20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm="
      + "5-views-no-dirty-exact8x15-no-models|captures=3-per-arm-case|pixels=exact|gain20-64=0.90|other=1.03|p99=1.05|"
      + "allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|surface=owned-rgba8-"
      + "depth24|vanilla-occlusion=off|spikelog=off|chunk-sort=coordinate-ties|textures=seed173-32ticks|jvm=cpu-gc-jit|"
        + "terrain=snapshot-after-timing";
  public void execute(String id) throws Exception {
    M794Performance.selfTest();
    M794Artifact.selfTest();
    SmokeSupport.require(ID.equals(id), "M794 identity drift");
    Path root = Path.of("").toAbsolutePath().normalize(), smoke = root.resolve("smokes/" + ID);
    Path build = root.resolve(".worldline/build/smoke/" + ID);
    Properties config = new Properties();
    SmokeSupport.load(smoke.resolve("smoke.properties"), config);
    SmokeSupport.require("8".equals(config.getProperty("sessions")) && "180".equals(config.getProperty("hold.frames"))
            && "0".equals(config.getProperty("maximum.changed.pixels")),
        "M794 design drift");
    SmokeSupport.require(
        M794Runtime.sha256(TRACE).equals(config.getProperty("expected.signature")), "M794 signature drift");
    SmokeSupport.recreate(root, build);
    List<M794Performance> profiles = new ArrayList<M794Performance>();
    List<M794Artifact> defaults = new ArrayList<M794Artifact>();
    StringBuilder evidence = new StringBuilder("id=" + ID + "\n");
    boolean visual = true, work = true;
    for (int group = 0; group < 4; group++) {
      boolean current = group == 1 || group == 2;
      Properties pin = new Properties();
      pin.putAll(config);
      if (!current)
        pin.setProperty("aero.revision", config.getProperty("baseline.revision"));
      M794Runtime runtime = new M794Runtime(root, smoke, build, pin,
          root.resolve(config.getProperty(current ? "aero.path" : "baseline.path")).normalize());
      runtime.verifyCheckout();
      runtime.verifyExtension();
      runtime.buildAero();
      Path template = build.resolve("template");
      if (group == 0)
        runtime.runClient(template, true, "prepare");
      for (int order = 0; order < 2; order++) {
        String name = order == 0 ? "off-on" : "on-off";
        Path game = build.resolve("group" + group + "-" + (current ? "current-" : "baseline-") + name);
        M794Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
        runtime.runClient(game, false, name);
        M794Artifact off = M794Artifact.read(game, "replay-off"), on = M794Artifact.read(game, "replay-on");
        off.verify();
        on.verify();
        defaults.add(off);
        M794VisualPair pair = M794VisualPair.compare(group * 2 + order, off, on);
        visual &= pair.changedPixels() == 0;
        work &= off.culled == 0 && on.culled == 0;
        evidence.append(pair.summary()).append('\n');
        profiles.add(M794Performance.read(game, current, order == 0));
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        SmokeSupport.require(visual, "M794 native oracle failed; preserve terrain snapshots before continuing");
      }
    }
    for (int i = 2; i < 8; i++) {
      M794VisualPair pair = M794VisualPair.compare(8 + i, defaults.get(i & 1), defaults.get(i));
      visual &= pair.changedPixels() == 0;
      evidence.append("baseline.").append(pair.summary()).append('\n');
    }
    boolean performance = M794Performance.evaluate(profiles, evidence);
    M794Runtime diagnostic =
        new M794Runtime(root, smoke, build, config, root.resolve(config.getProperty("aero.path")).normalize());
    diagnostic.verifyCheckout();
    diagnostic.buildAero();
    Path game = build.resolve("diagnostic-off-on");
    M794Runtime.copyWorld(build.resolve("template/saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
    diagnostic.runClient(game, false, "diagnostic-off-on");
    evidence.append("visual.exact=")
        .append(visual)
        .append("\nno.culls=")
        .append(work)
        .append("\nperformance.passes=")
        .append(performance)
        .append("\ntrace=")
        .append(TRACE)
        .append('\n');
    Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
    SmokeSupport.require(
        visual && work && performance, "M794 production qualification rejected; see " + build.resolve("evidence.txt"));
    System.out.println("WORLDLINE_M794_SIGNAL=" + config.getProperty("expected.signal"));
    System.out.println("WORLDLINE_M794_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M794_SIGNATURE=" + config.getProperty("expected.signature"));
  }
}
