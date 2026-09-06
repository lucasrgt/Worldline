import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import jdk.jfr.consumer.RecordingFile;

/** Separate allocation recordings never enter the performance qualification matrix. */
final class M794AllocationDiagnostic {
  private M794AllocationDiagnostic() {}

  static void run(Path root, Path smoke, Path build, Properties config) throws Exception {
    M794Artifact baseline = null;
    Properties baselineDispatch = null;
    for (int step = 0; step < 6; step++) {
      int mode = step < 3 ? step : 5 - step;
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
      if (step == 0)
        runtime.runClient(template, true, "prepare");
      Path game = build.resolve("cpu-group" + step / 3 + "-"
          + (current ? "current-" : "baseline-") + (candidate ? "on" : "off"));
      M794Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
      runtime.runClient(game, false, candidate ? "allocation-on" : "allocation-off");
      M794Artifact artifact = M794Artifact.read(game, candidate ? "replay-on" : "replay-off");
      artifact.verify();
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
      verifyRecording(game.resolve("allocations.jfr"));
      SmokeSupport.require(Files.size(game.resolve("compilation.xml")) > 0,
          "M794 JIT compilation log absent");
      System.out.println("[M794] allocation recording complete: " + game.getFileName());
    }
  }

  private static void verifyRecording(Path file) throws Exception {
    SmokeSupport.require(Files.isRegularFile(file) && Files.size(file) > 0,
        "M794 allocation recording absent: " + file);
    boolean allocation = false, execution = false, compilation = false;
    try (RecordingFile recording = new RecordingFile(file)) {
      while (recording.hasMoreEvents()) {
        var event = recording.readEvent();
        execution |= event.getEventType().getName().equals("jdk.ExecutionSample");
        compilation |= event.getEventType().getName().equals("jdk.Compilation");
        if (event.getEventType().getName().startsWith("jdk.ObjectAllocation")
            && event.getStackTrace() != null)
          allocation = true;
      }
    }
    SmokeSupport.require(allocation && execution && compilation,
        "M794 recording lacks allocation stacks, CPU samples or compilation events");
  }
}
