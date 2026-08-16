import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Verifies canonical M4 snapshot capture and cross-process restore. */
public final class SnapshotCycle {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
    private final Path smoke = root.resolve("smokes/m4-durable-snapshot");
    private final Path build = root.resolve(".worldline/smokes/m4-durable-snapshot");

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {"m4-durable-snapshot"})) {
            System.err.println("usage: java tools/smoke/SnapshotCycle.java m4-durable-snapshot");
            System.exit(2);
        }
        try { new SnapshotCycle().execute(); }
        catch (Exception error) { System.err.println("M4 snapshot cycle failed: " + error.getMessage());
            System.exit(1); }
    }

    private void execute() throws Exception {
        require(Files.isDirectory(client.resolve("adapter-classes")), "run ClientCycle before SnapshotCycle");
        recreate(build);
        List<Path> dependencies = paths(client.resolve("adapter-classes"), product("api"), product("kernel"));
        Path scenario = compile(smoke.resolve("src"), build.resolve("classes"), dependencies);
        Path official = root.resolve("local/workspaces/b1.7.3/jars/minecraft.jar");
        List<Path> subject = paths(scenario, client.resolve("instrumented-client"),
                client.resolve("adapter-classes"), client.resolve("headless-classes"), product("api"),
                product("kernel"), root.resolve("local/workspaces/b1.7.3/minecraft/bin"), official);
        subject.addAll(jars(root.resolve("local/workspaces/b1.7.3/libraries")));
        Path firstFile = build.resolve("first.wls"), secondFile = build.resolve("second.wls");
        Outcome first = run(subject, "capture", firstFile), second = run(subject, "capture", secondFile);
        require(first.equals(second) && Arrays.equals(Files.readAllBytes(firstFile),
                Files.readAllBytes(secondFile)), "fresh snapshot captures diverged");
        Outcome restored = run(subject, "restore", firstFile), replayed = run(subject, "restore", secondFile);
        require(first.equals(restored) && restored.equals(replayed), "cross-process restore diverged");
        List<Path> oraclePath = paths(client.resolve("oracle-classes"),
                client.resolve("headless-classes"), product("trace"), official);
        oraclePath.addAll(jars(root.resolve("local/workspaces/b1.7.3/libraries")));
        String oracle = capture(command(oraclePath, "WorldlineClientOracle"));
        require(oracle.replace('\\', '/').contains("jars/minecraft.jar"), "wrong official oracle class source");
        require(first.state.equals(stateAt(line(oracle, "WORLDLINE_STATE_TRACE="), "tick4")),
                "restored state diverged from official client oracle");
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
            properties.load(reader);
        }
        require(first.sha.equals(properties.getProperty("expected.snapshot.sha256")),
                "snapshot diverged from frozen SHA-256: " + first.sha);
        byte[] corrupt = Files.readAllBytes(firstFile); int state = index(corrupt, "state=") + 6;
        require(state >= 6, "snapshot state field is missing");
        corrupt[state] = (byte) (corrupt[state] == 'A' ? 'B' : 'A');
        Path corruptFile = build.resolve("corrupt.wls"); Files.write(corruptFile, corrupt);
        reject(subject, corruptFile, "snapshot checksum mismatch");
        Path versionFile = build.resolve("wrong-version.wls"); Files.write(versionFile,
                modified(Files.readAllBytes(firstFile), "WORLDLINE-SNAPSHOT/1", "WORLDLINE-SNAPSHOT/2"));
        reject(subject, versionFile, "unsupported snapshot version");
        Path runtimeFile = build.resolve("wrong-runtime.wls"); Files.write(runtimeFile,
                modified(Files.readAllBytes(firstFile), "minecraft-b1.7.3-client", "minecraft-b1.7.4-client"));
        reject(subject, runtimeFile, "snapshot runtime mismatch");
        Files.write(build.resolve("evidence.txt"), ("processes=5+3 rejected\nofficial.oracle=MATCH\n"
                + "corruption=REJECTED\nversion=REJECTED\nruntime=REJECTED\nstate=" + first.state + "\nfingerprint=" + first.fingerprint
                + "\nsnapshot.sha256=" + first.sha + "\n").getBytes(StandardCharsets.UTF_8));
        System.out.println("M4 durable snapshot cycle passed");
        System.out.println("  processes: 5 successful + 3 rejected inputs");
        System.out.println("  official client oracle: MATCH");
        System.out.println("  snapshot SHA-256: " + first.sha);
    }

    private Outcome run(List<Path> classpath, String mode, Path file) throws Exception {
        String output = capture(command(classpath, "worldline.smoke.m4.M4SnapshotSmoke", mode, file.toString()));
        require(output.replace('\\', '/').contains("instrumented-client/"), "wrong Minecraft class source");
        require(output.contains("WORLDLINE_M4_ROUNDTRIP=true"), "snapshot round-trip marker missing");
        return new Outcome(line(output, "WORLDLINE_M4_SNAPSHOT_SHA="),
                line(output, "WORLDLINE_M4_STATE="), line(output, "WORLDLINE_M4_FINGERPRINT="));
    }

    private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
        Files.createDirectories(output); List<String> command = new ArrayList<>(Arrays.asList("javac",
                "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror",
                "-classpath", classpath(dependencies), "-d", output.toString()));
        javaFiles(source).forEach(path -> command.add(path.toString())); capture(command); return output;
    }
    private List<String> command(List<Path> paths, String type, String... arguments) {
        List<String> result = new ArrayList<>(Arrays.asList("java", "-Djava.awt.headless=true",
                "-classpath", classpath(paths), type)); result.addAll(Arrays.asList(arguments)); return result;
    }
    private String capture(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start(); String output = new String(
            process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output); return output; }
    private String captureFailure(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start(); String output = new String(
            process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() != 0, "corrupt snapshot unexpectedly restored"); return output; }
    private void reject(List<Path> paths, Path file, String message) throws Exception {
        String output = captureFailure(command(paths, "worldline.smoke.m4.M4SnapshotSmoke", "restore", file.toString()));
        require(output.contains(message), "snapshot rejection did not report " + message); }
    private byte[] modified(byte[] input, String before, String after) {
        require(before.length() == after.length(), "replacement length mismatch"); int at = index(input, before);
        require(at >= 0, "snapshot replacement target missing"); byte[] result = input.clone();
        byte[] replacement = after.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(replacement, 0, result, at, replacement.length); return result; }
    private String stateAt(String trace, String label) { String marker = "|" + label + "=";
        int start = trace.indexOf(marker); require(start >= 0, "official trace lacks " + label);
        int end = trace.indexOf('|', start + marker.length()); return label + "="
                + trace.substring(start + marker.length(), end < 0 ? trace.length() : end); }
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
    private List<Path> paths(Path... value) { return new ArrayList<>(Arrays.asList(value)); }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String classpath(List<Path> paths) { return paths.stream().map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }

    private static final class Outcome {
        private final String sha, state, fingerprint;
        Outcome(String sha, String state, String fingerprint) { this.sha = sha; this.state = state;
            this.fingerprint = fingerprint; }
        @Override public boolean equals(Object other) { return other instanceof Outcome && sha.equals(((Outcome) other).sha)
                && state.equals(((Outcome) other).state) && fingerprint.equals(((Outcome) other).fingerprint); }
        @Override public int hashCode() { return 31 * (31 * sha.hashCode() + state.hashCode()) + fingerprint.hashCode(); }
    }
}
