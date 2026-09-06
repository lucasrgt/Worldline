import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import jdk.jfr.consumer.RecordingFile;

/** Separate allocation recordings never enter the performance qualification matrix. */
final class M794AllocationDiagnostic {
  private M794AllocationDiagnostic() {}

  static void run(Path root, Path smoke, Path build, Properties config) throws Exception {
    M794Artifact baseline = null;
    for (boolean current : new boolean[] {false, true}) {
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
      if (!current)
        runtime.runClient(template, true, "prepare");
      Path game = build.resolve(current ? "allocation-current-off" : "allocation-baseline-off");
      M794Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
      runtime.runClient(game, false, "allocation-off");
      M794Artifact artifact = M794Artifact.read(game, "replay-off");
      artifact.verify();
      if (baseline != null)
        SmokeSupport.require(M794VisualPair.compare(0, baseline, artifact).changedPixels() == 0,
            "M794 allocation diagnostic pixel mismatch");
      baseline = artifact;
      verifyRecording(game.resolve("allocations.jfr"));
      System.out.println("[M794] allocation recording complete: " + game.getFileName());
    }
  }

  private static void verifyRecording(Path file) throws Exception {
    SmokeSupport.require(Files.isRegularFile(file) && Files.size(file) > 0,
        "M794 allocation recording absent: " + file);
    boolean allocation = false;
    try (RecordingFile recording = new RecordingFile(file)) {
      while (recording.hasMoreEvents()) {
        var event = recording.readEvent();
        if (event.getEventType().getName().startsWith("jdk.ObjectAllocation")
            && event.getStackTrace() != null)
          allocation = true;
      }
    }
    SmokeSupport.require(allocation, "M794 allocation recording has no allocation stack traces");
  }
}
