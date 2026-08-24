import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Qualifies a BetaVault/BetaEnergistics reference through real ItemStack runtime boundaries. */
public final class BetaVaultItemReferenceCycle {
  private static final String ID = "betavault-item-reference";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path smoke = root.resolve("smokes").resolve(ID);
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Properties config = new Properties();

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/BetaVaultItemReferenceCycle.java " + ID);
      System.exit(2);
    }
    try {
      new BetaVaultItemReferenceCycle().execute();
    } catch (Exception error) {
      System.err.println("BetaVault item-reference cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    load();
    Path aero = external("aero", "aero.path", "aero.repository", "aero.revision");
    Path vault = external("BetaVault", "betavault.path", null, "betavault.revision");
    Path energy =
        external("BetaEnergistics", "betaenergistics.path", null, "betaenergistics.revision");
    Files.createDirectories(build);
    recreate(build.resolve("cycle"));
    buildSupport(energy);
    Phase create = phase(aero, build.resolve("cycle"), "create");
    Phase reload = phase(aero, build.resolve("cycle"), "reload");
    require(create.reference.equals(reload.reference), "reference changed across restart");
    verifyClean(aero, value("aero.revision"));
    verifyClean(vault, value("betavault.revision"));
    verifyClean(energy, value("betaenergistics.revision"));
    String trace = "v1|runtime=controlled-stationapi|host=real-betaenergistics"
        + "|store=real-betavault|identity=itemstack-nbt+copy+split+equality"
        + "|wire=packet104|restart=same-reference+iron100|vanilla-default=unchanged"
        + "|not-claimed=packet5+packet21";
    String evidence = "id=" + ID + "\nworldline.revision=" + git(root, "rev-parse", "HEAD").trim()
        + "\nbetavault.revision=" + value("betavault.revision") + "\nbetaenergistics.revision="
        + value("betaenergistics.revision") + "\naero.revision=" + value("aero.revision")
        + "\nreference=" + create.reference + "\ncreate=" + create.server
        + "\nreload=" + reload.server + "\ntrace=" + trace + "\nsignature=" + sha256(trace) + "\n";
    Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
    System.out.println("BetaVault item-reference extension passed");
    System.out.println("  reference: " + create.reference);
    System.out.println("  signature: " + sha256(trace));
    System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
  }

  private Phase phase(Path aero, Path workspace, String phase) throws Exception {
    Files.createDirectories(workspace);
    int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
    Path trees =
        root.resolve(".worldline/worktrees/itemref-" + phase + "-" + ProcessHandle.current().pid());
    Path serverTree = trees.resolve("server"), clientTree = trees.resolve("client");
    Captured server = null;
    try {
      addWorktree(aero, serverTree);
      addWorktree(aero, clientTree);
      Path serverGame = workspace.resolve("server"),
           clientGame = workspace.resolve("client-" + phase);
      server = Captured.start(serverTree.resolve("stationapi/test-bare"),
          command(serverTree, serverGame, "server", phase, port), build.resolve("gradle-server"));
      server.awaitText("Done (", timeout);
      String client = Captured.run(clientTree.resolve("stationapi/test-bare"),
          command(clientTree, clientGame, "client", phase, port), timeout,
          build.resolve("gradle-server"));
      server.write("save-all\nstop\n");
      server.finish(60);
      String serverText = server.output();
      server = null;
      require(client.contains("WORLDLINE_ITEMREF_CLIENT=PASS "),
          "client reference absent in " + phase + "\nclient:\n" + diagnostic(client) + "\nserver:\n"
              + diagnostic(serverText));
      String clientRow = line(client, "WORLDLINE_ITEMREF_CLIENT=PASS ");
      String prefix = "WORLDLINE_ITEMREF_" + phase.toUpperCase(Locale.ROOT) + "=PASS ";
      String serverRow = line(serverText, prefix);
      String serverRef = field(serverRow, "ref"), clientRef = field(clientRow, "ref");
      require(serverRef.equals(clientRef), "server/client reference drift in " + phase);
      require(client.contains("packet104=preserved") && client.contains("BUILD SUCCESSFUL"),
          "client lifecycle absent in " + phase + "\n" + diagnostic(client));
      require(serverText.contains(value("username") + " lost connection")
              && serverText.contains("Stopping server") && serverText.contains("BUILD SUCCESSFUL"),
          "server lifecycle absent in " + phase + "\n" + diagnostic(serverText));
      return new Phase(serverRef, serverRow);
    } finally {
      if (server != null && server.process.isAlive()) {
        try {
          server.write("stop\n");
          server.finish(20);
        } catch (Exception error) {
          server.kill();
        }
      }
      removeWorktree(aero, clientTree);
      removeWorktree(aero, serverTree);
    }
  }

  private List<String> command(Path tree, Path game, String role, String phase, int port) {
    Path project = tree.resolve("stationapi/test-bare");
    return new ArrayList<String>(Arrays.asList(project.resolve(wrapper()).toString(), "--no-daemon",
        "--init-script", smoke.resolve("item-reference.init.gradle").toString(),
        role.equals("server") ? "runServer" : "runClient", "-PworldlineItemRefRole=" + role,
        "-PworldlineItemRefRunDir=" + game, "-PworldlineItemRefPort=" + port,
        "-PworldlineItemRefPhase=" + phase, "-PworldlineItemRefSeed=" + value("world.seed"),
        "-PworldlineItemRefUsername=" + value("username")));
  }

  private void buildSupport(Path energy) throws Exception {
    String verified = Captured.run(
        energy, Arrays.asList("java", "tools/harness/BetaVaultIntegrationCheck.java"), 120);
    require(verified.contains("BetaVault") && verified.contains("integration PASS"),
        "BetaEnergistics BetaVault integration check failed");
    Path jar = build.resolve("support.jar");
    List<String> command = new ArrayList<String>();
    command.add(Paths.get(System.getProperty("java.home"), "bin", "jar").toString());
    command.addAll(Arrays.asList("--create", "--file", jar.toString()));
    Path itemref = root.resolve(".worldline/build/classes/itemref");
    if (!Files.isDirectory(itemref))
      itemref = root.resolve(".worldline/candidates").resolve(ID).resolve("classes/itemref");
    require(
        Files.isDirectory(itemref), "compiled Worldline itemref module absent; run Gate first");
    command.addAll(Arrays.asList("-C", itemref.toString(), "."));
    Path integration = energy.resolve(".betaenergistics/build/integration");
    for (String name : Arrays.asList("betavault-core", "betavault-codec", "betavault-journal",
             "betavault-store", "betavault-adapter", "product")) {
      Path classes = integration.resolve(name);
      require(Files.isDirectory(classes), "missing " + name);
      command.addAll(Arrays.asList("-C", classes.toString(), "."));
    }
    Captured.run(root, command, 30);
    require(Files.isRegularFile(jar), "support jar absent");
  }

  private Path external(String name, String pathKey, String originKey, String revisionKey)
      throws Exception {
    Path path = root.resolve(value(pathKey)).normalize();
    require(Files.exists(path.resolve(".git"), LinkOption.NOFOLLOW_LINKS), name + " absent");
    if (originKey != null)
      require(git(path, "remote", "get-url", "origin").trim().equals(value(originKey)),
          name + " origin drift");
    verifyClean(path, value(revisionKey));
    return path;
  }

  private void verifyClean(Path path, String revision) throws Exception {
    require(git(path, "rev-parse", "HEAD").trim().equals(revision), "revision drift: " + path);
    require(git(path, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(),
        "dirty checkout: " + path);
  }

  private void addWorktree(Path repo, Path target) throws Exception {
    Files.createDirectories(target.getParent());
    git(repo, "worktree", "add", "--detach", target.toString(), value("aero.revision"));
  }

  private void removeWorktree(Path repo, Path target) {
    try {
      if (registered(repo, target)) {
        try {
          git(repo, "worktree", "remove", "--force", target.toString());
        } catch (Exception error) {
          if (registered(repo, target))
            throw error;
        }
      }
      if (Files.exists(target))
        deleteRemainder(target);
    } catch (Exception error) {
      throw new IllegalStateException("could not remove worktree " + target, error);
    }
  }

  private boolean registered(Path repo, Path target) throws Exception {
    for (String row : git(repo, "worktree", "list", "--porcelain").split("\\R")) {
      if (row.startsWith("worktree ")
          && Paths.get(row.substring(9))
              .toAbsolutePath()
              .normalize()
              .equals(target.toAbsolutePath().normalize()))
        return true;
    }
    return false;
  }

  private void deleteRemainder(Path target) throws Exception {
    Path allowed = root.resolve(".worldline/worktrees").normalize();
    Path exact = target.toAbsolutePath().normalize();
    require(exact.startsWith(allowed) && !exact.equals(allowed), "unsafe worktree remainder");
    try (Stream<Path> paths = Files.walk(exact)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
        Files.deleteIfExists(path);
    }
  }

  private String git(Path path, String... arguments) throws Exception {
    List<String> command = new ArrayList<String>();
    command.add("git");
    command.add("-C");
    command.add(path.toString());
    command.addAll(Arrays.asList(arguments));
    return Captured.run(root, command, 60);
  }

  private void load() throws IOException {
    try (Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      config.load(reader);
    }
  }
  private String value(String key) {
    String result = config.getProperty(key);
    require(result != null && !result.trim().isEmpty(), "missing " + key);
    return result.trim();
  }
  private String line(String text, String prefix) {
    return text.lines()
        .filter(row -> row.startsWith(prefix))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("missing " + prefix + "\n" + diagnostic(text)));
  }
  private String field(String row, String name) {
    for (String token : row.split(" +"))
      if (token.startsWith(name + "="))
        return token.substring(name.length() + 1);
    throw new IllegalStateException("missing " + name);
  }
  private String diagnostic(String text) {
    return text.lines()
        .filter(
            row -> row.contains("ITEMREF") || row.contains("Exception") || row.contains("BUILD "))
        .collect(Collectors.joining("\n"));
  }
  private String wrapper() {
    return System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
  }
  private static int freePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
  private static String sha256(String value) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return java.util.HexFormat.of().formatHex(
        digest.digest(value.getBytes(StandardCharsets.UTF_8)));
  }
  private void recreate(Path target) throws IOException {
    if (Files.exists(target)) {
      require(target.startsWith(root.resolve(".worldline/smokes")), "unsafe build path");
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    }
    Files.createDirectories(target);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
  private static final class Phase {
    final String reference, server;
    Phase(String reference, String server) {
      this.reference = reference;
      this.server = server;
    }
  }

  private static final class Captured {
    final Process process;
    final StringBuilder text = new StringBuilder();
    final Thread reader;
    private Captured(Process process) {
      this.process = process;
      reader = new Thread(() -> {
        try (BufferedReader input = new BufferedReader(
                 new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
          String row;
          while ((row = input.readLine()) != null)
            synchronized (text) {
              text.append(row).append('\n');
              text.notifyAll();
            }
        } catch (IOException error) {
          synchronized (text) {
            text.append(error).append('\n');
          }
        }
      });
      reader.setDaemon(true);
      reader.start();
    }
    static Captured start(Path directory, List<String> command) throws IOException {
      return new Captured(new ProcessBuilder(command)
              .directory(directory.toFile())
              .redirectErrorStream(true)
              .start());
    }
    static Captured start(Path directory, List<String> command, Path gradleHome)
        throws IOException {
      ProcessBuilder builder =
          new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
      builder.environment().put("GRADLE_USER_HOME", gradleHome.toString());
      return new Captured(builder.start());
    }
    static String run(Path directory, List<String> command, int timeout) throws Exception {
      Captured value = start(directory, command);
      value.finish(timeout);
      return value.output();
    }
    static String run(Path directory, List<String> command, int timeout, Path gradleHome)
        throws Exception {
      Captured value = start(directory, command, gradleHome);
      value.finish(timeout);
      return value.output();
    }
    void awaitText(String expected, int timeout) throws Exception {
      long end = System.currentTimeMillis() + timeout * 1000L;
      synchronized (text) {
        while (!text.toString().contains(expected) && process.isAlive()
            && System.currentTimeMillis() < end)
          text.wait(100L);
      }
      if (!output().contains(expected)) {
        kill();
        throw new IllegalStateException("missing " + expected + "\n" + output());
      }
    }
    void write(String value) throws IOException {
      process.getOutputStream().write(value.getBytes(StandardCharsets.UTF_8));
      process.getOutputStream().flush();
    }
    void finish(int timeout) throws Exception {
      if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
        kill();
        throw new IllegalStateException("timeout\n" + output());
      }
      reader.join(5000L);
      require(process.exitValue() == 0, "process exit " + process.exitValue() + "\n" + output());
    }
    void kill() {
      process.descendants().forEach(ProcessHandle::destroyForcibly);
      process.destroyForcibly();
    }
    String output() {
      synchronized (text) {
        return text.toString();
      }
    }
  }
}
