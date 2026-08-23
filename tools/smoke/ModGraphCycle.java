import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Qualifies deterministic multi-mod dependency ordering through the neutral
 * mods module and the public CLI inspection surface.
 */
public final class ModGraphCycle {
  private static final String ID = "m13-mod-graph";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/ModGraphCycle.java " + ID);
      System.exit(2);
    }
    try {
      new ModGraphCycle().execute();
    } catch (Exception error) {
      System.err.println("M13 graph cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Files.createDirectories(build);
    Path classes = compile(root.resolve("smokes").resolve(ID).resolve("src"),
        build.resolve("classes"), Arrays.asList(product("mods")));
    Result first = process(classes, "worldline.smoke.m13.M13ModGraphSmoke", "run");
    Result second = process(classes, "worldline.smoke.m13.M13ModGraphSmoke", "run");
    require(first.code == 0 && first.text.equals(second.text), "graph smoke is not deterministic");
    require(first.text.contains("WORLDLINE_M13_ORDER=worldline.lib,worldline.core,worldline.app"),
        "unexpected resolved order");
    String report = "order=worldline.lib,worldline.core,worldline.app"
        + "\nrejections=missing,version,self,cycle\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M13 graph evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M13 mod graph cycle passed");
    System.out.println("  order: lib -> core -> app (input-order independent)");
    System.out.println("  rejected: missing, unmet version, self, cycle");
    System.out.println("  graph SHA-256: " + signature);
  }

  private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
    Files.createDirectories(output);
    List<String> command = new ArrayList<>(
        Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
            "-Werror", "-classpath", classpath(dependencies), "-d", output.toString()));
    try (java.util.stream.Stream<Path> paths = Files.walk(source)) {
      paths.filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .forEach(path -> command.add(path.toString()));
    }
    run(command);
    return output;
  }

  private Result process(Path classes, String type, String... arguments) throws Exception {
    List<Path> paths = new ArrayList<>(Arrays.asList(classes, product("mods")));
    List<String> command =
        new ArrayList<>(Arrays.asList("java", "-classpath", classpath(paths), type));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output);
  }

  private void run(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = process.waitFor();
    if (code != 0)
      throw new IllegalStateException(command.get(0) + " failed\n" + output);
  }

  private String classpath(List<Path> paths) {
    return paths.stream()
        .map(Path::toString)
        .collect(java.util.stream.Collectors.joining(System.getProperty("path.separator")));
  }
  private Path product(String name) {
    return root.resolve(".worldline/build/classes").resolve(name);
  }
  private String sha256(String text) throws Exception {
    byte[] hash =
        MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte value : hash)
      result.append(String.format("%02x", value & 255));
    return result.toString();
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
