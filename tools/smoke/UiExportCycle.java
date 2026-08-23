import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies the semantic UI tree export as a self-contained page. */
public final class UiExportCycle {
  private static final String ID = "ui-export";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/UiExportCycle.java " + ID);
      System.exit(2);
    }
    try {
      new UiExportCycle().execute();
    } catch (Exception error) {
      System.err.println("ui export cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate();
    Result first = launcher("ui", build.resolve("first.html").toString());
    require(first.code == 0 && first.text.contains("WORLDLINE_UI_EXPORT=PASS")
            && first.text.contains("page.sha256="),
        "first ui export failed");
    Result second = launcher("ui", build.resolve("second.html").toString());
    require(second.code == 0
            && Arrays.equals(Files.readAllBytes(build.resolve("first.html")),
                Files.readAllBytes(build.resolve("second.html"))),
        "ui export is not byte-deterministic");
    String page =
        new String(Files.readAllBytes(build.resolve("first.html")), StandardCharsets.UTF_8);
    for (String marker : new String[] {"Worldline UI Export", ">slot<", "screen=inventory"}) {
      require(page.contains(marker), "page missing marker: " + marker);
    }
    int slots = 0;
    for (String row : page.split("\n", -1)) {
      if (row.contains(">slot<"))
        slots++;
    }
    require(slots >= 40, "implausible slot count: " + slots);
    require(!page.contains("<script"), "ui page must not embed scripts");
    String digest = lineOf(first.text, "page.sha256=");
    String report = "seed=17320110707\nslots=" + slots + "\ndeterministic=true"
        + "\nscripts=none\npage.sha256=" + digest + "\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "ui export evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("UI export cycle passed");
    System.out.println("  inventory tree exported; slots=" + slots + "; byte-deterministic");
    System.out.println("  page.sha256=" + digest);
    System.out.println("  evidence SHA-256: " + signature);
  }

  private Result launcher(String... arguments) throws Exception {
    List<String> command =
        new java.util.ArrayList<>(Arrays.asList("java", "tools/replay/Replay.java"));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output.replace('\r', '\n'));
  }

  private String lineOf(String text, String prefix) {
    for (String row : text.split("\n", -1)) {
      if (row.startsWith(prefix))
        return row.substring(prefix.length());
    }
    throw new IllegalStateException("missing " + prefix);
  }

  private void recreate() throws Exception {
    if (Files.exists(build)) {
      try (java.util.stream.Stream<Path> paths = Files.walk(build)) {
        for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                 .collect(java.util.stream.Collectors.toList()))
          Files.delete(path);
      }
    }
    Files.createDirectories(build);
  }

  private String sha256File(Path path) throws Exception {
    return sha256(Files.readAllBytes(path));
  }

  private String sha256(String text) throws Exception {
    return sha256(text.getBytes(StandardCharsets.UTF_8));
  }
  private String sha256(byte[] data) throws Exception {
    MessageDigest d = MessageDigest.getInstance("SHA-256");
    StringBuilder r = new StringBuilder();
    for (byte b : d.digest(data))
      r.append(String.format("%02x", b));
    return r.toString();
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
  private static final class Result {
    final int code;
    final String text;
    Result(int code, String text) {
      this.code = code;
      this.text = text;
    }
  }
}
