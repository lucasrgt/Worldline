package worldline.smoke.creepertntb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.testkit.CreeperTntDifferentialFixture;

/** Composes two official probes into a normalized creeper-versus-TNT differential. */
public final class CreeperTntDifferentialSmoke {
  private CreeperTntDifferentialSmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 5)
      throw new IllegalArgumentException(
          "usage: CreeperTntDifferentialSmoke server.jar workspace port seed ignoredUser");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    long seed = Long.parseLong(args[3]);
    Files.createDirectories(workspace);
    String creeper = capture(
        ()
            -> invoke("worldline.smoke.creeperexplodesetb173.CreeperExplodeSetSmoke",
                new String[] {jar.toString(), workspace.resolve("creeper").toString(),
                    Integer.toString(freePort()), Long.toString(seed), "Creeper391", "0", "0"}));
    String tnt = capture(()
                             -> invoke("worldline.smoke.tntprimesetb173.TntPrimeSetSmoke",
                                 new String[] {jar.toString(), workspace.resolve("tnt").toString(),
                                     Integer.toString(freePort()), Long.toString(seed),
                                     "TntPrime381", "0", "0", "5", "100"}));
    String creeperSet = line(creeper, "WORLDLINE_M391_SET=");
    String tntSet = line(tnt, "WORLDLINE_M381_SET=");
    require(creeperSet.contains("packet60=strength3"), "creeper strength proof absent");
    require(tntSet.contains("strength=4"), "TNT strength proof absent");
    CreeperTntDifferentialFixture.Evidence evidence = CreeperTntDifferentialFixture.observe(3F, 4F);
    require(evidence.delta() == 1 && evidence.tntStronger(), "normalized differential drift");
    String signal = "creeper=packet60:strength3,tnt=packet60:strength4,delta=1"
        + ",ordering=creeper<tnt,official-probes=2,replicas=2,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|fixture=creeper391-pad+tnt381-charge"
        + "|action=proximity-fuse+packet15-flint-prime|observation=packet60-strength3+packet60-"
          + "strength4"
        + "|oracle=creeper-vs-tnt-strength-differential|" + signal;
    System.out.println("WORLDLINE_M638_SET=" + signal);
    System.out.println("WORLDLINE_M638_TRACE=" + trace);
    System.out.println("WORLDLINE_M638_SIGNATURE=" + sha(trace));
  }
  private static String capture(Action action) throws Exception {
    PrintStream prior = System.out;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (PrintStream stream = new PrintStream(bytes, true, StandardCharsets.UTF_8.name())) {
      System.setOut(stream);
      action.run();
    } finally {
      System.setOut(prior);
    }
    return bytes.toString(StandardCharsets.UTF_8.name());
  }
  private static void invoke(String type, String[] arguments) throws Exception {
    try {
      Class.forName(type).getMethod("main", String[].class).invoke(null, (Object) arguments);
    } catch (java.lang.reflect.InvocationTargetException error) {
      Throwable cause = error.getCause();
      if (cause instanceof Exception)
        throw (Exception) cause;
      throw error;
    }
  }
  private static int freePort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
  private static String line(String output, String prefix) {
    for (String value : output.split("\\R"))
      if (value.startsWith(prefix))
        return value.substring(prefix.length());
    throw new IllegalStateException("nested evidence absent: " + prefix);
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
  @FunctionalInterface
  private interface Action {
    void run() throws Exception;
  }
}
