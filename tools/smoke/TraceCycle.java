import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Verifies M6 trace viewing and exact first-divergence exploration. */
public final class TraceCycle {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
    private final Path smoke = root.resolve("smokes/m6-trace-explorer");
    private final Path build = root.resolve(".worldline/smokes/m6-trace-explorer");

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {"m6-trace-explorer"})) {
            System.err.println("usage: java tools/smoke/TraceCycle.java m6-trace-explorer");
            System.exit(2);
        }
        try { new TraceCycle().execute(); }
        catch (Exception error) { System.err.println("M6 trace cycle failed: " + error.getMessage());
            System.exit(1); }
    }

    private void execute() throws Exception {
        require(Files.isDirectory(client.resolve("classes")), "run ClientCycle before TraceCycle");
        recreate(build);
        String subject = trace("worldline.smoke.clientb173.ControlledClientTickSmoke", subjectPaths(),
                "instrumented-client/"), official = trace("WorldlineClientOracle", officialPaths(),
                "jars/minecraft.jar");
        require(subject.equals(official), "mapped and official state traces diverged before M6 injection");
        Path left = write("mapped.wltrace", subject), right = write("official.wltrace", official);
        Result view = cli("trace", "show", left.toString());
        require(view.status == 0 && view.output.contains("WORLDLINE_TRACE_SHOW=PASS")
                && view.output.contains("records=17") && view.output.contains("index\tlabel\tclientTick")
                && view.output.contains("16\ttick16\t16\t16"), "trace viewer output is incomplete");
        Result equal = cli("trace", "diff", left.toString(), right.toString());
        require(equal.status == 0 && equal.output.contains("WORLDLINE_TRACE_DIFF=EQUAL")
                && equal.output.contains("kind=NONE"), "equal trace comparison failed");
        Path divergent = write("divergent.wltrace", mutate(official, "tick9", "slot", 4L));
        Result difference = cli("trace", "diff", left.toString(), divergent.toString());
        require(difference.status == 3 && difference.output.equals(expectedDifference("2", "4")),
                "first divergence report was not exact\n" + difference.output);
        Result reverse = cli("trace", "diff", divergent.toString(), left.toString());
        require(reverse.status == 3 && reverse.output.equals(expectedDifference("4", "2")),
                "reverse first divergence report was not exact");
        Path invalid = write("invalid.wltrace", subject.replaceFirst(
                "schema=clientTick,worldTime", "schema=clientTick,clientTick"));
        Result rejected = cli("trace", "show", invalid.toString());
        require(rejected.status == 1 && rejected.output.contains("duplicate trace field: clientTick"),
                "invalid trace was not rejected at its schema boundary");
        String signature = sha256(difference.output);
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
            properties.load(reader);
        }
        require(signature.equals(properties.getProperty("expected.divergence.sha256")),
                "divergence report changed: " + signature);
        Files.write(build.resolve("evidence.txt"), ("processes=7\nofficial.oracle=MATCH\nrecords=17\n"
                + "equal=PASS\ninvalid=REJECTED\nfirst.record=9\nfirst.label=tick9\nfirst.field=slot\n"
                + "left=2\nright=4\ndivergence.sha256=" + signature + "\n")
                .getBytes(StandardCharsets.UTF_8));
        System.out.println("M6 trace explorer cycle passed");
        System.out.println("  real traces: mapped client == official client");
        System.out.println("  first divergence: record 9 tick9 field 11 slot, 2 -> 4");
        System.out.println("  divergence SHA-256: " + signature);
    }

    private String trace(String type, List<Path> paths, String source) throws Exception {
        String output = capture(command(paths, type));
        require(output.replace('\\', '/').contains(source), "wrong trace process class source");
        return line(output, "WORLDLINE_STATE_TRACE=");
    }
    private Result cli(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("java", "tools/replay/Replay.java"));
        command.addAll(Arrays.asList(arguments)); return result(command);
    }
    private String mutate(String trace, String label, String field, long replacement) {
        String[] parts = trace.split("\\|", -1), schema = parts[2].substring("schema=".length()).split(",", -1);
        int fieldIndex = Arrays.asList(schema).indexOf(field); require(fieldIndex >= 0, "mutation field missing");
        for (int index = 3; index < parts.length; index++) if (parts[index].startsWith(label + "=")) {
            String[] values = parts[index].substring(label.length() + 1).split(",", -1);
            require(values[fieldIndex].equals("2"), "unexpected mutation baseline"); values[fieldIndex] = Long.toString(replacement);
            parts[index] = label + "=" + String.join(",", values); return String.join("|", parts); }
        throw new IllegalStateException("mutation record missing");
    }
    private String expectedDifference(String left, String right) { return "WORLDLINE_TRACE_DIFF=DIVERGED\n"
            + "kind=VALUE\nrecord.index=9\nrecord.label=tick9\nfield.index=11\nfield=slot\nleft="
            + left + "\nright=" + right + "\n"; }
    private Path write(String name, String value) throws Exception { Path path = build.resolve(name);
        Files.write(path, value.getBytes(StandardCharsets.UTF_8)); return path; }
    private Result result(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start(); String output = new String(
            process.getInputStream().readAllBytes(), StandardCharsets.UTF_8); return new Result(process.waitFor(), output); }
    private String capture(List<String> command) throws Exception { Result result = result(command);
        if (result.status != 0) throw new IllegalStateException(command.get(0) + " failed\n" + result.output);
        return result.output; }
    private List<String> command(List<Path> paths, String type) { return Arrays.asList("java",
            "-Djava.awt.headless=true", "-classpath", classpath(paths), type); }
    private List<Path> subjectPaths() throws Exception { Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> paths = new ArrayList<>(Arrays.asList(client.resolve("classes"), client.resolve("instrumented-client"),
                client.resolve("adapter-classes"), client.resolve("headless-classes"), product("api"),
                product("invariants"), product("trace"),
                product("kernel"), product("reproduction"), workspace.resolve("minecraft/bin"),
                workspace.resolve("jars/minecraft.jar"))); paths.addAll(jars(workspace.resolve("libraries"))); return paths; }
    private List<Path> officialPaths() throws Exception { Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> paths = new ArrayList<>(Arrays.asList(client.resolve("oracle-classes"),
                client.resolve("headless-classes"), product("trace"), workspace.resolve("jars/minecraft.jar")));
        paths.addAll(jars(workspace.resolve("libraries"))); return paths; }
    private List<Path> jars(Path source) throws Exception { try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".jar")).sorted().collect(Collectors.toList()); } }
    private void recreate(Path target) throws Exception { if (Files.exists(target)) try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); }
        Files.createDirectories(target); }
    private String line(String output, String prefix) { return output.lines().filter(value -> value.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private String sha256(String value) throws Exception { return HexFormat.of().formatHex(MessageDigest
            .getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String classpath(List<Path> paths) { return paths.stream().map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static final class Result { final int status; final String output;
        Result(int status, String output) { this.status = status; this.output = output; } }
}
