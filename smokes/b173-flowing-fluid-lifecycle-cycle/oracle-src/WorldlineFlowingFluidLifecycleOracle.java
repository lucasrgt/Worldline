import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.testkit.FlowingFluidLifecycleEvidence;
import worldline.testkit.FlowingFluidLifecycleFixture;
import worldline.testkit.FlowingFluidLifecycleObservation;
import worldline.testkit.FlowingFluidObservation;
import worldline.trace.CanonicalTrace;

/** Executes the public moving-fluid lifecycle against the official obfuscated server JAR. */
public final class WorldlineFlowingFluidLifecycleOracle {
  private static final long SEED = 17320110707L;

  private WorldlineFlowingFluidLifecycleOracle() {
  }

  public static void main(String[] arguments) throws Exception {
    FlowingFluidObservation water = new OracleFlowingFluidNativeEngine(
        SEED, na.B.bn, na.C.bn, 1, 5, 30).execute(SEED);
    FlowingFluidObservation lava = new OracleFlowingFluidNativeEngine(
        SEED, na.D.bn, na.E.bn, 2, 30, -18).execute(SEED);
    FlowingFluidLifecycleObservation observation = new FlowingFluidLifecycleObservation(
        water, lava, ReloadBoundary.CHUNK_RELOAD);
    FlowingFluidLifecycleEvidence evidence = FlowingFluidLifecycleFixture.execute(
        () -> observation);
    byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest(evidence.canonical().getBytes(StandardCharsets.UTF_8));
    CanonicalTrace trace = new CanonicalTrace(SEED);
    trace.record("flowing-fluid-lifecycle", 0L, 0, integers(digest));
    trace.emitTo(System.out);
  }

  private static int[] integers(byte[] digest) {
    int[] values = new int[digest.length / 4];
    for (int index = 0; index < values.length; index++) {
      int offset = index * 4;
      values[index] = (digest[offset] & 255) << 24
          | (digest[offset + 1] & 255) << 16
          | (digest[offset + 2] & 255) << 8
          | digest[offset + 3] & 255;
    }
    return values;
  }
}
