import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Verifies portable deterministic bundles and the repository replay CLI. */
public final class BundleCycle {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
    private final Path smoke = root.resolve("smokes/m5-reproduction-bundle");
    private final Path build = root.resolve(".worldline/smokes/m5-reproduction-bundle");
    private final Path snapshot = root.resolve(".worldline/smokes/m4-durable-snapshot/first.wls");

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {"m5-reproduction-bundle"})) {
            System.err.println("usage: java tools/smoke/BundleCycle.java m5-reproduction-bundle");
            System.exit(2);
        }
        try { new BundleCycle().execute(); }
        catch (Exception error) { System.err.println("M5 bundle cycle failed: " + error.getMessage());
            System.exit(1); }
    }

    private void execute() throws Exception {
        require(Files.isRegularFile(snapshot), "run SnapshotCycle before BundleCycle"); recreate(build);
        List<Path> runtime = runtimePaths();
        Path scenario = compile(smoke.resolve("src"), build.resolve("classes"), runtime);
        List<Path> packPath = new ArrayList<>(); packPath.add(scenario); packPath.addAll(runtime);
        Path firstFile = build.resolve("first.wlrb"), secondFile = build.resolve("second.wlrb");
        Pack first = pack(packPath, "pack", firstFile), second = pack(packPath, "pack", secondFile);
        require(first.equals(second) && Arrays.equals(Files.readAllBytes(firstFile),
                Files.readAllBytes(secondFile)), "fresh bundle pack processes diverged");
        Path portable = build.resolve("portable copy/m5 bundle.wlrb"); Files.createDirectories(portable.getParent());
        Files.copy(firstFile, portable, StandardCopyOption.REPLACE_EXISTING);
        Replay replayed = replay(firstFile), copied = replay(portable);
        require(replayed.equals(copied) && replayed.bundle.equals(first.bundle)
                && replayed.snapshot.equals(first.snapshot), "portable CLI replays diverged");
        String oracle = capture(command(officialPaths(), "WorldlineClientOracle"));
        require(oracle.replace('\\', '/').contains("jars/minecraft.jar"), "wrong official oracle class source");
        require(replayed.state.equals(stateAt(line(oracle, "WORLDLINE_STATE_TRACE="), "tick4")),
                "bundle replay diverged from official client oracle");
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
            properties.load(reader);
        }
        require(first.bundle.equals(properties.getProperty("expected.bundle.sha256")),
                "bundle diverged from frozen SHA-256: " + first.bundle);
        byte[] corrupt = Files.readAllBytes(firstFile); int payload = index(corrupt, "snapshot=") + 9;
        require(payload >= 9, "bundle snapshot field missing"); corrupt[payload] = (byte) (corrupt[payload] == 'A' ? 'B' : 'A');
        Path corruptFile = build.resolve("corrupt.wlrb"); Files.write(corruptFile, corrupt);
        reject(corruptFile, "bundle snapshot SHA-256 mismatch");
        Path wrongRuntime = build.resolve("wrong-runtime.wlrb"); pack(packPath, "wrong-runtime", wrongRuntime);
        reject(wrongRuntime, "no provider for minecraft-b1.7.4-client");
        Path wrongWorldline = build.resolve("wrong-worldline.wlrb"); pack(packPath, "wrong-worldline", wrongWorldline);
        reject(wrongWorldline, "Worldline version mismatch");
        Path wrongClient = build.resolve("wrong-client.wlrb"); pack(packPath, "wrong-client", wrongClient);
        reject(wrongClient, "client SHA-256 mismatch");
        Path wrongToolchain = build.resolve("wrong-toolchain.wlrb"); pack(packPath, "wrong-toolchain", wrongToolchain);
        reject(wrongToolchain, "toolchain revision mismatch");
        Files.write(build.resolve("evidence.txt"), ("valid.processes=5\nfixture.builders=4\nrejected=5\n"
                + "official.oracle=MATCH\nstate=" + replayed.state + "\nsnapshot.sha256=" + first.snapshot
                + "\nbundle.sha256=" + first.bundle + "\n").getBytes(StandardCharsets.UTF_8));
        System.out.println("M5 reproduction bundle cycle passed");
        System.out.println("  valid processes: 2 pack + 2 replay CLI + 1 official oracle");
        System.out.println("  rejected inputs: corruption, runtime, Worldline, client, toolchain");
        System.out.println("  bundle SHA-256: " + first.bundle);
    }

    private Pack pack(List<Path> paths, String mode, Path file) throws Exception {
        String output = capture(command(paths, "worldline.smoke.m5.M5BundleSmoke", mode,
                snapshot.toString(), file.toString())); require(output.contains("WORLDLINE_M5_PACK=PASS"),
                "bundle pack marker missing"); return new Pack(line(output, "bundle.sha256="),
                line(output, "snapshot.sha256=")); }
    private Replay replay(Path file) throws Exception { String output = capture(Arrays.asList("java",
            "tools/replay/Replay.java", "replay", file.toString())); require(output.contains("WORLDLINE_REPLAY=PASS"),
            "replay CLI marker missing"); return new Replay(line(output, "bundle.sha256="),
                line(output, "snapshot.sha256="), line(output, "runtime="), line(output, "state=")); }
    private void reject(Path file, String message) throws Exception { String output = captureFailure(Arrays.asList(
            "java", "tools/replay/Replay.java", "replay", file.toString()));
        require(output.contains(message), "CLI rejection did not report " + message); }
    private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
        Files.createDirectories(output); List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding",
                "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                classpath(dependencies), "-d", output.toString())); javaFiles(source).forEach(path -> command.add(path.toString()));
        capture(command); return output; }
    private List<Path> runtimePaths() throws Exception { Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> paths = new ArrayList<>(Arrays.asList(client.resolve("adapter-classes"), product("reproduction"),
                product("api"), product("kernel"), client.resolve("instrumented-client"),
                client.resolve("headless-classes"), workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar")));
        paths.addAll(jars(workspace.resolve("libraries"))); return paths; }
    private List<Path> officialPaths() throws Exception { Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> paths = new ArrayList<>(Arrays.asList(client.resolve("oracle-classes"),
                client.resolve("headless-classes"), product("trace"), workspace.resolve("jars/minecraft.jar")));
        paths.addAll(jars(workspace.resolve("libraries"))); return paths; }
    private List<String> command(List<Path> paths, String type, String... arguments) { List<String> result = new ArrayList<>(
            Arrays.asList("java", "-Djava.awt.headless=true", "-classpath", classpath(paths), type));
        result.addAll(Arrays.asList(arguments)); return result; }
    private String capture(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start(); String output = new String(
            process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output); return output; }
    private String captureFailure(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start(); String output = new String(
            process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() != 0, "invalid bundle unexpectedly replayed"); return output; }
    private String stateAt(String trace, String label) { String marker = "|" + label + "="; int start = trace.indexOf(marker);
        require(start >= 0, "official trace lacks " + label); int end = trace.indexOf('|', start + marker.length());
        return label + "=" + trace.substring(start + marker.length(), end < 0 ? trace.length() : end); }
    private String line(String output, String prefix) { return output.lines().filter(value -> value.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private int index(byte[] value, String text) { byte[] target = text.getBytes(StandardCharsets.US_ASCII);
        outer: for (int at = 0; at <= value.length - target.length; at++) { for (int index = 0; index < target.length; index++)
            if (value[at + index] != target[index]) continue outer; return at; } return -1; }
    private void recreate(Path target) throws Exception { if (Files.exists(target)) try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); }
        Files.createDirectories(target); }
    private List<Path> javaFiles(Path source) throws Exception { try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().collect(Collectors.toList()); } }
    private List<Path> jars(Path source) throws Exception { try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".jar")).sorted().collect(Collectors.toList()); } }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String classpath(List<Path> paths) { return paths.stream().map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static class Pack { final String bundle, snapshot; Pack(String bundle, String snapshot) {
            this.bundle = bundle; this.snapshot = snapshot; } @Override public boolean equals(Object other) {
            return other instanceof Pack && bundle.equals(((Pack) other).bundle) && snapshot.equals(((Pack) other).snapshot); }
        @Override public int hashCode() { return 31 * bundle.hashCode() + snapshot.hashCode(); } }
    private static final class Replay extends Pack { final String runtime, state;
        Replay(String bundle, String snapshot, String runtime, String state) { super(bundle, snapshot);
            this.runtime = runtime; this.state = state; } @Override public boolean equals(Object other) {
            return other instanceof Replay && super.equals(other) && runtime.equals(((Replay) other).runtime)
                    && state.equals(((Replay) other).state); } @Override public int hashCode() {
            return 31 * (31 * super.hashCode() + runtime.hashCode()) + state.hashCode(); } }
}
