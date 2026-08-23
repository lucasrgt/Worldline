import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies durable differential testing across two versions of one mod. */
public final class VersionCycle {
  private static final String ID = "m8-mod-version-diff";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path smoke = root.resolve("smokes").resolve(ID);
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/VersionCycle.java " + ID);
      System.exit(2);
    }
    try {
      new VersionCycle().execute();
    } catch (Exception error) {
      System.err.println("M8 version cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Path adapter = client.resolve("adapter-classes");
    require(Files.isDirectory(adapter), "run ClientCycle first");
    recreate(build);
    Path modClasses =
        compile(smoke.resolve("mod-src"), build.resolve("mod-classes"), Arrays.asList(adapter));
    Path v1 = modJar("v1", "v1-a", modClasses), v1Copy = modJar("v1", "v1-b", modClasses);
    Path v2 = modJar("v2", "v2-a", modClasses), v2Copy = modJar("v2", "v2-b", modClasses);
    require(Arrays.equals(Files.readAllBytes(v1), Files.readAllBytes(v1Copy))
            && Arrays.equals(Files.readAllBytes(v2), Files.readAllBytes(v2Copy)),
        "deterministic mod packaging failed");
    Path scenario = compile(smoke.resolve("src"), build.resolve("classes"),
        Arrays.asList(adapter, product("api"), product("trace"), product("mods")));
    List<Path> game = gamePath(scenario, adapter);
    Run baseline = scenario(game, "baseline", "base-a.wltrace", null);
    Run baselineCopy = scenario(game, "baseline", "base-b.wltrace", null);
    Run one = scenario(game, "mod", "v1-a.wltrace", v1);
    Run oneCopy = scenario(game, "mod", "v1-b.wltrace", v1Copy);
    Run two = scenario(game, "mod", "v2-a.wltrace", v2);
    Run twoCopy = scenario(game, "mod", "v2-b.wltrace", v2Copy);
    same(baseline, baselineCopy, "baseline");
    same(one, oneCopy, "version 1");
    same(two, twoCopy, "version 2");
    require(trace(baseline).endsWith("|tick1=1,0|tick2=2,0|tick3=3,0"), "baseline state drifted");
    require(trace(one).endsWith("|tick1=1,20|tick2=2,20|tick3=3,20"), "version 1 state drifted");
    require(trace(two).endsWith("|tick1=1,0|tick2=2,41|tick3=3,41"), "version 2 state drifted");
    Path resultOne = build.resolve("v1-a.wlmtest"), resultOneCopy = build.resolve("v1-b.wlmtest");
    Path resultTwo = build.resolve("v2.wlmtest");
    Result recordOne =
        cli("mod", "test", "record", v1.toString(), one.trace.toString(), resultOne.toString());
    Result recordOneCopy = cli("mod", "test", "record", v1Copy.toString(), oneCopy.trace.toString(),
        resultOneCopy.toString());
    Result recordTwo =
        cli("mod", "test", "record", v2.toString(), two.trace.toString(), resultTwo.toString());
    require(recordOne.code == 0 && recordOne.text.equals(recordOneCopy.text) && recordTwo.code == 0
            && Arrays.equals(Files.readAllBytes(resultOne), Files.readAllBytes(resultOneCopy)),
        "mod test result recording is not deterministic");
    Result baseOne = cli("trace", "diff", baseline.trace.toString(), one.trace.toString());
    Result baseTwo = cli("trace", "diff", baseline.trace.toString(), two.trace.toString());
    requireDiff(baseOne, 1, "tick1", "0", "20");
    requireDiff(baseTwo, 2, "tick2", "0", "41");
    Result versionDiff = cli("mod", "test", "diff", resultOne.toString(), resultTwo.toString());
    require(versionDiff.code == 3
            && normalized(versionDiff.text).contains("same.mod=true\nsame.version=false")
            && normalized(versionDiff.text)
                .contains("record.index=1\nrecord.label=tick1\nfield.index=1\n"
                    + "field=block65\nleft=20\nright=0\n"),
        "version result diff was not exact");
    Result equal = cli("mod", "test", "diff", resultOne.toString(), resultOneCopy.toString());
    require(equal.code == 0 && equal.text.contains("WORLDLINE_MOD_TEST_DIFF=EQUAL"),
        "equal results diverged");
    byte[] corrupt = Files.readAllBytes(resultTwo);
    corrupt[corrupt.length - 3] ^= 1;
    Path corruptPath = build.resolve("corrupt.wlmtest");
    Files.write(corruptPath, corrupt);
    require(cli("mod", "test", "diff", resultOne.toString(), corruptPath.toString()).code == 1,
        "corrupt mod test result was accepted");
    String report = "baseline.trace=" + signature(baseline) + "\nv1.trace=" + signature(one)
        + "\nv2.trace=" + signature(two) + "\nv1.jar=" + sha256(v1) + "\nv2.jar=" + sha256(v2)
        + "\nv1.result=" + sha256(resultOne) + "\nv2.result=" + sha256(resultTwo)
        + "\nbaseline.v1.diff=" + sha256(normalized(baseOne.text))
        + "\nbaseline.v2.diff=" + sha256(normalized(baseTwo.text))
        + "\nversion.diff=" + sha256(normalized(versionDiff.text)) + "\ncorrupt=REJECTED\n";
    String evidence = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(evidence.equals(expected.getProperty("expected.signature")),
        "M8 evidence diverged: " + evidence);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M8 mod version differential cycle passed");
    System.out.println("  fresh runs: 2 baseline + 2 v1.0.0 + 2 v1.1.0");
    System.out.println("  first version divergence: tick1.block65, 20 -> 0");
    System.out.println("  evidence SHA-256: " + evidence);
  }

  private Run scenario(List<Path> game, String mode, String output, Path jar) throws Exception {
    Path trace = build.resolve(output);
    List<String> arguments = new ArrayList<>(Arrays.asList(mode, trace.toString()));
    if (jar != null)
      arguments.add(jar.toString());
    Result result =
        process(game, "worldline.smoke.m8.M8VersionSmoke", arguments.toArray(new String[0]));
    require(result.code == 0, "scenario failed\n" + result.text);
    return new Run(result.text, trace);
  }

  private void same(Run left, Run right, String role) throws IOException {
    require(left.output.equals(right.output)
            && Arrays.equals(Files.readAllBytes(left.trace), Files.readAllBytes(right.trace)),
        role + " runs diverged");
  }
  private String trace(Run run) throws IOException {
    return new String(Files.readAllBytes(run.trace), StandardCharsets.UTF_8);
  }
  private String signature(Run run) {
    return line(run.output, "trace.sha256=");
  }
  private void requireDiff(Result result, int record, String label, String left, String right) {
    String value = normalized(result.text);
    require(result.code == 3
            && value.contains("record.index=" + record + "\nrecord.label=" + label
                + "\nfield.index=1\nfield=block65\nleft=" + left + "\nright=" + right + "\n"),
        "baseline diff was not exact");
  }

  private Path modJar(String descriptorName, String outputName, Path classes) throws Exception {
    Path output = build.resolve(outputName + ".jar");
    Path descriptor = smoke.resolve("descriptors")
                          .resolve(descriptorName)
                          .resolve("META-INF/worldline-mod.properties");
    try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(output))) {
      entry(jar, "META-INF/worldline-mod.properties", Files.readAllBytes(descriptor));
      try (Stream<Path> paths = Files.walk(classes)) {
        for (Path path : paths.filter(Files::isRegularFile).sorted().collect(Collectors.toList())) {
          entry(jar, classes.relativize(path).toString().replace('\\', '/'),
              Files.readAllBytes(path));
        }
      }
    }
    return output;
  }
  private void entry(JarOutputStream jar, String name, byte[] bytes) throws IOException {
    CRC32 crc = new CRC32();
    crc.update(bytes);
    JarEntry entry = new JarEntry(name);
    entry.setTimeLocal(LocalDateTime.of(2000, 1, 1, 0, 0));
    entry.setMethod(JarEntry.STORED);
    entry.setSize(bytes.length);
    entry.setCompressedSize(bytes.length);
    entry.setCrc(crc.getValue());
    jar.putNextEntry(entry);
    jar.write(bytes);
    jar.closeEntry();
  }

  private Result cli(String... arguments) throws Exception {
    return process(Arrays.asList(product("cli"), product("reproduction"), product("api"),
                       product("invariants"), product("semantics"), product("trace"),
                       product("mods"), product("analysis"), product("modtest")),
        "worldline.cli.WorldlineCli", arguments);
  }
  private List<Path> gamePath(Path scenario, Path adapter) throws IOException {
    Path workspace = root.resolve("local/workspaces/b1.7.3");
    List<Path> result = new ArrayList<>(Arrays.asList(scenario,
        client.resolve("instrumented-client"), adapter, client.resolve("headless-classes"),
        product("api"), product("trace"), product("kernel"), product("mods"),
        workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar")));
    try (Stream<Path> paths = Files.walk(workspace.resolve("libraries"))) {
      result.addAll(paths.filter(path -> path.toString().endsWith(".jar"))
              .sorted()
              .collect(Collectors.toList()));
    }
    return result;
  }
  private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
    Files.createDirectories(output);
    List<String> command = new ArrayList<>(
        Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
            "-Werror", "-classpath", classpath(dependencies), "-d", output.toString()));
    try (Stream<Path> paths = Files.walk(source)) {
      paths.filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .forEach(path -> command.add(path.toString()));
    }
    Result result = command(command);
    require(result.code == 0, "compilation failed\n" + result.text);
    return output;
  }
  private Result process(List<Path> paths, String type, String... arguments) throws Exception {
    List<String> command = new ArrayList<>(
        Arrays.asList("java", "-Djava.awt.headless=true", "-classpath", classpath(paths), type));
    command.addAll(Arrays.asList(arguments));
    return command(command);
  }
  private Result command(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), text);
  }
  private String classpath(List<Path> paths) {
    return paths.stream()
        .map(Path::toString)
        .collect(Collectors.joining(System.getProperty("path.separator")));
  }
  private Path product(String name) {
    return root.resolve(".worldline/build/classes").resolve(name);
  }
  private String line(String text, String prefix) {
    return text.lines()
        .filter(row -> row.startsWith(prefix))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("missing " + prefix))
        .substring(prefix.length());
  }
  private String normalized(String text) {
    return text.replace("\r\n", "\n").replace('\r', '\n');
  }
  private String sha256(Path path) throws Exception {
    return sha256(Files.readAllBytes(path));
  }
  private String sha256(String text) throws Exception {
    return sha256(text.getBytes(StandardCharsets.UTF_8));
  }
  private String sha256(byte[] bytes) throws Exception {
    byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder result = new StringBuilder();
    for (byte item : digest)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private void recreate(Path target) throws IOException {
    if (Files.exists(target))
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    Files.createDirectories(target);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
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
  private static final class Run {
    final String output;
    final Path trace;
    Run(String output, Path trace) {
      this.output = output;
      this.trace = trace;
    }
  }
}
