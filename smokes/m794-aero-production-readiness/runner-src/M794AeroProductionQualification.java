import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/** Production matrix and default regression against an immutable previous renderer. */
public final class M794AeroProductionQualification {
  private static final String ID = "m794-aero-production-readiness";
  static final String TRACE = "v12-gpu-quiescence-acquisition|sdk=public|aero=current41dc5a8,baseline88adc8e|triplets=Boff-Coff-Con,Con-Coff-Boff,"
      + "Con-Coff-Boff,Boff-Coff-Con|sessions=12|cases=cold,1,"
      + "20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm="
      + "5-views-plus-each-case-all-chunks-exact8x15-no-models|model-warm=300-fixed-view-except-cold|captures=3-per-"
      + "case|pixels=exact|saturation128-256=fallback|gain20-64=0.90|"
      + "other=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|"
      + "surface=owned-rgba8-depth24|vanilla-occlusion=off|spikelog=off|chunk-sort=arm-reset|textures=arsenic-seed173-"
      + "32ticks|jvm=fixed-production-mode-cpu-gc-jit|terrain=snapshot-after-timing|window=post-capture-preview"
      + "|host=windows-high-four-physical-vm4|fixture=direct-renderer-primitive-light"
      + "|allocation-diagnostic=preflight-baseline-off-current-off-jfr-separate"
      + "|chunk-dispatch=registered-pixels-vanilla-null-success-failure-returns|mixinextras=0.5.0-both-arms|cpu-acquisition=6-counterbalanced-jfr-jit-logs|gpu-entry=finish-separate-drain-and-inclusive|clock=instant-midpoint-minspan16|production-qualification=not-attempted";
  public void execute(String id) throws Exception {
    M794Performance.selfTest();
    M794Artifact.selfTest();
    SmokeSupport.require(ID.equals(id), "M794 identity drift");
    Path root = Path.of("").toAbsolutePath().normalize(), smoke = root.resolve("smokes/" + ID);
    Path build = root.resolve(".worldline/build/smoke/" + ID);
    Properties config = new Properties();
    SmokeSupport.load(smoke.resolve("smoke.properties"), config);
    SmokeSupport.require("12".equals(config.getProperty("sessions"))
            && "180".equals(config.getProperty("hold.frames"))
            && "0".equals(config.getProperty("maximum.changed.pixels")),
        "M794 design drift");
    SmokeSupport.require(
        M794Runtime.sha256(TRACE).equals(config.getProperty("expected.signature")), "M794 signature drift");
    SmokeSupport.recreate(root, build);
    M794AllocationDiagnostic.run(root, smoke, build, config);
    if (Boolean.parseBoolean(config.getProperty("diagnostic.acquisition.only", "false"))) {
      Files.writeString(build.resolve("diagnostic-disposition.txt"),
          "Six GPU-quiescence CPU/JIT acquisitions completed. Production qualification was not attempted.\n");
      throw new IllegalStateException("M794 GPU-quiescence acquisition complete; production qualification not attempted");
    }
    List<M794Performance> profiles = new ArrayList<M794Performance>();
    List<M794Artifact> defaults = new ArrayList<M794Artifact>();
    StringBuilder evidence = new StringBuilder("id=" + ID + "\n");
    boolean visual = true, work = true;
    for (int group = 0; group < 4; group++) {
      boolean offFirst = group == 0 || group == 3;
      for (int position = 0; position < 3; position++) {
        int mode = offFirst ? position : 2 - position;
        boolean current = mode != 0, candidate = mode == 2;
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
        String arm = candidate ? "replay-on" : "replay-off";
        Path game = build.resolve("group" + group + "-" + (current ? "current-" : "baseline-") + arm);
        M794Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
        runtime.runClient(game, false, arm);
        M794Artifact artifact = M794Artifact.read(game, arm);
        artifact.verify();
        if (!defaults.isEmpty()) {
          M794VisualPair pair = M794VisualPair.compare(group * 3 + position, defaults.get(0), artifact);
          visual &= pair.changedPixels() == 0;
          evidence.append(pair.summary()).append('\n');
        }
        defaults.add(artifact);
        work &= artifact.culled == 0;
        profiles.add(M794Performance.read(game, current, candidate, offFirst));
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        SmokeSupport.require(visual, "M794 native oracle failed; preserve terrain snapshots before continuing");
      }
    }
    boolean performance = M794Performance.evaluate(profiles, evidence);
    M794Runtime diagnostic =
        new M794Runtime(root, smoke, build, config, root.resolve(config.getProperty("aero.path")).normalize());
    diagnostic.verifyCheckout();
    diagnostic.buildAero();
    Path game = build.resolve("diagnostic-on");
    M794Runtime.copyWorld(build.resolve("template/saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
    diagnostic.runClient(game, false, "diagnostic-on");
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
