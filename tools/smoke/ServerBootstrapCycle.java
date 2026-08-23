import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Boots and cleanly stops two fresh official b1.7.3 dedicated servers. */
public final class ServerBootstrapCycle {
  private static final String ID = "m20-server-bootstrap";
  private static final String TRACE =
      "v1|version=Beta 1.7.3|startup=done|online=false|shutdown=clean";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Properties smoke = new Properties();
  private final Properties artifact = new Properties();

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/ServerBootstrapCycle.java " + ID);
      System.exit(2);
    }
    try {
      new ServerBootstrapCycle().execute();
    } catch (Exception error) {
      System.err.println("server bootstrap cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    load(root.resolve("smokes").resolve(ID).resolve("smoke.properties"), smoke);
    load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
    require(ID.equals(value(smoke, "id")), "smoke descriptor id mismatch");
    Path official = localArtifact();
    verifyArtifact(official);
    Path build = root.resolve(".worldline/smokes").resolve(ID).normalize();
    recreate(build);
    String first = runServer(build.resolve("first"), official);
    String second = runServer(build.resolve("second"), official);
    require(first.equals(second) && first.equals(TRACE), "normalized server lifecycle drift");
    String signature = digest(TRACE.getBytes(StandardCharsets.UTF_8));
    require(
        signature.equals(value(smoke, "expected.signature")), "M20 signature drift: " + signature);
    String evidence = "id=" + ID + "\nserver.sha256=" + value(artifact, "expected.sha256")
        + "\nprocesses=2\ntrace=" + TRACE + "\nsignature=" + signature + "\n";
    Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
    System.out.println("M20 official server bootstrap passed");
    System.out.println("  processes: 2 fresh dedicated servers");
    System.out.println("  lifecycle: localhost boot -> Done -> stop -> clean save/exit");
    System.out.println("  signature: " + signature);
    System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
  }

  private String runServer(Path directory, Path official) throws Exception {
    Files.createDirectories(directory);
    Files.copy(official, directory.resolve("server.jar"), StandardCopyOption.REPLACE_EXISTING);
    int port = freePort();
    String properties = "allow-nether=false\nlevel-name=world\nlevel-seed=" + value(smoke, "seed")
        + "\nmax-players=4\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=" + port
        + "\nspawn-animals=false\nspawn-monsters=false\nview-distance=3\n";
    Files.write(
        directory.resolve("server.properties"), properties.getBytes(StandardCharsets.UTF_8));
    Process process = new ProcessBuilder(javaCommand(), "-Djava.awt.headless=true", "-Xms64m",
        "-Xmx256m", "-jar", "server.jar", "nogui")
                          .directory(directory.toFile())
                          .redirectErrorStream(true)
                          .start();
    List<String> output = Collections.synchronizedList(new ArrayList<>());
    Thread reader = reader(process, output);
    Duration timeout = Duration.ofSeconds(Long.parseLong(value(smoke, "timeout.seconds")));
    try {
      await(output, "Done (", timeout, process);
      try (Writer writer =
               new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
        writer.write("stop\n");
        writer.flush();
      }
      require(process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS),
          "server did not stop in time");
      reader.join(5000);
      require(
          process.exitValue() == 0, "server exited " + process.exitValue() + "\n" + tail(output));
      require(contains(output, "Starting minecraft server version Beta 1.7.3"),
          "official version marker absent\n" + tail(output));
      require(contains(output, "Saving chunks"), "save marker absent\n" + tail(output));
      require(contains(output, "Stopping server"), "clean stop marker absent\n" + tail(output));
      return TRACE;
    } finally {
      if (process.isAlive())
        process.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
    }
  }

  private Thread reader(Process process, List<String> output) {
    Thread result = new Thread(() -> {
      try (BufferedReader lines = new BufferedReader(
               new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = lines.readLine()) != null)
          output.add(line);
      } catch (IOException error) {
        output.add("reader-error: " + error.getMessage());
      }
    }, "worldline-m20-server-output");
    result.setDaemon(true);
    result.start();
    return result;
  }

  private void await(List<String> output, String marker, Duration timeout, Process process)
      throws Exception {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      if (contains(output, marker))
        return;
      if (!process.isAlive())
        throw new IllegalStateException("server exited before startup\n" + tail(output));
      Thread.sleep(100);
    }
    throw new IllegalStateException("server startup timed out\n" + tail(output));
  }

  private boolean contains(List<String> output, String value) {
    synchronized (output) {
      return output.stream().anyMatch(line -> line.contains(value));
    }
  }

  private String tail(List<String> output) {
    synchronized (output) {
      int start = Math.max(0, output.size() - 30);
      return output.subList(start, output.size()).stream().collect(Collectors.joining("\n"));
    }
  }

  private int freePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private Path localArtifact() {
    Path local = root.resolve("local").normalize();
    Path result = root.resolve(value(artifact, "local.path")).normalize();
    require(
        result.startsWith(local) && !result.equals(local), "server artifact must be inside local/");
    require(Files.isRegularFile(result), "server artifact absent; run Acquire.java server");
    return result;
  }

  private void verifyArtifact(Path path) throws Exception {
    require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")),
        "server size mismatch");
    require(fileDigest(path, "SHA-1").equals(value(artifact, "expected.sha1")),
        "server SHA-1 mismatch");
    require(fileDigest(path, "SHA-256").equals(value(artifact, "expected.sha256")),
        "server SHA-256 mismatch");
    require(value(smoke, "server.jar.sha256").equals(value(artifact, "expected.sha256")),
        "smoke server descriptor drift");
  }

  private void recreate(Path path) throws IOException {
    if (Files.exists(path)) {
      require(
          path.startsWith(root.resolve(".worldline")) && !path.equals(root), "unsafe build path");
      try (Stream<Path> paths = Files.walk(path)) {
        for (Path item : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(item);
      }
    }
    Files.createDirectories(path);
  }

  private String javaCommand() {
    boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    return Paths.get(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java")
        .toString();
  }

  private void load(Path path, Properties target) throws IOException {
    try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      target.load(reader);
    }
  }

  private String value(Properties source, String key) {
    String result = source.getProperty(key);
    require(result != null && !result.trim().isEmpty(), "missing property " + key);
    return result.trim();
  }

  private String fileDigest(Path path, String algorithm) throws Exception {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    try (InputStream input = Files.newInputStream(path)) {
      byte[] buffer = new byte[8192];
      int count;
      while ((count = input.read(buffer)) >= 0)
        digest.update(buffer, 0, count);
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private String digest(byte[] value) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
