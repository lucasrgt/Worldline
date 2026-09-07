import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Separate assembly acquisitions never enter the performance qualification matrix. */
final class M794AllocationDiagnostic {
  private M794AllocationDiagnostic() {}

  static void run(Path root, Path smoke, Path build, Properties config) throws Exception {
    M794Artifact baseline = null;
    Properties baselineDispatch = null;
    for (int step = 0; step < 6; step++) {
      M794Runtime runtime = new M794Runtime(root, smoke, build, config,
          root.resolve(config.getProperty("aero.path")).normalize());
      runtime.verifyCheckout();
      runtime.verifyExtension();
      runtime.buildAero();
      Path template = build.resolve("template");
      if (step == 0)
        runtime.runClient(template, true, "prepare");
      Path game = build.resolve("assembly-nojfr-repeat" + step + "-current-off");
      M794Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
      runtime.runClient(game, false, "allocation-off");
      M794Artifact artifact = M794Artifact.read(game, "replay-off");
      artifact.verify();
      for (int scene = 0; scene < 12; scene++) {
        Properties profile = new Properties();
        SmokeSupport.load(game.resolve("profile-" + artifact.arm + "-" + scene + ".properties"), profile);
        long drain = Long.parseLong(profile.getProperty("gpudrain.total"));
        long render = Long.parseLong(profile.getProperty("render.total"));
        long inclusive = Long.parseLong(profile.getProperty("renderwithdrain.total"));
        SmokeSupport.require(drain == 0L && inclusive == render, "M794 unexpected GPU drain");
      }
      if (baseline != null)
        SmokeSupport.require(M794VisualPair.compare(0, baseline, artifact).changedPixels() == 0,
            "M794 allocation diagnostic pixel mismatch");
      if (baseline == null) baseline = artifact;
      Properties dispatch = new Properties();
      SmokeSupport.load(game.resolve("dispatch.properties"), dispatch);
      SmokeSupport.require("return:true".equals(dispatch.getProperty("registered"))
              && "return:false".equals(dispatch.getProperty("failed"))
              && dispatch.getProperty("vanilla") != null && dispatch.getProperty("null") != null,
          "M794 native chunk dispatch observations missing");
      SmokeSupport.require(baselineDispatch == null || baselineDispatch.equals(dispatch),
          "M794 native chunk dispatch return or exception mismatch");
      baselineDispatch = dispatch;
      SmokeSupport.require(!Files.exists(game.resolve("allocations.jfr")),
          "M794 unexpected JFR recording in assembly-only acquisition");
      SmokeSupport.require(Files.size(game.resolve("compilation.xml")) > 0,
          "M794 JIT compilation log absent");
      String assembly = Files.readString(game.resolve("runtime-console.log"));
      SmokeSupport.require(!assembly.contains("Started recording")
              && !assembly.contains("StartFlightRecording")
              && assembly.contains("Compiled method (c2)") && assembly.contains("[MachCode]")
              && assembly.contains("Aero_TessellatorBulkWriter::write")
              && assembly.contains("Aero_MeshBatchDraw::drawBucket"),
          "M794 selected Aero compilation output absent");
      System.out.println("[M794] assembly recording complete: " + game.getFileName());
    }
  }

}
