import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies the concrete Beta 1.7.3 EntityList physical-envelope matrix twice. */
public final class EntityPhysicalEnvelopeCycle {
  private static final String ID = "b173-entity-physical-envelope-cycle";
  private final Path root = Path.of("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/EntityPhysicalEnvelopeCycle.java " + ID);
      System.exit(2);
    }
    try { new EntityPhysicalEnvelopeCycle().execute(); }
    catch (Exception error) {
      System.err.println("entity physical envelope cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate(build);
    capture(build.resolve("first"));
    capture(build.resolve("second"));
    Path first = build.resolve("first/entity-physical-envelope.wlevidence");
    Path second = build.resolve("second/entity-physical-envelope.wlevidence");
    byte[] evidence = Files.readAllBytes(first);
    require(Arrays.equals(evidence, Files.readAllBytes(second)),
        "fresh entity physical envelope captures diverged");
    String canonical = new String(evidence, StandardCharsets.UTF_8);
    require(canonical.startsWith(
            "schema=worldline.entity-physical-envelope-evidence.v1\nclaims=23\n"),
        "entity physical envelope evidence framing drifted");
    require(occurrences(canonical, "#collision-shape|ARCHETYPE") == 13
            && occurrences(canonical, "#collision-shape|SINGULAR") == 10,
        "entity physical envelope layer routing drifted");
    require(occurrences(canonical, "|centered=true|vertical=true") == 23,
        "entity physical envelope geometry is incomplete");
    require(!canonical.contains("entity/048#collision-shape"),
        "abstract EntityLiving was materialized dishonestly");
    String evidenceSha = sha(evidence);
    String signal = "family=entity-physical-envelope,subjects=23,claims=23,"
        + "layers=ARCHETYPEx13+SINGULARx10,abstract=entity/048:NOT_APPLICABLE,"
        + "deterministic=true,evidence=" + evidenceSha;
    String trace = "v1|client=official-mapped-b1.7.3|family=entity-physical-envelope|"
        + "actions=construct-canonical-entities+normalize-slime+position+inspect-aabb+"
        + "inspect-contact-dispositions|oracle=public-entity-physical-envelope-evidence|"
        + "evidence=" + evidenceSha;
    String signature = sha(trace.getBytes(StandardCharsets.UTF_8));
    Properties descriptor = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
            root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      descriptor.load(reader);
    }
    String expected = descriptor.getProperty("expected.signature");
    if (!"pending".equals(expected)) {
      require(signal.equals(descriptor.getProperty("expected.signal")), "frozen signal drifted");
      require(trace.equals(descriptor.getProperty("expected.trace")), "frozen trace drifted");
      require(signature.equals(expected), "frozen signature drifted");
    }
    Files.writeString(build.resolve("evidence.txt"), canonical, StandardCharsets.UTF_8);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SET=" + signal);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_TRACE=" + trace);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SIGNATURE=" + signature);
    System.out.println("b173 entity physical envelope cycle "
        + ("pending".equals(expected) ? "diagnostic passed" : "passed"));
  }

  private void capture(Path output) throws Exception {
    recreate(output);
    Process process = new ProcessBuilder("java", "tools/replay/Replay.java", "census",
            output.toString()).directory(root.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(process.waitFor() == 0 && text.contains("WORLDLINE_CENSUS=PASS")
            && text.contains("entity-physical-envelope.sha256="),
        "entity physical envelope capture failed: " + summarize(text));
  }

  private static int occurrences(String value, String marker) {
    int count = 0, start = 0;
    while ((start = value.indexOf(marker, start)) >= 0) { count++; start += marker.length(); }
    return count;
  }

  private static String summarize(String value) {
    String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
    return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
  }

  private static void recreate(Path directory) throws Exception {
    if (Files.exists(directory)) {
      try (var paths = Files.walk(directory)) {
        for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
          Files.delete(path);
        }
      }
    }
    Files.createDirectories(directory);
  }

  private static String sha(byte[] value) throws Exception {
    StringBuilder result = new StringBuilder();
    for (byte item : MessageDigest.getInstance("SHA-256").digest(value)) {
      result.append(String.format("%02x", item & 255));
    }
    return result.toString();
  }

  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
