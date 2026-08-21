import java.io.IOException;
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

/** Builds and differentially verifies the first controlled client tick. */
public final class ClientCycle {
    private static final String ID = "controlled-client-tick";
    private static final String TRACE = "WORLDLINE_SMOKE_TRACE=";
    private static final String SIGNATURE = "WORLDLINE_SMOKE_SIGNATURE=";
    private static final String STATE_TRACE = "WORLDLINE_STATE_TRACE=", STATE_SIGNATURE = "WORLDLINE_STATE_SIGNATURE=";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/ClientCycle.java " + ID);
            System.exit(2);
        }
        try { new ClientCycle().execute(); }
        catch (Exception error) {
            System.err.println("client cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID);
        load(smoke.resolve("smoke.properties"), config);
        require(ID.equals(required("id")), "smoke descriptor id mismatch");
        Path workspace = local(required("workspace"));
        verifyInputs(workspace);
        Path mapped = workspace.resolve("minecraft/bin");
        if (!Files.isRegularFile(mapped.resolve("net/minecraft/client/Minecraft.class"))) {
            stage("RetroMCP decompile and recompile client", workspace,
                    "java", "-jar", "RetroMCP-CLI.jar", "decompile", required("side"));
        }
        require(Files.isRegularFile(mapped.resolve("net/minecraft/client/Minecraft.class")),
                "RetroMCP produced no mapped client Minecraft.class");
        verifySymbols(workspace.resolve("conf/mappings.tiny"), smoke.resolve("symbols.map"));

        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize();
        recreate(build, root.resolve(".worldline").normalize());
        Path adapterRoot = root.resolve("adapters/b173-client");
        Path stubs = compile(adapterRoot.resolve("headless-src"), build.resolve("headless-classes"),
                new ArrayList<>(), "headless boundary compilation");
        List<Path> libraries = jarFiles(workspace.resolve("libraries"));
        List<Path> adapterDependencies = paths(stubs, mapped, product("api"), product("kernel"),
                product("reproduction"), product("trace"), product("mods"), product("modtest"),
                product("minimization"), product("fuzz"));
        adapterDependencies.addAll(libraries);
        Path adapter = compile(adapterRoot.resolve("src/main/java"), build.resolve("adapter-classes"),
                adapterDependencies, "reusable b1.7.3 adapter compilation");
        Path instrumented = instrumentClient(workspace, build, adapter, stubs, mapped, libraries);
        List<Path> subjectDependencies = paths(adapter, product("api"), product("invariants"), product("trace"));
        Path subject = compile(smoke.resolve("src"), build.resolve("classes"),
                subjectDependencies, "mapped client scenario compilation");
        Path officialJar = workspace.resolve("jars/minecraft.jar");
        List<Path> oracleDependencies = paths(stubs, product("trace"), officialJar);
        oracleDependencies.addAll(libraries);
        Path oracle = compile(smoke.resolve("oracle-src"), build.resolve("oracle-classes"),
                oracleDependencies, "official client oracle compilation");
        verifyControlPath(subject, adapter, instrumented, oracle, mapped, officialJar, stubs, libraries);

        List<Path> subjectRuntime = paths(subject, instrumented, adapter, stubs, product("api"),
                product("invariants"), product("trace"), product("kernel"), mapped, officialJar);
        subjectRuntime.addAll(libraries);
        List<Path> oracleRuntime = paths(oracle, stubs, product("trace"), officialJar);
        oracleRuntime.addAll(libraries);
        Outcome first = runScenario("worldline.smoke.clientb173.ControlledClientTickSmoke",
                subjectRuntime, "Minecraft.runTick", "instrumented-client/");
        Outcome second = runScenario("worldline.smoke.clientb173.ControlledClientTickSmoke",
                subjectRuntime, "Minecraft.runTick", "instrumented-client/");
        Outcome officialFirst = runScenario("WorldlineClientOracle", oracleRuntime,
                "net.minecraft.client.Minecraft.k", "jars/minecraft.jar");
        Outcome officialSecond = runScenario("WorldlineClientOracle", oracleRuntime,
                "net.minecraft.client.Minecraft.k", "jars/minecraft.jar");
        same(first, second, "mapped client processes");
        same(officialFirst, officialSecond, "official client processes");
        same(first, officialFirst, "mapped client and official client");
        require(required("expected.signature").equals(first.signature),
                "trace diverged from frozen signature: " + first.signature);
        require(required("expected.state.signature").equals(first.stateSignature),
                "state trace diverged from frozen signature: " + first.stateSignature);
        Path evidence = writeEvidence(build, first);
        System.out.println("controlled client cycle passed");
        System.out.println("  processes: 4 (2 mapped client, 2 official client)");
        System.out.println("  bootHeadless -> loadWorld -> tick(1): verified");
        System.out.println("  official client oracle: MATCH");
        System.out.println("  signature: " + first.signature);
        System.out.println("  16-tick state signature: " + first.stateSignature);
        System.out.println("  trace: " + first.trace);
        System.out.println("  evidence: " + root.relativize(evidence));
    }

    private void verifyInputs(Path workspace) throws Exception {
        verifyHash(workspace.resolve("jars/minecraft.jar"), required("client.jar.sha256"));
        verifyHash(workspace.resolve("conf/version.json"), required("version.json.sha256"));
        verifyHash(workspace.resolve("conf/mappings.tiny"), required("mappings.tiny.sha256"));
        verifyHash(workspace.resolve("conf/exceptions.exc"), required("exceptions.exc.sha256"));
    }

    private void verifySymbols(Path mappingsPath, Path symbolsPath) throws IOException {
        List<String> mappings = Files.readAllLines(mappingsPath, StandardCharsets.UTF_8);
        int verified = 0;
        for (String row : Files.readAllLines(symbolsPath, StandardCharsets.UTF_8)) {
            if (row.isEmpty() || row.startsWith("#")) continue;
            String[] columns = row.split("\t", -1);
            if (columns.length == 5) { columns = Arrays.copyOf(columns, 6); columns[5] = ""; }
            require(columns.length == 6, "invalid symbols.map row: " + row);
            String owner = "c\t" + columns[0];
            int start = -1;
            for (int index = 0; index < mappings.size(); index++) {
                if (mappings.get(index).startsWith(owner + "\t")) { start = index; break; }
            }
            require(start >= 0, "mapped owner is absent: " + columns[0]);
            int end = start + 1; while (end < mappings.size() && !mappings.get(end).startsWith("c\t")) end++;
            String expected = columns[1].equals("c")
                    ? "c\t" + columns[3] + "\t" + columns[4] + "\t" + columns[5]
                    : "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3]
                            + "\t" + columns[4] + "\t" + columns[5];
            require(mappings.subList(start, end).contains(expected),
                    "mapped symbol is absent from owner " + columns[0] + ": " + expected);
            verified++;
        }
        System.out.println("  client mapped symbols: " + verified + " verified");
    }

    private Path compile(Path sources, Path output, List<Path> dependencies, String label)
            throws Exception {
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror"));
        if (!dependencies.isEmpty()) {
            command.add("-classpath");
            command.add(classpath(dependencies));
        }
        command.add("-d");
        command.add(output.toString());
        javaFiles(sources).forEach(path -> command.add(path.toString()));
        stage(label, root, command.toArray(new String[0]));
        return output;
    }

    private Path instrumentClient(Path workspace, Path build, Path adapter, Path stubs,
            Path mapped, List<Path> libraries) throws Exception {
        Path source = workspace.resolve("minecraft/src/net/minecraft/client/Minecraft.java");
        String original = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        String call = "System.currentTimeMillis()";
        int count = (original.length() - original.replace(call, "").length()) / call.length();
        require(count == 7, "unexpected client clock call count: " + count);
        Path generated = build.resolve("instrumented-src/net/minecraft/client/Minecraft.java");
        Files.createDirectories(generated.getParent());
        String transformed = original.replace(call, "worldline.b173.B173ClockHooks.currentTimeMillis()")
                .replace("(GuiScreen)var1", "var1").replace("public abstract class Minecraft",
                        "@SuppressWarnings(\"this-escape\")\npublic abstract class Minecraft");
        Files.write(generated, transformed.getBytes(StandardCharsets.UTF_8));
        List<Path> dependencies = paths(adapter, mapped, product("api"), product("kernel"), product("reproduction"));
        dependencies.addAll(libraries);
        dependencies.add(stubs);
        return compile(build.resolve("instrumented-src"), build.resolve("instrumented-client"),
                dependencies, "virtual-clock client instrumentation");
    }

    private void verifyControlPath(Path subject, Path adapter, Path instrumented, Path oracle,
            Path mapped, Path official, Path stubs, List<Path> libraries) throws Exception {
        List<Path> subjectPaths = paths(subject, instrumented, adapter, stubs, product("api"),
                product("invariants"), product("trace"), product("kernel"), mapped, official);
        subjectPaths.addAll(libraries);
        List<Path> oraclePaths = paths(oracle, stubs, product("trace"), official);
        oraclePaths.addAll(libraries);
        String driver = javap(subjectPaths, "worldline.smoke.clientb173.ControlledClientTickSmoke");
        String backend = javap(subjectPaths, "worldline.b173.B173ClientBackend");
        String direct = javap(oraclePaths, "WorldlineClientOracle");
        require(driver.contains("B173Runtime.tick:(I)V")
                        && backend.contains("Minecraft.runTick:()V")
                        && direct.contains("net/minecraft/client/Minecraft.k:()V"),
                "compiled client control paths do not reach both tick roots");
        System.out.println("  control path: tick(1) -> Minecraft.runTick / Minecraft.k verified");
    }

    private String javap(List<Path> paths, String type) throws Exception {
        return capture(root, "javap", "-classpath", classpath(paths), "-c", "-p", type);
    }

    private Outcome runScenario(String type, List<Path> runtime, String rootName,
            String sourceMarker) throws Exception {
        String output = capture(root, "java", "-Djava.awt.headless=true", "-classpath",
                classpath(runtime), type);
        require(output.contains("WORLDLINE_CLIENT_ROOT=" + rootName), "wrong client tick root");
        require(output.contains("WORLDLINE_CLIENT_HEADLESS=true"), "headless proof is absent");
        require(output.replace('\\', '/').contains(sourceMarker), "wrong Minecraft class source");
        require(!type.contains("ControlledClientTickSmoke") || output.contains(
                "WORLDLINE_BOUNDARIES=clock,input,rng,scheduler,filesystem,network,threading"),
                "M2 boundary proof is absent");
        return new Outcome(line(output, TRACE), line(output, SIGNATURE),
                line(output, STATE_TRACE), line(output, STATE_SIGNATURE));
    }

    private void same(Outcome left, Outcome right, String label) {
        require(left.trace.equals(right.trace) && left.signature.equals(right.signature)
                        && left.stateTrace.equals(right.stateTrace)
                        && left.stateSignature.equals(right.stateSignature),
                label + " produced different canonical traces");
    }

    private Path writeEvidence(Path build, Outcome outcome) throws IOException {
        Path evidence = build.resolve("evidence.txt");
        String value = "id=" + ID + "\nprocesses=4\nmapped.processes=2\nofficial.processes=2"
                + "\nclient.jar.sha256=" + required("client.jar.sha256")
                + "\nheadless=true\ntick.root.named=Minecraft.runTick"
                + "\ntick.root.official=net.minecraft.client.Minecraft.k"
                + "\nofficial.oracle=MATCH\nsignature=" + outcome.signature
                + "\ntrace=" + outcome.trace + "\nstate.signature=" + outcome.stateSignature
                + "\nstate.trace=" + outcome.stateTrace + "\n";
        Files.write(evidence, value.getBytes(StandardCharsets.UTF_8));
        return evidence;
    }

    private void verifyHash(Path path, String expected) throws Exception {
        require(Files.isRegularFile(path), "missing frozen client input: " + path);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (java.io.InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        require(HexFormat.of().formatHex(digest.digest()).equals(expected),
                "frozen client input drift: " + path);
    }

    private List<Path> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .collect(Collectors.toList());
        }
    }

    private List<Path> jarFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.toString().endsWith("-sources.jar")).sorted()
                    .collect(Collectors.toList());
        }
    }

    private void recreate(Path target, Path safeRoot) throws IOException {
        require(target.startsWith(safeRoot) && !target.equals(safeRoot),
                "unsafe generated output path: " + target);
        if (Files.exists(target)) {
            try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                    Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }

    private void stage(String label, Path directory, String... command) throws Exception {
        capture(directory, command);
        System.out.println("  " + label + ": passed");
    }

    private String capture(Path directory, String... command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        if (exit != 0) throw new IllegalStateException(command[0] + " exited " + exit + "\n" + output);
        return output;
    }

    private String line(String output, String prefix) {
        List<String> matches = Arrays.stream(output.split("\\R"))
                .filter(value -> value.startsWith(prefix)).collect(Collectors.toList());
        require(matches.size() == 1, "scenario emitted " + matches.size() + " lines for " + prefix);
        return matches.get(0).substring(prefix.length());
    }

    private Path product(String module) { return root.resolve(".worldline/build/classes").resolve(module); }

    private List<Path> paths(Path... paths) { return new ArrayList<>(Arrays.asList(paths)); }

    private String classpath(List<Path> paths) {
        return paths.stream().map(Path::toString)
                .collect(Collectors.joining(System.getProperty("path.separator")));
    }

    private Path local(String relative) {
        Path local = root.resolve("local").normalize();
        Path path = root.resolve(relative).normalize();
        require(path.startsWith(local) && !path.equals(local), "workspace must be inside local/");
        return path;
    }

    private String required(String key) {
        String value = config.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing smoke property: " + key);
        return value.trim();
    }

    private void load(Path path, Properties properties) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Outcome {
        private final String trace, signature, stateTrace, stateSignature;
        private Outcome(String trace, String signature, String stateTrace, String stateSignature) {
            this.trace = trace; this.signature = signature;
            this.stateTrace = stateTrace; this.stateSignature = stateSignature;
        }
    }
}
