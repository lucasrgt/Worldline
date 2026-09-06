import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/** Native source-consumer qualification for the unchanged legacy renderer platform. */
public final class M795AeroLegacyConsumer {
  private static final String ID = "m795-aero-legacy-consumer";
  static final String TRACE = "v1|aero=9abdcc5|sdk=public|native=modloader+forge1.0.6|sessions=2-per-loader|java=8"
      + "|source=complete-core-and-adapter|surface=rgba8-depth24-128x128|draw=direct-list-cached-rebuilt"
      + "|animation=zero-advanced-nbt-restored|pixels=exact-cross-session-cross-loader|dispose=gl-lists-released"
      + "|profiler=disabled|performance=unclaimed";
  public void execute(String id) throws Exception {
    SmokeSupport.require(ID.equals(id), "M795 identity drift");
    Path root = Path.of("").toAbsolutePath().normalize();
    Path smoke = root.resolve("smokes/" + ID), build = root.resolve(".worldline/build/smoke/" + ID);
    Properties config = new Properties();
    SmokeSupport.load(smoke.resolve("smoke.properties"), config);
    String signature = digest(TRACE.getBytes(StandardCharsets.UTF_8));
    SmokeSupport.require(signature.equals(config.getProperty("expected.signature")), "M795 signature drift");
    SmokeSupport.recreate(root, build);
    M795Inputs inputs = new M795Inputs(root, smoke, build, config);
    inputs.prepare();
    byte[][] reference = null;
    StringBuilder evidence = new StringBuilder("id=" + ID + "\n");
    for (String loader : new String[] {"modloader", "forge"}) {
      M795Client client = new M795Client(inputs, inputs.compile(loader), loader);
      for (int session = 0; session < 2; session++) {
        Path result = client.run(session);
        byte[][] captures = new byte[7][];
        for (int i = 0; i < captures.length; i++) {
          captures[i] = Files.readAllBytes(result.resolve("capture-" + i + ".rgba"));
          SmokeSupport.require(captures[i].length == 128 * 128 * 4, "M795 incomplete native framebuffer");
          if (reference != null)
            SmokeSupport.require(Arrays.equals(reference[i], captures[i]), "M795 cross-session native pixels differ");
          evidence.append(loader)
              .append('.')
              .append(session)
              .append('.')
              .append(i)
              .append('=')
              .append(digest(captures[i]))
              .append('\n');
        }
        if (reference == null)
          reference = captures;
        Files.writeString(build.resolve("evidence.txt"), evidence);
      }
    }
    evidence.append("trace=").append(TRACE).append("\nsignature=").append(signature).append('\n');
    Files.writeString(build.resolve("evidence.txt"), evidence);
    System.out.println("WORLDLINE_M795_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
    System.out.println("WORLDLINE_M795_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M795_SIGNATURE=" + signature);
  }
  private static String digest(byte[] value) throws Exception {
    return HexDigest.sha256Hex(value);
  }
}
