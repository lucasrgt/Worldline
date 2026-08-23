package worldline.smoke.m5;

import java.nio.file.Files;
import java.nio.file.Paths;
import worldline.api.RuntimeSnapshot;
import worldline.b173.B173ReplayProvider;
import worldline.reproduction.ReproductionBundle;

/** Builds canonical M5 bundles in isolated pack processes. */
public final class M5BundleSmoke {
  private M5BundleSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length == 3, "expected mode, snapshot, and bundle paths");
    RuntimeSnapshot snapshot = RuntimeSnapshot.of(Files.readAllBytes(Paths.get(arguments[1])));
    ReproductionBundle bundle;
    if (arguments[0].equals("pack"))
      bundle = B173ReplayProvider.bundle(snapshot);
    else if (arguments[0].equals("wrong-runtime"))
      bundle = create(snapshot, "minecraft-b1.7.4-client", B173ReplayProvider.WORLDLINE_VERSION,
          B173ReplayProvider.CLIENT_SHA256, B173ReplayProvider.TOOLCHAIN_REVISION);
    else if (arguments[0].equals("wrong-worldline"))
      bundle = create(snapshot, B173ReplayProvider.RUNTIME_ID, "9.9.9",
          B173ReplayProvider.CLIENT_SHA256, B173ReplayProvider.TOOLCHAIN_REVISION);
    else if (arguments[0].equals("wrong-client"))
      bundle = create(snapshot, B173ReplayProvider.RUNTIME_ID, B173ReplayProvider.WORLDLINE_VERSION,
          repeat('0', 64), B173ReplayProvider.TOOLCHAIN_REVISION);
    else if (arguments[0].equals("wrong-toolchain"))
      bundle = create(snapshot, B173ReplayProvider.RUNTIME_ID, B173ReplayProvider.WORLDLINE_VERSION,
          B173ReplayProvider.CLIENT_SHA256, repeat('0', 40));
    else
      throw new IllegalArgumentException("unknown pack mode " + arguments[0]);
    Files.write(Paths.get(arguments[2]), bundle.bytes());
    System.out.println("WORLDLINE_M5_PACK=PASS");
    System.out.println("bundle.sha256=" + bundle.sha256());
    System.out.println("snapshot.sha256=" + bundle.snapshot().sha256());
  }

  private static ReproductionBundle create(
      RuntimeSnapshot snapshot, String runtime, String version, String client, String toolchain) {
    return ReproductionBundle.create(runtime, version, client, toolchain, snapshot);
  }
  private static String repeat(char value, int count) {
    StringBuilder result = new StringBuilder();
    while (result.length() < count)
      result.append(value);
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalArgumentException(message);
  }
}
